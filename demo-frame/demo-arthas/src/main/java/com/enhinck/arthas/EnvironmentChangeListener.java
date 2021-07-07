package com.enhinck.arthas;

import com.alibaba.arthas.spring.ArthasConfiguration;
import com.alibaba.arthas.spring.ArthasProperties;
import com.alibaba.arthas.spring.StringUtils;
import com.taobao.arthas.agent.attach.ArthasAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>环境变量改变监听
 *
 * @author xiaomi（huenbin ）
 * @since 2021-05-19 12:50
 */
@Component
@Order(100)
@Import(ArthasConfiguration.class)
public class EnvironmentChangeListener implements
        ApplicationListener<EnvironmentChangeEvent> {
    @Autowired
    private Environment env;

    @Autowired
    private Map<String,String> arthasConfigMap;

    @Autowired
    private ArthasProperties arthasProperties;
    @Autowired
    private ApplicationContext applicationContext;
    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        for (String key : keys) {
            if ("spring.arthas.enabled".equals(key)) {
                if ("true".equals(env.getProperty(key))) {

                    registerArthas();
                }
            }
        }
    }
    private void registerArthas() {
        DefaultListableBeanFactory defaultListableBeanFactory =
                (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        String bean = "arthasAgent";
        if (defaultListableBeanFactory.containsBean(bean)) {
            ((ArthasAgent)defaultListableBeanFactory.getBean(bean)).init();
            return;
        }
        defaultListableBeanFactory.registerSingleton(bean, arthasAgentInit());
    }
    private ArthasAgent arthasAgentInit() {
        arthasConfigMap = StringUtils.removeDashKey(arthasConfigMap);
        // 给配置全加上前缀
        Map<String, String> mapWithPrefix = new HashMap<String, String>
                (arthasConfigMap.size());
        for (Map.Entry<String,String> entry : arthasConfigMap.entrySet()) {
            mapWithPrefix.put("arthas." + entry.getKey(), entry.getValue());
        }
        final ArthasAgent arthasAgent = new ArthasAgent(mapWithPrefix,
                arthasProperties.getHome(),
                arthasProperties.isSlientInit(), null);
        arthasAgent.init();


        return arthasAgent;
    }
}