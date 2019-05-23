package com.github.vdns;

import com.github.vdns.core.DnsConstants;
import com.github.vdns.core.NameServiceInterceptor;
import com.github.vdns.utils.ReflectUtil;

import java.net.InetAddress;
import java.util.List;

/**
 * Jdk6 Hook
 *
 * @author Arvin
 * @version 1.0
 * @since 2018/12/7 9:12
 */
public class Jdk6InetAddressDnsHook extends AbstractJdkInetAddressDnsHook {

    @Override
    protected Object createVirtualNameService() {
        return NameServiceUtil.createJdk678NameServiceProxy(new DnsNameService(customDnsResolver));
    }

    @Override
    protected Object lookupCurrentNameService() {
        try {
            return ReflectUtil.getFieldValue(InetAddress.class, DnsConstants.FIELD_NAME_SERVICE);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    protected Object createHookNameService(List<Object> nsList, NameServiceInterceptor interceptor) {
        return NameServiceUtil.createJdk678NameServiceProxy(new NameServiceListWrapper(interceptor, nsList));
    }
}
