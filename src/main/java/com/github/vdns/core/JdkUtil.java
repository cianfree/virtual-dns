package com.github.vdns.core;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/7 10:31
 */
public class JdkUtil {

    private JdkUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static boolean isJava6;
    private static boolean isJava7;
    private static boolean isJava8;
    private static boolean isJava9;
    private static boolean isJava10;
    private static boolean isJava11;

    static {
        String jdkVersion = System.getProperty("java.version");
        isJava6 = jdkVersion.startsWith("1.6");
        isJava7 = jdkVersion.startsWith("1.7");
        isJava8 = jdkVersion.startsWith("1.8");
        isJava9 = jdkVersion.startsWith("9");
        isJava10 = jdkVersion.startsWith("10");
        isJava11 = jdkVersion.startsWith("11");
    }


    public static boolean isIsJava6() {
        return isJava6;
    }

    public static boolean isIsJava7() {
        return isJava7;
    }

    public static boolean isIsJava8() {
        return isJava8;
    }

    public static boolean isIsJava9() {
        return isJava9;
    }

    public static boolean isIsJava10() {
        return isJava10;
    }

    public static boolean isIsJava11() {
        return isJava11;
    }
}
