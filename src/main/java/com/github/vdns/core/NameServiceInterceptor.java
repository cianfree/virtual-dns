package com.github.vdns.core;

import java.net.InetAddress;

/**
 * DNS 域名实际解析前后拦截器
 *
 * @author Arvin
 */
public interface NameServiceInterceptor {

    /**
     * 解析之前
     *
     * @param host 待解析host
     */
    void before(final String host);

    /**
     * 解析之后的结果
     *
     * @param host      本次解析的主机地址
     * @param addresses 解析结果
     * @param exception 解析发生的异常信息
     */
    void after(final String host, final InetAddress[] addresses, Exception exception);
}
