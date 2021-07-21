package com.enhinck.dubbo;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 *
 */
public class ChannelRouterLoadBalance implements LoadBalance {
    public static final String NAME = "router";
    private static final Logger log = LoggerFactory.getLogger(ChannelRouterLoadBalance.class);

    public ChannelRouterLoadBalance() {
        log.info("ChannelRouterLoadBalance Init!");
    }

    public String getRouterApplicationName(String channel) {
        //TODO--
        if (channel.equals("1")){
            return "demo-test2";
        }else {
            return "demo-test3";
        }
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        log.info("use loadbalance {}", NAME);
        //  invokers.size()>=2
        Object[] args = invocation.getArguments();
        if (args.length > 0) {
            // 渠道参数
            String channel = String.valueOf(args[1]);
            // channgel 路由过滤
            List<Invoker<T>> channelInvokers = fliterChannel(invokers, getRouterApplicationName(channel));
            if (channelInvokers.size() > 1) {
                return doDefault(invokers, url, invocation);
            }
            return channelInvokers.get(0);
        } else {
            return doDefault(invokers, url, invocation);
        }
    }

    private <T> Invoker<T> doDefault(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        LoadBalance defualtLoadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(CommonConstants.DEFAULT_LOADBALANCE);
        return defualtLoadBalance.select(invokers, url, invocation);
    }


    private <T> List<Invoker<T>> fliterChannel(List<Invoker<T>> invokers, String applicationName) {
        Iterator<Invoker<T>> iterator = invokers.iterator();
        List<Invoker<T>> newInvokers = new ArrayList<>();
        while (iterator.hasNext()) {
            Invoker<T> invoker = iterator.next();
            try {
                Field field = invoker.getClass().getDeclaredField(DUBBO_INVOKER_PROVIDER_URL_FIELD_KEY);
                field.setAccessible(true);
                URL providerUrl = (URL) field.get(invoker);
                String application = providerUrl.getParameter(DUBBO_PROVIDER_APPLICATION_KEY);
                if (applicationName.equals(application)) {
                    newInvokers.add(invoker);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("获取providerUrl错误：{}", invoker.getUrl());
            }
        }
        if (newInvokers.isEmpty()) {
            throw new RpcException("当前无可用服务:applicationName-" + applicationName);
        }
        return newInvokers;

    }

    static final String DEFAULT_APPLICATION_LOAD_BALANCE_VERSION = "default";
    static final String DUBBO_PROVIDER_APPLICATION_KEY = "application";
    static final String DUBBO_INVOKER_PROVIDER_URL_FIELD_KEY = "providerUrl";


}
