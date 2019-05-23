package com.github.vdns;

import com.github.vdns.core.DnsResolver;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/7 9:21
 */
public abstract class AbstractInetAddressDnsHook implements InetAddressDnsHook {

    protected DnsResolver customDnsResolver;

    protected boolean hookEnabled;

    public DnsResolver getCustomDnsResolver() {
        return customDnsResolver;
    }

    public void setCustomDnsResolver(DnsResolver customDnsResolver) {
        this.customDnsResolver = customDnsResolver;
    }

    public boolean isHookEnabled() {
        return hookEnabled;
    }

    public void setHookEnabled(boolean hookEnabled) {
        this.hookEnabled = hookEnabled;
    }
}
