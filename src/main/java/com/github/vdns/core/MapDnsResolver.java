package com.github.vdns.core;

import com.github.vdns.utils.CommonUtil;
import com.github.vdns.utils.IpUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/7 9:39
 */
public class MapDnsResolver implements DnsResolver {

    /**
     * 域名 to InetAddress 数组 MAP
     */
    private Map<String, InetAddress[]> domainIpsMap = new HashMap<String, InetAddress[]>();

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = domainIpsMap.get(host);

        if (null == addresses || addresses.length < 1) {
            throw new UnknownHostException(host);
        }

        return addresses;
    }


    /**
     * 设置自定义的DNS，每一行是一个域名ip对应关系： domain ip
     * 设置的含义是： 会把之前的自定义的DNS都清理，然后以本次输入的为主
     * 一个域名可以对应多个IP
     *
     * @param ipDomainLines 域名-IP 列表
     */
    public void set(List<String> ipDomainLines) {

        // 清理缓存，同时清理自定义DNS域名
        clear();

        this.domainIpsMap = HostsFileUtil.buildDnsInetAddressMap(HostsFileUtil.ipDomainToDomainIpMap(ipDomainLines));
    }

    /**
     * 添加自定义的DNS，每一行是一个域名ip对应关系： domain ip
     * 一个域名可以对应多个IP
     * 语义： 如果历史存在的，会直接覆盖
     *
     * @param ipDomainList IP to 域名 列表
     */
    public void add(List<String> ipDomainList) {
        add(HostsFileUtil.ipDomainToDomainIpMap(ipDomainList));
    }

    /**
     * 更新自定义的DNS，该域名的历史DNS会清理掉
     *
     * @param domain 域名
     * @param ips    ip 列表
     */
    public void add(String domain, String... ips) {
        add(domain, Arrays.asList(ips));
    }

    /**
     * 更新自定义的DNS，该域名的历史DNS会清理掉
     *
     * @param domain 域名
     * @param ips    ip 集合
     */
    public void add(String domain, Collection<String> ips) {
        if (CommonUtil.isBlank(domain)) {
            throw new RuntimeException("域名不能为空！");
        }

        Map<String, List<String>> domainIpMap = new HashMap<String, List<String>>(1);
        List<String> ipList = new ArrayList<String>();

        for (String ip : ips) {
            if (CommonUtil.isNotBlank(ip) || isValidIp(ip)) {
                ipList.add(ip);
            }
        }

        if (ipList.isEmpty()) {
            throw new RuntimeException("没有指定[" + domain + "] 对应的解析IP");
        }

        domainIpMap.put(domain, ipList);

        add(domainIpMap);
    }

    public void update(String domain, String... ips) {
        remove(domain);
        add(domain, ips);
    }

    /**
     * 删除自定义域名的 DNS
     *
     * @param domain 域名
     */
    public void remove(String domain) {
        remove(new ArrayList<String>(Collections.singletonList(domain)));
    }

    /**
     * 移除域名列表
     *
     * @param domains 要移除的域名列表
     */
    public void remove(Collection<String> domains) {

        if (null != domains && !domains.isEmpty()) {
            for (String domain : domains) {
                domainIpsMap.remove(domain);
            }
        }
    }

    /**
     * 清空所有的自定义 DNS
     */
    public void clear() {
        domainIpsMap = new HashMap<String, InetAddress[]>();
    }

    protected Map<String, List<String>> add(Map<String, List<String>> domainIpMap) {
        if (null != domainIpMap && !domainIpMap.isEmpty()) {

            Map<String, InetAddress[]> map = HostsFileUtil.buildDnsInetAddressMap(domainIpMap);

            if (!map.isEmpty()) {
                domainIpsMap.putAll(map);
            }

        }

        return domainIpMap;
    }

    /**
     * 检查IP地址格式是否合法
     *
     * @param ip ip地址
     * @return true 表示地址合法，false 标识不合法
     */
    protected boolean isValidIp(String ip) {
        return IpUtil.isValidIp(ip);
    }
}
