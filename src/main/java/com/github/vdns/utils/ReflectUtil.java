package com.github.vdns.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 反射工具类
 *
 * @author Arvin
 */
public class ReflectUtil {

    private ReflectUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtil.class);

    public static Class<?> findClass(String className) {
        return findClass(className, null);
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        try {
            if (classLoader == null) {
                return Class.forName(className);
            } else {
                return classLoader.loadClass(className);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method findDeclaredMethod(Object target, String methodName, Class<?>... paramTypes) {
        Class<?> targetClass = getObjectClass(target);

        Exception exception = null;

        for (Class<?> superClass = targetClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(e.getMessage(), e);
                }
                exception = e;
            }
        }
        if (null != exception) {
            throw new RuntimeException(exception);
        }
        throw new RuntimeException("No such method exception[" + methodName + "] for class: " + targetClass.getSimpleName());

    }

    public static Object invokeMethod(Object target, String methodName) {
        return invokeMethod(target, findDeclaredMethod(target, methodName));
    }

    public static Object invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        return invokeMethod(target, findDeclaredMethod(target, methodName, paramTypes), args);
    }

    public static Object invokeMethod(Object target, Method method, Object... args) {
        boolean accessible = method.isAccessible();
        try {
            if (!accessible) {
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                method.setAccessible(false);
            }
        }
    }

    public static Object newInstanceByDefaultConstructor(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("NewInstanceError:" + e.getMessage(), e);
        }
    }

    /**
     * 获取字段的值
     *
     * @param obj   源对象
     * @param field 属性
     * @return 属性值
     */
    public static Object getFieldValue(Object obj, Field field) {
        if (null == obj || null == field) {
            return null;
        }
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 获取字段的值
     *
     * @param obj       源对象
     * @param fieldName 属性名称
     * @return 属性值
     */
    @SuppressWarnings({"unchecked"})
    public static Object getFieldValue(Object obj, String fieldName) {
        if (null == obj || CommonUtil.isBlank(fieldName)) {
            return null;
        }
        Class<?> objClass = getObjectClass(obj);
        Field field = findDeclaredField(objClass, fieldName);
        if (null == field) {
            return null;
        }
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 设置属性的值
     *
     * @param obj       对象
     * @param fieldName 属性名称
     * @param value     值
     */
    @SuppressWarnings({"unchecked"})
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        Class<?> objClass = getObjectClass(obj);
        Field field = ReflectUtil.findDeclaredField(objClass, fieldName);
        if (null == field)
            throw new RuntimeException("[" + objClass.getName() + "]'s filed[" + fieldName + "] not exists!");
        setFieldValue(obj, field, value);
    }

    /**
     * 设置属性的值
     *
     * @param obj   对象
     * @param field 属性
     * @param value 值
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        boolean oldAccessible = field.isAccessible();
        if (!oldAccessible) {
            field.setAccessible(true);
        }
        boolean hadChangeFinal = false;
        if (Modifier.isFinal(field.getModifiers())) {
            updateFieldAsNotFinal(field);
            hadChangeFinal = true;
        }
        try {
            Class<?> type = field.getType();
            if (type.equals(Integer.class) || type.equals(int.class)) {
                field.set(obj, ((Number) value).intValue());
            } else if (type.equals(Long.class) || type.equals(long.class)) {
                field.set(obj, ((Number) value).longValue());
            } else {
                field.set(obj, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(oldAccessible);
            if (hadChangeFinal) {
                updateFieldAsFinal(field);
            }
        }
    }

    public static void updateFieldAsNotFinal(Field field) {
        try {
            Field modifiersField = ReflectUtil.findDeclaredField(Field.class, "modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateFieldAsFinal(Field field) {
        try {
            Field modifiersField = ReflectUtil.findDeclaredField(Field.class, "modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getObjectClass(Object obj) {
        if (obj instanceof Class) {
            return (Class<?>) obj;
        }
        return obj.getClass();
    }

    /**
     * 搜索字段，包含私有的，从当前类开始搜索，如果当前类没有，继续往父类中查找，直到找到或到Object为止
     *
     * @param clazz     类对象
     * @param fieldName 属性名称
     * @return 属性对象
     */
    public static Field findDeclaredField(Class<?> clazz, String fieldName) {
        if (null == clazz || CommonUtil.isBlank(fieldName)) {
            return null;
        }
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        }
        return null;
    }
}
