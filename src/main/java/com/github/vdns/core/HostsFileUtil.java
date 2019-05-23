package com.github.vdns.core;

import com.github.vdns.utils.CommonUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 14:59
 */
public class HostsFileUtil {

    private HostsFileUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 构造 domain to InetAddress[] Map
     *
     * @param domainIpMap 域名 to ip 列表 MAP
     * @return 返回 域名 to InetAddress 数组
     */
    public static Map<String, InetAddress[]> buildDnsInetAddressMap(Map<String, List<String>> domainIpMap) {

        if (null == domainIpMap || domainIpMap.isEmpty()) {
            return new HashMap<String, InetAddress[]>(0);
        }

        Map<String, InetAddress[]> map = new HashMap<String, InetAddress[]>();

        for (Map.Entry<String, List<String>> entry : domainIpMap.entrySet()) {

            List<InetAddress> inetAddressList = new ArrayList<InetAddress>();

            for (String ip : entry.getValue()) {
                try {
                    InetAddress inetAddress = InetAddress.getByAddress(ip, InetAddress.getByName(ip).getAddress());
                    inetAddressList.add(inetAddress);
                } catch (UnknownHostException ignored) {
                }
            }

            if (!inetAddressList.isEmpty()) {
                map.put(entry.getKey(), inetAddressList.toArray(new InetAddress[inetAddressList.size()]));
            }
        }
        return map;
    }

    /**
     * 加载 hosts 文件，同时返回 域名 to InetAddress[] 数组列表， 注意，按照文件列表顺序，最后一个的覆盖前一个
     *
     * @param hostsFiles hosts 文件
     * @return domain to InetAddress[]
     */
    public static Map<String, InetAddress[]> loadDnsMapByHostsFiles(List<String> hostsFiles) {
        return buildDnsInetAddressMap(loadDomainIpMapByHostsFiles(hostsFiles));
    }

    /**
     * 加载 hosts 文件， 注意，按照文件列表顺序，最后一个的覆盖前一个
     *
     * @param hostsFiles hosts 文件
     * @return 返回 域名 to ip 列表
     */
    public static Map<String, List<String>> loadDomainIpMapByHostsFiles(List<String> hostsFiles) {
        Map<String, List<String>> domainIpMap = new HashMap<String, List<String>>();
        if (hostsFiles == null || hostsFiles.isEmpty()) {
            return domainIpMap;
        }

        for (String hostsFile : hostsFiles) {
            CommonUtil.appendMapOverride(domainIpMap, loadDomainIpMapByHostsFile(hostsFile));
        }

        return domainIpMap;
    }

    /**
     * 加载 hosts 文件，返回 域名 to IP 列表
     *
     * @param hostsFile hosts 文件
     * @return 返回 域名 to ip 列表
     */
    public static Map<String, List<String>> loadDomainIpMapByHostsFile(String hostsFile) {
        return ipDomainToDomainIpMap(CommonUtil.readAsStringList(hostsFile));
    }

    /**
     * 构造 DNS MAP, 传入参数 IP DOMAIN
     *
     * @param ipDomainLines 配置行列表，每一行要求输入的文件格式是一行表是一个 IP 域名， 可以使用 # 注释
     * @return 返回 域名IP列表
     */
    public static Map<String, List<String>> ipDomainToDomainIpMap(List<String> ipDomainLines) {
        Map<String, List<String>> domainIpMap = new HashMap<String, List<String>>();
        if (null == ipDomainLines || ipDomainLines.isEmpty()) {
            return domainIpMap;
        }

        for (String line : ipDomainLines) {
            if (CommonUtil.isBlank(line)) {
                continue;
            }

            String value = line.trim();
            if (value.startsWith("#")) {
                // 过滤掉注释行
                continue;
            }

            // 去掉注释行
            value = removeComments(value);

            String[] array = value.split("\\s+");
            if (array.length < 2) {
                continue;
            }
            String ip = array[0];

            for (int i = 1; i < array.length; ++i) {
                String domain = array[i];
                if (CommonUtil.isBlank(domain)) {
                    continue;
                }
                List<String> set = domainIpMap.get(domain);
                if (set == null) {
                    set = new ArrayList<String>();
                    domainIpMap.put(domain, set);
                    set.add(ip);
                } else {
                    // 去重
                    if (!set.contains(ip)) {
                        set.add(ip);
                    }
                }
            }
        }

        return domainIpMap;
    }

    private static String removeComments(String hostsEntry) {
        String filteredEntry = hostsEntry;
        int hashIndex;

        if ((hashIndex = hostsEntry.indexOf('#')) != -1) {
            filteredEntry = hostsEntry.substring(0, hashIndex);
        }
        return filteredEntry;
    }

}
