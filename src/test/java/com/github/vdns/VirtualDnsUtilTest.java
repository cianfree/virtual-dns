package com.github.vdns;

import com.github.vdns.core.MapDnsResolver;
import com.github.vdns.core.NameServiceInterceptor;
import com.github.vdns.utils.ReflectUtil;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

//import sun.net.InetAddressCachePolicy;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 18:20
 */
public class VirtualDnsUtilTest {

    private void disabledDnsCache() {

        Class<?> policyClass = ReflectUtil.findClass("sun.net.InetAddressCachePolicy");
        try {
            ReflectUtil.setFieldValue(policyClass, "cachePolicy", 0);
        } catch (Exception ignored) {
        }

        try {
            ReflectUtil.setFieldValue(policyClass, "set", true);

        } catch (Exception ignored) {
        }
    }

    private void cleanDnsCache() {
        // 清理 DNS 缓存
        try {
            Map map = (Map) ReflectUtil.getFieldValue(InetAddress.class, "cache");
            map.clear();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void hook() throws UnknownHostException {

        // 先去掉缓存
        disabledDnsCache();

        String domain = "www.baidu.com";
        String ip = "127.0.0.1";

        MapDnsResolver resolver = new MapDnsResolver();
        resolver.add(domain, ip);

        VirtualDnsUtil.hook(resolver);

        InetAddress inetAddress = InetAddress.getByName(domain);
        cleanDnsCache();

        assertEquals(ip, inetAddress.getHostAddress());

        VirtualDnsUtil.unhook();
        inetAddress = InetAddress.getByName(domain);
        cleanDnsCache();

        assertNotEquals(ip, inetAddress.getHostAddress());

        MapDnsResolver mapDnsResolver = VirtualDnsUtil.hook("127.0.0.1 www.baidu.com");
        inetAddress = InetAddress.getByName(domain);
        cleanDnsCache();
        assertEquals(ip, inetAddress.getHostAddress());

        ip = "127.0.0.2";
        mapDnsResolver.update("www.baidu.com", ip);
        inetAddress = InetAddress.getByName(domain);
        cleanDnsCache();
        assertEquals(ip, inetAddress.getHostAddress());

    }

    @Test
    public void hookWithInterceptor() throws UnknownHostException {

        final String domain = "www.baidu.com";
        final String ip = "127.0.0.1";

        final AtomicBoolean hadIntercepted = new AtomicBoolean(false);

        VirtualDnsUtil.hook(new NameServiceInterceptor() {
            @Override
            public void before(String host) {
                hadIntercepted.set(true);
            }

            @Override
            public void after(String host, InetAddress[] addresses, Exception exception) {
            }
        }, ip + " " + domain);

        InetAddress inetAddress = InetAddress.getByName(domain);
        assertTrue(hadIntercepted.get());

        assertEquals(inetAddress.getHostAddress(), ip);

    }

}