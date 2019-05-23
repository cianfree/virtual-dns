package com.github.vdns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 17:52
 */
public class UnSupportInetAddressDnsHook implements InetAddressDnsHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnSupportInetAddressDnsHook.class);

    @Override
    public void hook() {
        LOGGER.warn("当前Jdk版本不支持 InetAddressDnsHook.hook()");
    }

    @Override
    public void unhook() {
        LOGGER.warn("当前Jdk版本不支持 InetAddressDnsHook.unhook()");
    }


}
