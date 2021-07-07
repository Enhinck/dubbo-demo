

package com.enhinck.monitor.arthas;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;
import io.netty.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author xiaomi（huenbin ）
 * @since 2021-05-19 12:50
 */
@Slf4j
public class TunnelSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final static Logger logger = LoggerFactory.getLogger(TunnelSocketFrameHandler.class);

    private TunnelServer tunnelServer;

    public TunnelSocketFrameHandler(TunnelServer tunnelServer) {
        this.tunnelServer = tunnelServer;
    }

    public static final String CONNECT_ARTHAS = "connectArthas";
    public static final String AGENT_REGISTER = "agentRegister";
    public static final String OPEN_TUNNEL = "openTunnel";




    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HandshakeComplete) {
            HandshakeComplete handshake = (HandshakeComplete) evt;
            // http request uri
            String uri = handshake.requestUri();
            logger.info("websocket handshake complete, uri: {}", uri);

            MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
            String method = parameters.getFirst("method");

            if (CONNECT_ARTHAS.equals(method)) {
                // form browser
                connectArthas(ctx, parameters);
            } else if (AGENT_REGISTER.equals(method)) {
                // form arthas agent, register
                agentRegister(ctx, uri);
            }
            if (OPEN_TUNNEL.equals(method)) {
                // from arthas agent open tunnel
                String clientConnectionId = parameters.getFirst("clientConnectionId");
                openTunnel(ctx, clientConnectionId);
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

    }

    private void connectArthas(ChannelHandlerContext tunnelSocketCtx, MultiValueMap<String, String> parameters)
            throws URISyntaxException {
        List<String> agentId = parameters.getOrDefault("id", Collections.emptyList());
        if (agentId.isEmpty()) {
            log.error("arthas agent id can not be null, parameters: ", parameters);
            throw new IllegalArgumentException("arthas agent id can not be null");
        }
        log.info("try to connect to arthas agent, id: " + agentId.get(0));
        Optional<AgentInfo> findAgent = tunnelServer.findAgent(agentId.get(0));
        if (findAgent.isPresent()) {
            ChannelHandlerContext agentCtx = findAgent.get().getChannelHandlerContext();
            String clientConnectionId = RandomStringUtils.random(20, true, true).toUpperCase();
            log.info("random clientConnectionId: " + clientConnectionId);
            URI uri = new URI("response", null, "/",
                    "method=startTunnel" + "&id=" + agentId.get(0) + "&clientConnectionId=" + clientConnectionId, null);
            log.info("startTunnel response: " + uri);
            ClientConnectionInfo clientConnectionInfo = new ClientConnectionInfo();
            SocketAddress remoteAddress = tunnelSocketCtx.channel().remoteAddress();
            if (remoteAddress instanceof InetSocketAddress) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
                clientConnectionInfo.setHost(inetSocketAddress.getHostString());
                clientConnectionInfo.setPort(inetSocketAddress.getPort());
            }
            clientConnectionInfo.setChannelHandlerContext(tunnelSocketCtx);
            // when the agent open tunnel success, will set result into the promise
            Promise<Channel> promise = GlobalEventExecutor.INSTANCE.newPromise();
            promise.addListener(new FutureListener<Channel>() {
                @Override
                public void operationComplete(final Future<Channel> future) throws Exception {
                    final Channel outboundChannel = future.getNow();
                    if (future.isSuccess()) {
                        tunnelSocketCtx.pipeline().remove(TunnelSocketFrameHandler.this);
                        // outboundChannel is form arthas agent
                        outboundChannel.pipeline().removeLast();
                        outboundChannel.pipeline().addLast(new RelayHandler(tunnelSocketCtx.channel()));
                        tunnelSocketCtx.pipeline().addLast(new RelayHandler(outboundChannel));
                    } else {
                        logger.error("wait for agent connect error. agentId: {}, clientConnectionId: {}", agentId,
                                clientConnectionId);
                        ChannelUtils.closeOnFlush(agentCtx.channel());
                    }
                }
            });
            clientConnectionInfo.setPromise(promise);
            this.tunnelServer.addClientConnectionInfo(clientConnectionId, clientConnectionInfo);
            tunnelSocketCtx.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    tunnelServer.removeClientConnectionInfo(clientConnectionId);
                }
            });
            agentCtx.channel().writeAndFlush(new TextWebSocketFrame(uri.toString()));
            log.info("browser connect waitting for arthas agent open tunnel");
            boolean watiResult = promise.awaitUninterruptibly(20, TimeUnit.SECONDS);
            if (watiResult) {
                log.info(
                        "browser connect wait for arthas agent open tunnel success, agentId: {}, clientConnectionId: {}",
                        agentId, clientConnectionId);
            } else {
                log.error(
                        "browser connect wait for arthas agent open tunnel timeout, agentId: {}, clientConnectionId: {}",
                        agentId, clientConnectionId);
                tunnelSocketCtx.close();
            }
        } else {
            tunnelSocketCtx.channel().writeAndFlush(new CloseWebSocketFrame(2000, "Can not find arthas agent by id: "+ agentId));
            log.error("Can not find arthas agent by id: {}", agentId);
            throw new IllegalArgumentException("Can not find arthas agent by id: " + agentId);
        }
    }

    private void agentRegister(ChannelHandlerContext ctx, String requestUri) throws URISyntaxException {
        // generate a random agent id
        String id = RandomStringUtils.random(20, true, true).toUpperCase();

        QueryStringDecoder queryDecoder = new QueryStringDecoder(requestUri);
        List<String> idList = queryDecoder.parameters().get("id");
        if (idList != null && !idList.isEmpty()) {
            id = idList.get(0);
        }

        final String finalId = id;

        URI responseUri = new URI("response", null, "/", "method=agentRegister" + "&id=" + id, null);

        AgentInfo info = new AgentInfo();
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        if (remoteAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
            info.setHost(inetSocketAddress.getHostString());
            info.setPort(inetSocketAddress.getPort());
        }
        info.setChannelHandlerContext(ctx);

        tunnelServer.addAgent(id, info);
        ctx.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                tunnelServer.removeAgent(finalId);
            }

        });

        ctx.channel().writeAndFlush(new TextWebSocketFrame(responseUri.toString()));
    }

    private void openTunnel(ChannelHandlerContext ctx, String clientConnectionId) {
        Optional<ClientConnectionInfo> infoOptional = this.tunnelServer.findClientConnection(clientConnectionId);

        if (infoOptional.isPresent()) {
            ClientConnectionInfo info = infoOptional.get();
            logger.info("openTunnel clientConnectionId:" + clientConnectionId);

            Promise<Channel> promise = info.getPromise();
            promise.setSuccess(ctx.channel());
        } else {
            logger.error("Can not find client connection by id: {}", clientConnectionId);
        }

    }
}