package com.github.vdns;

import com.github.vdns.core.DnsResolver;
import com.github.vdns.core.JdkUtil;
import com.github.vdns.core.MapDnsResolver;
import com.github.vdns.core.NameServiceInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 17:25
 */
public class VirtualDnsUtil {

    private VirtualDnsUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualDnsUtil.class);

    private static InetAddressDnsHook hook;

    private static MapDnsResolver mapDnsResolver;

    public static synchronized MapDnsResolver hook(String... hostsLine) {
        return hook(null, hostsLine);
    }

    public static synchronized MapDnsResolver hook(NameServiceInterceptor interceptor, String... hostsLine) {
        if (hook == null || mapDnsResolver == null) {
            mapDnsResolver = new MapDnsResolver();
            if (hostsLine != null && hostsLine.length > 0) {
                mapDnsResolver.set(Arrays.asList(hostsLine));
                hook(mapDnsResolver, interceptor);
            }
        } else {
            hook.hook();
        }
        return mapDnsResolver;
    }

    public static void hook(DnsResolver dnsResolver) {
        hook(dnsResolver, null);
    }

    public static synchronized void hook(DnsResolver dnsResolver, NameServiceInterceptor interceptor) {

        unhook();

        hook = createHook(dnsResolver, interceptor);
        hook.hook();
    }

    public static synchronized void unhook() {
        if (hook != null) {
            hook.unhook();
        }
        hook = null;
    }

    private static InetAddressDnsHook createHook(DnsResolver dnsResolver, NameServiceInterceptor interceptor) {
        if (null == dnsResolver) throw new RuntimeException("DnsResolver should not be null!");

        String hookClass = UnSupportInetAddressDnsHook.class.getName();
        try {
            if (JdkUtil.isIsJava6()) {
                hookClass = Jdk6InetAddressDnsHook.class.getName();
                return AbstractJdkInetAddressDnsHook.getInstance(Jdk6InetAddressDnsHook.class, dnsResolver, interceptor);
            }
            if (JdkUtil.isIsJava7() || JdkUtil.isIsJava8()) {
                hookClass = Jdk78InetAddressDnsHook.class.getName();
                return AbstractJdkInetAddressDnsHook.getInstance(Jdk78InetAddressDnsHook.class, dnsResolver, interceptor);
            }

            if (JdkUtil.isIsJava9() || JdkUtil.isIsJava10() || JdkUtil.isIsJava11()) {
                hookClass = Jdk9to11InetAddressDnsHook.class.getName();
                return AbstractJdkInetAddressDnsHook.getInstance(Jdk9to11InetAddressDnsHook.class, dnsResolver, interceptor);
            }
        } finally {
            LOGGER.info("Hook DNS By class {}", hookClass);
        }

        LOGGER.warn("当前JDK版本不支持 InetAddressDnsHook");
        return new UnSupportInetAddressDnsHook();
    }
}
