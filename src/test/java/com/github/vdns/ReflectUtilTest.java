package com.github.vdns;

import com.github.vdns.core.MapDnsResolver;
import com.github.vdns.utils.ReflectUtil;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/29 17:49
 */
public class ReflectUtilTest {

    @Test
    public void test() {

        String domain = "www.baidu.com";
        String ip = "127.0.0.1";

        final MapDnsResolver dnsResolver = new MapDnsResolver();
        dnsResolver.add(domain, ip);

        String nameServiceClass = "sun.net.spi.nameservice.NameService";
        Class<?> clazz = ReflectUtil.findClass(nameServiceClass);

        final Object instance = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                if ("lookupAllHostAddr".equals(method.getName())) {
                    return dnsResolver.resolve(String.valueOf(args[0]));
                }

                throw new UnknownHostException();
            }
        });


        InetAddress[] addresses = (InetAddress[]) ReflectUtil.invokeMethod(instance, "lookupAllHostAddr", new Class[]{String.class}, new Object[]{"www.baidu.com"});

        System.out.println(addresses[0].getHostAddress());


    }

    @Test
    public void testJdk9InetAddress() throws UnknownHostException, IllegalAccessException, NoSuchFieldException {

        Class<?> policyClass = ReflectUtil.findClass("sun.net.InetAddressCachePolicy");
        ReflectUtil.setFieldValue(policyClass, "cachePolicy", -1);
        System.out.println("Policy: " + ReflectUtil.getFieldValue(policyClass, "cachePolicy"));

        String domain = "www.baidu.com";
        String ip = "127.0.0.1";

        final MapDnsResolver dnsResolver = new MapDnsResolver();
        dnsResolver.add(domain, ip);

        InetAddress inetAddress = null;

//        InetAddress.getByName("www.baidu.com");
//
//        System.out.println(inetAddress.getHostAddress());


        Field field = ReflectUtil.findDeclaredField(InetAddress.class, "nameService");

        System.out.println(field);
        field.setAccessible(true);
        Object originInstance = field.get(InetAddress.class);
        System.out.println("OriginInstance: " + originInstance);

        Class<?> nsClass = ReflectUtil.findClass("java.net.InetAddress$NameService");
        System.out.println(nsClass);

        InvocationHandler handler = new DnsNameService(dnsResolver);
        Object instance = NameServiceUtil.createJdk9PlusNameServiceProxy(handler);

        System.out.println("====>  " + nsClass.isInstance(instance));
        System.out.println("Instance: " + instance);

        System.out.println("NameService1: " + field.get(InetAddress.class));
        field.set(InetAddress.class, instance);

        ReflectUtil.setFieldValue(InetAddress.class, "nameService", instance);

        System.out.println("NameService2: " + field.get(InetAddress.class));

        inetAddress = InetAddress.getByName("www.baidu.com");
        System.out.println("NameService3: " + field.get(InetAddress.class));

        System.out.println(inetAddress.getHostAddress());

        ReflectUtil.setFieldValue(InetAddress.class, "nameService", originInstance);

        // 清理 DNS 缓存
        Map map = (Map) ReflectUtil.getFieldValue(InetAddress.class, "cache");
        map.clear();

        inetAddress = InetAddress.getByName("www.baidu.com");
        System.out.println("NameService4: " + field.get(InetAddress.class));

        System.out.println(inetAddress.getHostAddress());

    }
}
