

package com.enhinck.monitor.arthas.config;

import com.enhinck.monitor.arthas.TunnelServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author xiaomi（huenbin ）
 * @since 2021-05-19 12:50
 */
@Configuration
@EnableConfigurationProperties(ArthasProperties.class)
public class TunnelServerConfiguration {

    @Autowired
    ArthasProperties arthasProperties;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TunnelServer tunnelServer() {
        TunnelServer tunnelServer = new TunnelServer();

        tunnelServer.setHost(arthasProperties.getServer().getHost());
        tunnelServer.setPort(arthasProperties.getServer().getPort());
        tunnelServer.setSsl(arthasProperties.getServer().isSsl());
        return tunnelServer;
    }

}