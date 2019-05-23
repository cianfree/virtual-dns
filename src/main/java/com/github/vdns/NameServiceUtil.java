package com.github.vdns;

import com.github.vdns.utils.ReflectUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/29 18:13
 */
public class NameServiceUtil {

    private NameServiceUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Object createJdk678NameServiceProxy(InvocationHandler invocationHandler) {
        return Proxy.newProxyInstance(
                InetAddress.class.getClassLoader(),
                new Class[]{
                        ReflectUtil.findClass("sun.net.spi.nameservice.NameService")
                },
                invocationHandler);
    }

    public static Object createJdk9PlusNameServiceProxy(InvocationHandler invocationHandler) {
        return Proxy.newProxyInstance(
                InetAddress.class.getClassLoader(),
                new Class[]{
                        ReflectUtil.findClass("java.net.InetAddress$NameService")
                },
                invocationHandler);
    }
}
