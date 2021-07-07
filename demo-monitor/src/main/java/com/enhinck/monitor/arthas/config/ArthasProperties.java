

package com.enhinck.monitor.arthas.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author xiaomi（huenbin ）
 * @since 2021-05-19 12:50
 */
@Component
@ConfigurationProperties(prefix = "arthas")
public class ArthasProperties {

    private Server server;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Data
    public static class Server {
        private String host;
        private int port;
        private boolean ssl;
    }

}