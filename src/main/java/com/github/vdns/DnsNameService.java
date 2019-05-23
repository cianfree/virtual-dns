package com.github.vdns;

import com.github.vdns.core.DnsResolver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 14:35
 */
public class DnsNameService implements InvocationHandler {

    private static final String METHOD_LOOKUP_ALL_HOST_ADDR = "lookupAllHostAddr";

    private final DnsResolver dnsResolver;

    public DnsNameService(DnsResolver dnsResolver) {
        this.dnsResolver = dnsResolver;
    }

    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        InetAddress[] addresses = dnsResolver.resolve(host);
        if (null == addresses || addresses.length < 1) {
            throw new UnknownHostException(host);
        }
        return addresses;
    }

    public String getHostByAddr(byte[] bytes) throws UnknownHostException {
        // 不进行解析，让别的NameService进行解析
        throw new UnknownHostException();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (METHOD_LOOKUP_ALL_HOST_ADDR.equals(method.getName())) {
            return lookupAllHostAddr(String.valueOf(args[0]));
        }

        return method.invoke(this, args);
    }
}
