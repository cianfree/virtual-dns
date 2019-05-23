package com.github.vdns.utils;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 14:23
 */
public class IpUtil {

    private IpUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 检查IP地址格式是否合法
     *
     * @param ip ip地址
     * @return true 表示地址合法，false 标识不合法
     */
    public static boolean isValidIp(String ip) {
        return IpUtil.isIPv4LiteralAddress(ip) || IpUtil.isIPv6LiteralAddress(ip);
    }

    public static byte[] textToNumericFormatV4(String ip) {
        if (ip.length() == 0) {
            return null;
        } else {
            byte[] b = new byte[4];
            String[] arr = ip.split("\\.", -1);

            try {
                long item;
                int i;
                switch (arr.length) {
                    case 1:
                        item = Long.parseLong(arr[0]);
                        if (item < 0L || item > 4294967295L) {
                            return null;
                        }

                        b[0] = (byte) ((int) (item >> 24 & 255L));
                        b[1] = (byte) ((int) ((item & 16777215L) >> 16 & 255L));
                        b[2] = (byte) ((int) ((item & 65535L) >> 8 & 255L));
                        b[3] = (byte) ((int) (item & 255L));
                        break;
                    case 2:
                        item = (long) Integer.parseInt(arr[0]);
                        if (item >= 0L && item <= 255L) {
                            b[0] = (byte) ((int) (item & 255L));
                            item = (long) Integer.parseInt(arr[1]);
                            if (item < 0L || item > 16777215L) {
                                return null;
                            }

                            b[1] = (byte) ((int) (item >> 16 & 255L));
                            b[2] = (byte) ((int) ((item & 65535L) >> 8 & 255L));
                            b[3] = (byte) ((int) (item & 255L));
                            break;
                        }

                        return null;
                    case 3:
                        for (i = 0; i < 2; ++i) {
                            item = (long) Integer.parseInt(arr[i]);
                            if (item < 0L || item > 255L) {
                                return null;
                            }

                            b[i] = (byte) ((int) (item & 255L));
                        }

                        item = (long) Integer.parseInt(arr[2]);
                        if (item < 0L || item > 65535L) {
                            return null;
                        }

                        b[2] = (byte) ((int) (item >> 8 & 255L));
                        b[3] = (byte) ((int) (item & 255L));
                        break;
                    case 4:
                        for (i = 0; i < 4; ++i) {
                            item = (long) Integer.parseInt(arr[i]);
                            if (item < 0L || item > 255L) {
                                return null;
                            }

                            b[i] = (byte) ((int) (item & 255L));
                        }

                        return b;
                    default:
                        return null;
                }

                return b;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static byte[] textToNumericFormatV6(String ip) {
        if (ip.length() < 2) {
            return null;
        } else {
            char[] carray = ip.toCharArray();
            byte[] b = new byte[16];
            int cLen = carray.length;
            int idx = ip.indexOf('%');
            if (idx == cLen - 1) {
                return null;
            } else {
                if (idx != -1) {
                    cLen = idx;
                }

                int var1 = -1;
                int var9 = 0;
                int var10 = 0;
                if (carray[var9] == ':') {
                    ++var9;
                    if (carray[var9] != ':') {
                        return null;
                    }
                }

                int var11 = var9;
                boolean var3 = false;
                int var4 = 0;

                while (true) {
                    int var12;
                    while (var9 < cLen) {
                        char var2 = carray[var9++];
                        var12 = Character.digit(var2, 16);
                        if (var12 != -1) {
                            var4 <<= 4;
                            var4 |= var12;
                            if (var4 > 65535) {
                                return null;
                            }

                            var3 = true;
                        } else {
                            if (var2 != ':') {
                                if (var2 == '.' && var10 + 4 <= 16) {
                                    String var13 = ip.substring(var11, cLen);
                                    int var14 = 0;

                                    for (int var15 = 0; (var15 = var13.indexOf(46, var15)) != -1; ++var15) {
                                        ++var14;
                                    }

                                    if (var14 != 3) {
                                        return null;
                                    }

                                    byte[] var16 = textToNumericFormatV4(var13);
                                    if (var16 == null) {
                                        return null;
                                    }

                                    for (int var17 = 0; var17 < 4; ++var17) {
                                        b[var10++] = var16[var17];
                                    }

                                    var3 = false;
                                    break;
                                }

                                return null;
                            }

                            var11 = var9;
                            if (!var3) {
                                if (var1 != -1) {
                                    return null;
                                }

                                var1 = var10;
                            } else {
                                if (var9 == cLen) {
                                    return null;
                                }

                                if (var10 + 2 > 16) {
                                    return null;
                                }

                                b[var10++] = (byte) (var4 >> 8 & 255);
                                b[var10++] = (byte) (var4 & 255);
                                var3 = false;
                                var4 = 0;
                            }
                        }
                    }

                    if (var3) {
                        if (var10 + 2 > 16) {
                            return null;
                        }

                        b[var10++] = (byte) (var4 >> 8 & 255);
                        b[var10++] = (byte) (var4 & 255);
                    }

                    if (var1 != -1) {
                        var12 = var10 - var1;
                        if (var10 == 16) {
                            return null;
                        }

                        for (var9 = 1; var9 <= var12; ++var9) {
                            b[16 - var9] = b[var1 + var12 - var9];
                            b[var1 + var12 - var9] = 0;
                        }

                        var10 = 16;
                    }

                    if (var10 != 16) {
                        return null;
                    }

                    byte[] var18 = convertFromIPv4MappedAddress(b);
                    if (var18 != null) {
                        return var18;
                    }

                    return b;
                }
            }
        }
    }

    public static boolean isIPv4LiteralAddress(String ip) {
        return textToNumericFormatV4(ip) != null;
    }

    public static boolean isIPv6LiteralAddress(String ip) {
        return textToNumericFormatV6(ip) != null;
    }

    public static byte[] convertFromIPv4MappedAddress(byte[] b) {
        if (isIPv4MappedAddress(b)) {
            byte[] ipBytes = new byte[4];
            System.arraycopy(b, 12, ipBytes, 0, 4);
            return ipBytes;
        } else {
            return null;
        }
    }

    private static boolean isIPv4MappedAddress(byte[] ip) {
        if (ip.length < 16) {
            return false;
        } else {
            return ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] == 0 && ip[4] == 0 && ip[5] == 0 && ip[6] == 0 && ip[7] == 0 && ip[8] == 0 && ip[9] == 0 && ip[10] == -1 && ip[11] == -1;
        }
    }
}
