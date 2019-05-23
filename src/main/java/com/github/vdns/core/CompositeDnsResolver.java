package com.github.vdns.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 15:46
 */
public class CompositeDnsResolver implements DnsResolver {

    private final List<DnsResolver> resolverList;

    public CompositeDnsResolver(List<DnsResolver> resolverList) {
        this.resolverList = resolverList;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        if (null == resolverList || resolverList.isEmpty()) {
            throw new UnknownHostException(host);
        }
        for (DnsResolver resolver : resolverList) {
            try {
                return resolver.resolve(host);
            } catch (Exception ignored) {
            }
        }
        throw new UnknownHostException(host);
    }
}
