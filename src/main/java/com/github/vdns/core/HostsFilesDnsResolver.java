package com.github.vdns.core;

import com.github.vdns.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 14:42
 */
public class HostsFilesDnsResolver implements DnsResolver, Runnable {

    public static final long DEFAULT_RELOAD_INTERVAL_MILLIS = 5000;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * host 文件路径
     **/
    private final List<String> filePaths;

    /**
     * 域名 to InetAddress 数组 MAP
     */
    private Map<String, InetAddress[]> domainIpsMap = new HashMap<String, InetAddress[]>();

    /**
     * 重新加载的刷新时间间隔，单位是毫秒，默认是 5000 毫秒
     **/
    private long reloadIntervalMillis;

    /**
     * 允许使用 classpath 前缀
     *
     * @param filePaths            文件路径
     * @param ignoredUnknownFile   是否忽略不存在的文件
     * @param reloadIntervalMillis 重新加载的时间间隔，单位是毫秒， -1 表示不需要重新加载
     */
    public HostsFilesDnsResolver(List<String> filePaths, boolean ignoredUnknownFile, long reloadIntervalMillis) {
        this.reloadIntervalMillis = reloadIntervalMillis;
        this.filePaths = validateAndFilterUnknownFiles(filePaths, ignoredUnknownFile);
        reloadDnsMap(true);

        // 注册文件变更处理事件
        scheduleHostsFileReload();
    }

    /**
     * 允许使用 classpath 前缀
     *
     * @param filePaths          文件路径
     * @param ignoredUnknownFile 是否忽略不存在的文件
     */
    public HostsFilesDnsResolver(List<String> filePaths, boolean ignoredUnknownFile) {
        this(filePaths, ignoredUnknownFile, DEFAULT_RELOAD_INTERVAL_MILLIS);
    }

    public HostsFilesDnsResolver(List<String> filePaths) {
        this(filePaths, true);
    }

    public HostsFilesDnsResolver(String filePath) {
        this(Collections.singletonList(filePath), true);
    }

    public HostsFilesDnsResolver(String filePath, boolean ignoredUnknownFile) {
        this(Collections.singletonList(filePath), ignoredUnknownFile);
    }

    public HostsFilesDnsResolver(String filePath, boolean ignoredUnknownFile, long reloadIntervalMillis) {
        this(Collections.singletonList(filePath), ignoredUnknownFile, reloadIntervalMillis);
    }

    /**
     * 调度 hosts 文件变更重新加载
     */
    private void scheduleHostsFileReload() {
        if (reloadIntervalMillis > 0) {
            LOGGER.info("Schedule Hosts file load by reloadIntervalMillis: {}", reloadIntervalMillis);
            Executors.newSingleThreadScheduledExecutor()
                    .scheduleAtFixedRate(this, this.reloadIntervalMillis, reloadIntervalMillis, TimeUnit.MILLISECONDS);
        } else {
            LOGGER.info("No Schedule Hosts file load by reloadIntervalMillis: {}", reloadIntervalMillis);
        }
    }

    @Override
    public void run() {
        reloadDnsMap(false);
    }

    public long getReloadIntervalMillis() {
        return reloadIntervalMillis;
    }

    private void reloadDnsMap(boolean log) {
        this.domainIpsMap = HostsFileUtil.loadDnsMapByHostsFiles(this.filePaths);

        if (log) {
            logDomainIpsMap(this.domainIpsMap);
        }
    }

    protected void logDomainIpsMap(Map<String, InetAddress[]> domainIpsMap) {

        StringBuilder builder = new StringBuilder();
        if (domainIpsMap != null && !domainIpsMap.isEmpty()) {
            for (Map.Entry<String, InetAddress[]> entry : domainIpsMap.entrySet()) {
                builder.append(entry.getKey()).append(" --> ").append(inetAddressToIpString(entry.getValue())).append("\n");
            }
        }

        LOGGER.info("本次刷新DNS解析结果如下： \n{}", builder);
    }

    private String inetAddressToIpString(InetAddress[] addresses) {
        StringBuilder builder = new StringBuilder("[");
        if (addresses != null && addresses.length > 0) {
            for (InetAddress address : addresses) {
                builder.append(address.getHostAddress()).append(",");
            }
        }
        if (builder.length() > 1) {
            builder.setLength(builder.length() - 1);
        }
        builder.append("]");
        return builder.toString();

    }

    private List<String> validateAndFilterUnknownFiles(List<String> filePaths, boolean ignoredUnknownFile) {
        List<String> files = new ArrayList<String>();
        if (filePaths != null && !filePaths.isEmpty()) {
            for (String filePath : filePaths) {
                boolean existsFile = CommonUtil.isFileExists(filePath);
                if (!ignoredUnknownFile && !existsFile) {
                    throw new IllegalArgumentException("Host file [" + filePath + "] not exists!");
                }
                if (existsFile) {
                    files.add(filePath);
                }
            }
        }

        return files;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = domainIpsMap.get(host);

        if (null == addresses || addresses.length < 1) {
            throw new UnknownHostException(host);
        }

        return addresses;
    }
}
