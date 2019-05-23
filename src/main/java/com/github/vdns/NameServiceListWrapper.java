package com.github.vdns;

import com.github.vdns.core.NameServiceInterceptor;
import com.github.vdns.utils.ReflectUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Arvin
 */
public class NameServiceListWrapper implements InvocationHandler {

    /**
     * 拦截器
     */
    private final NameServiceInterceptor interceptor;

    /**
     * Name Server 列表最大下标
     */
    private final int maxIndex;

    /**
     * 域名解析服务列表
     */
    private final List<Object> nameServiceList;

    /**
     * 最后一个生效的 NameService
     */
    private final Object lastNameService;

    public NameServiceListWrapper(NameServiceInterceptor interceptor, List<Object> nameServiceList) {

        if (null == nameServiceList || nameServiceList.isEmpty())
            throw new RuntimeException("NameService list should not be null");

        this.interceptor = interceptor;
        this.nameServiceList = nameServiceList;

        maxIndex = this.nameServiceList.size() - 1;
        lastNameService = this.nameServiceList.get(maxIndex);

    }

    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        if (null != interceptor) {
            try {
                interceptor.before(host);
            } catch (Exception ignored) {
            }
        }


        Exception exception = null;
        InetAddress[] addresses = null;
        try {
            for (int i = 0; i < maxIndex; ++i) {

                Object nameService = nameServiceList.get(i);
                try {
                    addresses = (InetAddress[]) ReflectUtil.invokeMethod(nameService, "lookupAllHostAddr", new Class[]{String.class}, new Object[]{host});
                    return addresses;
                } catch (Exception ignored) {
                }
            }

            addresses = (InetAddress[]) ReflectUtil.invokeMethod(lastNameService, "lookupAllHostAddr", new Class[]{String.class}, new Object[]{host});
            return addresses;

        } catch (Exception e) {
            if (e instanceof UnknownHostException) {
                exception = e;
                throw (UnknownHostException) e;
            }
            UnknownHostException tempE = new UnknownHostException(host);
            exception = tempE;
            throw tempE;
        } finally {
            if (interceptor != null) {
                try {
                    interceptor.after(host, addresses, exception);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public String getHostByAddr(byte[] bytes) {
        for (int i = 0; i < maxIndex; ++i) {

            Object nameService = nameServiceList.get(i);
            try {
                return (String) ReflectUtil.invokeMethod(nameService, "getHostByAddr", new Class[]{byte[].class}, new Object[]{bytes});
            } catch (Exception ignored) {
            }
        }

        return (String) ReflectUtil.invokeMethod(lastNameService, "getHostByAddr", new Class[]{byte[].class}, new Object[]{bytes});
    }

    private static final String METHOD_LOOKUP_ALL_HOST_ADDR = "lookupAllHostAddr";

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (METHOD_LOOKUP_ALL_HOST_ADDR.equals(method.getName())) {
            return lookupAllHostAddr(String.valueOf(args[0]));
        }

        return getHostByAddr((byte[]) args[0]);
    }
}
