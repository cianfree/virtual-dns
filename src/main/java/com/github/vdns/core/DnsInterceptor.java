package com.github.vdns.core;

import java.net.InetAddress;

/**
 * DNS 解析拦截器
 *
 * @author Arvin
 */
public interface DnsInterceptor {

    /**
     * 解析之前，不允许抛出任何异常信息
     *
     * @param host 解析的host
     */
    void before(String host);

    /**
     * 解析结束，不允许抛出任何异常信息
     *
     * @param host      解析的host
     * @param addresses 解析结果
     * @param exception 异常信息，如果有的话
     */
    void after(String host, InetAddress[] addresses, Exception exception);
}
