# virtual-dns
Hook InetAddress to change dns

# 基本原理
JVM 会使用自己的DNS解析，使用的是 InetAddress， 分析这个类的源码，你可以定义自己的 NameService 并且随时更新NameService的调用链（jdk1.6+）

通过把自己的NameService设置为第一优先级实现自己的DNS解析

# 支持的 JDK
支持 JDK 1.6， 1.7， 1.8， 1.9， 1.10， 1.11

# 使用说明
直接使用 VirtualDnsUtil 工具类即可，有几个重载的hook方法. 更多示例请参考 <code>com.github.vdns.VirtualDnsUtilTest</code>