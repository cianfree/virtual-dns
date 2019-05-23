package com.github.vdns.core;

import com.github.vdns.utils.CommonUtil;
import com.github.vdns.utils.SystemUtil;

import java.io.File;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 15:41
 */
public class SystemHostsFileDnsResolver extends HostsFilesDnsResolver {

    public static final String DEFAULT_WINDOWS_HOST_FILE_PATH = "C:" + File.separator + "windows" + File.separator + "System32" + File.separator + "drivers" + File.separator + "etc" + File.separator + "hosts";
    public static final String DEFAULT_LINUX_HOST_FILE_PATH = File.separator + "etc" + File.separator + "hosts";

    public SystemHostsFileDnsResolver(String systemHostsFile) {
        super(Collections.singletonList(readSystemHostsFile(systemHostsFile)), true, DEFAULT_RELOAD_INTERVAL_MILLIS);
    }

    public SystemHostsFileDnsResolver(String systemHostsFile, long reloadIntervalMillis) {
        super(Collections.singletonList(readSystemHostsFile(systemHostsFile)), true, reloadIntervalMillis);
    }

    public SystemHostsFileDnsResolver(long reloadIntervalMillis) {
        this(null, reloadIntervalMillis);
    }

    public SystemHostsFileDnsResolver() {
        this(null, DEFAULT_RELOAD_INTERVAL_MILLIS);
    }

    private static String readSystemHostsFile(String systemHostsFile) {
        if (CommonUtil.isFileExists(systemHostsFile)) {
            return systemHostsFile;
        }
        if (SystemUtil.IS_OS_WINDOWS) {
            return DEFAULT_WINDOWS_HOST_FILE_PATH;
        }
        return DEFAULT_LINUX_HOST_FILE_PATH;
    }

    @Override
    protected void logDomainIpsMap(Map<String, InetAddress[]> domainIpsMap) {
        // Nothing to do
    }
}
