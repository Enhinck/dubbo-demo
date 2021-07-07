

package com.enhinck.monitor.arthas.web;

import com.enhinck.monitor.arthas.AgentInfo;
import com.enhinck.monitor.arthas.TunnelServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;


/**
 * Provides informations for the arthas view. Only available clusters until now.
 *
 * @author Johannes Edmeier
 * @author xiaomi（huenbin ）
 * @since 2021-05-19 12:50
 */
@RequestMapping("/api/arthas")
@RestController
public class ArthasController {
    @Autowired
    private TunnelServer tunnelServer;

    @RequestMapping(value = "/clients", method = RequestMethod.GET)
    public Set<String> getClients() {
        Map<String, AgentInfo> agentInfoMap = tunnelServer.getAgentInfoMap();
        return agentInfoMap.keySet();
    }

}