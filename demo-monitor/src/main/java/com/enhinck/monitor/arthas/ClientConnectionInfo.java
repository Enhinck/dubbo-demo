

package com.enhinck.monitor.arthas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Promise;
import lombok.Data;

/**
 * @author xiaomi（huenbin ）
 * @since 2021-05-19 12:50
 */
@Data
public class ClientConnectionInfo {

    @JsonIgnore
    private ChannelHandlerContext channelHandlerContext;
    private String host;
    private int port;
    /**
     * wait for agent connect
     */
    @JsonIgnore
    private Promise<Channel> promise;
}