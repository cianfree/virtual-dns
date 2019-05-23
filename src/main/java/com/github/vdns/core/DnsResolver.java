package com.github.vdns.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/7 9:17
 */
public interface DnsResolver {

    /**
     * 返回给定主机地址的InetAddress
     *
     * @param host 主机地址
     * @return 无法解析到则抛出异常
     * @throws UnknownHostException 无法解析
     */
    InetAddress[] resolve(String host) throws UnknownHostException;

}
