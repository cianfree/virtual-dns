package com.github.vdns;

import com.github.vdns.core.DnsConstants;
import com.github.vdns.core.NameServiceInterceptor;
import com.github.vdns.utils.ReflectUtil;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/7 10:16
 */
public class Jdk78InetAddressDnsHook extends AbstractJdkInetAddressDnsHook {


    /**
     * JDK 1.7 是使用多个 NameService 进行获取DNS解析
     */
    @Override
    protected Object lookupCurrentNameService() {
        try {
            return ReflectUtil.getFieldValue(InetAddress.class, DnsConstants.FIELD_NAME_SERVICES);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    protected Object createVirtualNameService() {
        return NameServiceUtil.createJdk678NameServiceProxy(new DnsNameService(customDnsResolver));
    }

    @Override
    protected Object createHookNameService(List<Object> nsList, NameServiceInterceptor interceptor) {

        Object wrapperNs = NameServiceUtil.createJdk678NameServiceProxy(new NameServiceListWrapper(interceptor, nsList));
        List<Object> list = new ArrayList<Object>(1);
        list.add(wrapperNs);

        return list;
    }

    @Override
    public void doHook(Object hookNameService) {
        try {
            ReflectUtil.setFieldValue(InetAddress.class, DnsConstants.FIELD_NAME_SERVICES, hookNameService);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void doUnhook(Object originNameService) {
        try {
            ReflectUtil.setFieldValue(InetAddress.class, DnsConstants.FIELD_NAME_SERVICES, originNameService);
        } catch (Exception ignored) {
        }
    }
}
