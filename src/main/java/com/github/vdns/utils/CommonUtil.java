package com.github.vdns.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通用工具类，杂七杂八的就放在这里
 *
 * @author Arvin
 */
public class CommonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    public static final String DEFAULT_ENCODING = "UTF-8";

    interface LineHandler {

        void handle(String line);
    }

    private CommonUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 追加并覆盖 map
     *
     * @param sourceMap 源map
     * @param subMap    子map，要追加的
     * @param <K>       key类型
     * @param <V>       值类型
     * @return sourceMap
     */
    public static <K, V> Map<K, V> appendMapOverride(Map<K, V> sourceMap, Map<K, V> subMap) {
        if (sourceMap == null) {
            return subMap;
        }

        if (isNotEmptyMap(subMap)) {
            sourceMap.putAll(subMap);
            return sourceMap;
        }
        return sourceMap;
    }

    public static <K, V> boolean isNotEmptyMap(Map<K, V> map) {
        return null != map && !map.isEmpty();
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFileExists(String filePath) {
        if (isBlank(filePath)) {
            return false;
        }
        if (isClasspathFileExists(filePath)) {
            return true;
        }
        try {
            File file = new File(filePath);
            return file.exists();
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isClasspath(String filePath) {
        return isNotBlank(filePath) && filePath.matches("^(?i)classpath\\*?:.*");
    }

    public static boolean isClasspathFileExists(String filePath) {
        try {
            if (isClasspath(filePath)) {
                URL url = getClasspathUrl(filePath);
                return null != url;
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static String removeClasspathPrefix(String filePath) {
        return filePath.replaceFirst("^(?i)\\s*(classpath\\*?:)(.*)", "$2");
    }

    public static URL getClasspathUrl(String classpath) {
        if (isBlank(classpath)) {
            return null;
        }
        classpath = removeClasspathPrefix(classpath);

        if (classpath.startsWith("/")) {
            return CommonUtil.class.getResource(classpath);
        }
        URL url = CommonUtil.class.getResource(classpath);
        if (null == url) {
            return CommonUtil.class.getResource("/" + classpath);
        }
        return url;
    }

    public static List<String> readAsStringList(File file) {
        return readAsStringList(file, DEFAULT_ENCODING);
    }

    public static List<String> readAsStringList(String filePath) {
        return readAsStringList(filePath, DEFAULT_ENCODING);
    }

    public static List<String> readAsStringList(String filePath, String encoding) {
        if (isClasspathFileExists(filePath)) {
            return readClasspathFileAsStringList(filePath, encoding);
        }
        if (isFileExists(filePath) && !isClasspath(filePath)) {
            return readAsStringList(new File(filePath), encoding);
        }
        return new ArrayList<String>();
    }

    public static List<String> readAsStringList(File file, String encoding) {
        final List<String> lines = new ArrayList<String>();
        lineIterate(file, encoding, new LineHandler() {
            @Override
            public void handle(String line) {
                lines.add(line);
            }
        });
        return lines;
    }

    public static void lineIterate(File file, String encoding, LineHandler lineHandler) {

        if (null == lineHandler) throw new RuntimeException("Line handler should not be null");
        if (!(file != null && file.exists() && file.isFile()))
            throw new RuntimeException("File should be exists or a file");

        if (isBlank(encoding)) {
            encoding = DEFAULT_ENCODING;
        }

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            String line;
            while ((line = reader.readLine()) != null) {
                lineHandler.handle(line);
            }
        } catch (Exception ignored) {
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
    }

    public static void lineIterate(String filePath, LineHandler lineHandler) {
        if (isClasspath(filePath)) {
            lineIterateClasspathFile(filePath, DEFAULT_ENCODING, lineHandler);
        } else {
            lineIterate(new File(filePath), DEFAULT_ENCODING, lineHandler);
        }
    }

    public static void lineIterate(String filePath, String encoding, LineHandler lineHandler) {
        if (isClasspath(filePath)) {
            lineIterateClasspathFile(filePath, encoding, lineHandler);
        } else {
            lineIterate(new File(filePath), encoding, lineHandler);
        }
    }

    public static void lineIterate(File file, LineHandler lineHandler) {
        lineIterate(file, DEFAULT_ENCODING, lineHandler);
    }

    public static List<String> readClasspathFileAsStringList(String classpathFile, String encoding) {
        final List<String> lines = new ArrayList<String>();
        lineIterateClasspathFile(classpathFile, encoding, new LineHandler() {
            @Override
            public void handle(String line) {
                lines.add(line);
            }
        });
        return lines;
    }


    public static void lineIterateClasspathFile(String classpathFile, String encoding, LineHandler lineHandler) {
        if (null == lineHandler) throw new RuntimeException("Line handler should not be null");
        if (!isClasspathFileExists(classpathFile))
            throw new RuntimeException("Class path file[" + classpathFile + "] not exists");

        if (isBlank(encoding)) {
            encoding = DEFAULT_ENCODING;
        }

        URL url = getClasspathUrl(classpathFile);
        if (null != url) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));

                String line;
                while ((line = reader.readLine()) != null) {
                    lineHandler.handle(line);
                }
            } catch (Exception ignored) {
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // ignored
                    }
                }
            }
        }
    }

}
