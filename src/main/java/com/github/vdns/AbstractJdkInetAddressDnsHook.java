package com.github.vdns;

import com.github.vdns.core.DnsConstants;
import com.github.vdns.core.DnsResolver;
import com.github.vdns.core.NameServiceInterceptor;
import com.github.vdns.utils.ReflectUtil;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/29 19:43
 */
public abstract class AbstractJdkInetAddressDnsHook extends AbstractInetAddressDnsHook {

    /**
     * NameService对象
     */
    private final Object originNameService = lookupCurrentNameService();

    /**
     * 自定义的 NameService， 实现自己的 DNS 解析
     */
    private Object virtualNameService;

    /**
     * Hook NameService
     **/
    private Object hookNameService;

    private static AbstractJdkInetAddressDnsHook instance;

    public AbstractJdkInetAddressDnsHook() {
        if (instance != null) {
            throw new IllegalStateException("Can' not new!");
        }

        instance = this;
    }

    protected abstract Object createVirtualNameService();

    protected abstract Object createHookNameService(List<Object> nsList, NameServiceInterceptor interceptor);

    /**
     * 获取当前的 NameService
     *
     * @return 返回当前的 nameService
     */
    protected abstract Object lookupCurrentNameService();

    public static synchronized InetAddressDnsHook getInstance(Class<? extends AbstractJdkInetAddressDnsHook> hookClass, DnsResolver customDnsResolver, NameServiceInterceptor interceptor) {
        if (instance == null) {
            instance = (AbstractJdkInetAddressDnsHook) ReflectUtil.newInstanceByDefaultConstructor(hookClass);
        }
        instance.reset(customDnsResolver, interceptor);
        return instance;
    }

    public void reset(DnsResolver customDnsResolver, NameServiceInterceptor interceptor) {
        unhook();

        this.setCustomDnsResolver(customDnsResolver);
        this.virtualNameService = createVirtualNameService();
        this.hookNameService = createHookNameService(buildNameServiceList(), interceptor);
    }

    private List<Object> buildNameServiceList() {

        List<Object> nsList = new ArrayList<Object>(2);
        nsList.add(virtualNameService);

        if (originNameService instanceof Collection) {
            Collection collection = (Collection) originNameService;
            if (!collection.isEmpty()) {
                nsList.addAll(collection);
            }
        } else {
            nsList.add(originNameService);
        }

        return nsList;
    }

    @Override
    public final void hook() {
        if (!hookEnabled) {
            doHook(hookNameService);
        }
        setHookEnabled(true);
    }

    protected void doHook(Object hookNameService) {
        try {
            ReflectUtil.setFieldValue(InetAddress.class, DnsConstants.FIELD_NAME_SERVICE, hookNameService);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void unhook() {
        doUnhook(originNameService);
        setHookEnabled(false);
    }

    protected void doUnhook(Object originNameService) {
        try {
            ReflectUtil.setFieldValue(InetAddress.class, DnsConstants.FIELD_NAME_SERVICE, originNameService);
        } catch (Exception ignored) {
        }
    }
}
