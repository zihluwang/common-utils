package cn.vorbote.common.utils;

import cn.vorbote.time.DateTime;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This util class can be used for automatic conversion
 * between dictionaries/maps and objects.
 *
 * @author vorbote thills@vorbote.cn
 */
@Slf4j
public final class MapUtil {

    private MapUtil() {
    }

    private static final String DT_SHORT = "short";
    private static final String DT_INT = "int";
    private static final String DT_LONG = "long";
    private static final String DT_BYTE = "byte";
    private static final String DT_CHAR = "char";
    private static final String DT_BOOL = "boolean";
    private static final String DT_FLOAT = "float";
    private static final String DT_DOUBLE = "double";

    private static final String CLASS_STRING = "class java.lang.String";
    private static final String CLASS_BIG_DECIMAL = "class java.math.BigDecimal";
    private static final String CLASS_DATE = "class java.util.Date";
    private static final String CLASS_INTEGER = "class java.lang.Integer";
    private static final String CLASS_SHORT = "class java.lang.Short";
    private static final String CLASS_LONG = "class java.lang.Long";
    private static final String CLASS_BYTE = "class java.lang.Byte";
    private static final String CLASS_CHAR = "class java.lang.Character";
    private static final String CLASS_FLOAT = "class java.lang.Float";
    private static final String CLASS_DOUBLE = "class java.lang.Double";
    private static final String CLASS_BOOL = "class java.lang.Boolean";
    private static final String CLASS_DATE_TIME = "class cn.vorbote.time.DateTime";

    /**
     * Dynamically convert object to dictionary/map
     *
     * @param obj Objects that need to be converted to Map
     * @return Converted HashMap
     * @throws Exception Any Exception could be generated while converting
     */
    public static Map<String, Object> SetMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();

        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (field.get(obj) != null) {
                map.put(field.getName(), field.get(obj));
            }
        }

        return map;
    }

    /**
     * Convert Map to Object. {@code Date} instance and {@code DateTime}
     * instance need timestamp data.
     *
     * @param map Map data to be convert
     * @param obj Target object should be explicitly given to a special
     *            instantiated object
     * @see #SetObject(Map, Class)
     */
    @Deprecated
    public static void SetObject(Map<String, Object> map, Object obj)
            throws Exception {
        obj = SetObject(map, obj.getClass());
    }

    /**
     * Set a object by reflection, it will create a new instance by this method,
     * you don't have to pass an instance to it anymore.
     *
     * @param map          The map which put the data.
     * @param requiredType The type you want.
     * @param <T>          The type you want.
     * @return An instance of the class you provided, the instance contains all
     * the field values with the same key in the Map.
     * @throws NoSuchMethodException     This method will get a no-param
     *                                   constructor, if the class you provided
     *                                   doesn't contain this class, this
     *                                   exception will be threw out.
     * @throws IllegalAccessException    This method will use the no-param
     *                                   constructor to create a new instance,
     *                                   if your constructor is modified by
     *                                   modifier {@code private},
     *                                   {@code protected} or the default
     *                                   modifier.
     * @throws InvocationTargetException If the underlying constructor throws
     *                                   an exception, then it will be throws
     *                                   out by this method.
     * @throws InstantiationException    If you provided an abstract class, it
     *                                   will throws out this exception.
     * @see Class#getConstructor(Class...)
     * @see java.lang.reflect.Constructor#newInstance(Object...)
     */
    public static <T> T SetObject(Map<String, Object> map, Class<T> requiredType)
            throws Exception {
        T bean = requiredType.getConstructor().newInstance();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                try {
                    String entryValue = entry.getValue().toString();
                    // 根据字段名获取字段
                    Field field = requiredType.getDeclaredField(entry.getKey());

                    // 判断类中对应字段的Class
                    switch (field.getGenericType().toString()) {
                        case DT_SHORT:
                        case CLASS_SHORT:
                            entry.setValue(Short.parseShort(entryValue));
                            break;
                        case DT_INT:
                        case CLASS_INTEGER:
                            entry.setValue(Integer.parseInt(entryValue));
                            break;
                        case DT_LONG:
                        case CLASS_LONG:
                            entry.setValue(Long.parseLong(entryValue));
                            break;
                        case DT_FLOAT:
                        case CLASS_FLOAT:
                            entry.setValue(Float.parseFloat(entryValue));
                            break;
                        case DT_DOUBLE:
                        case CLASS_DOUBLE:
                            entry.setValue(Double.parseDouble(entryValue));
                            break;
                        case DT_CHAR:
                        case CLASS_CHAR:
                            entry.setValue(entryValue.charAt(0));
                            break;
                        case DT_BYTE:
                        case CLASS_BYTE:
                            entry.setValue(Byte.parseByte(entryValue));
                            break;
                        case DT_BOOL:
                        case CLASS_BOOL:
                            entry.setValue(Boolean.parseBoolean(entryValue));
                            break;
                        case CLASS_STRING:
                            entry.setValue(entryValue);
                            break;
                        case CLASS_BIG_DECIMAL:
                            entry.setValue(BigDecimal.valueOf(Double.parseDouble(entryValue)));
                            break;
                        case CLASS_DATE_TIME:
                            entry.setValue(new DateTime(Long.parseLong(entryValue)));
                            break;
                        case CLASS_DATE:
                            entry.setValue(new Date(Long.parseLong(entryValue)));
                            break;

                        default:
                            log.error("Unsupported Type or Class: {}", field.getGenericType());
                            // System.err.println("Unsupported Type or Class");
                    }

                    // 设置值
                    SetFieldValue(bean, entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    log.error("Map to Object failed.");
                }
            }
        }
        return bean;
    }


    /**
     * Get the specified field value, equivalent to {@code obj.getFieldName}. Before using
     * this method, please make sure that you have a <strong>getter</strong> for that field
     * you need.
     *
     * @param fieldName Field name
     * @param obj       object
     * @return A string of the object corresponding to the object value.
     * @throws Exception Exceptions will be caused due to other methods, please see upriver
     *                   methods for details of these Exceptions' info.
     * @see Field#setAccessible(boolean)
     */
    public static String GetFieldValue(Object obj, String fieldName) throws Exception {
        try {
            String methodName = getMethodName("get", fieldName);
            Method method = getDeclaredMethod(obj, methodName);
            if (method != null) {
                method.setAccessible(true);
                return defaultObject(method.invoke(obj));
            }
        } catch (Exception ex) {
            log.error("Failed getting object: " + ex.getLocalizedMessage());
            throw new Exception("Failed getting object.");
        }
        return "";
    }

    /**
     * Get the specified field value, equivalent to {@code obj.getFieldName}. Before using
     * this method, please make sure that you have a <strong>getter</strong> for that field
     * you need.
     *
     * @param fieldName Field name
     * @param obj       object
     * @return A string of the object corresponding to the object value
     * @throws Exception Exceptions will be caused due to other methods, please see upriver
     *                   methods for details of these Exceptions' info.
     * @see Field#get(Object)
     * @see Field#setAccessible(boolean)
     */
    public static <T> T GetFieldValue(Object obj, String fieldName, Class<T> requiredClass) throws Exception {
        try {
            var objClass = obj.getClass();
            var field = objClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            var value = field.get(obj);
            return Cast(value, requiredClass);
        } catch (Exception ex) {
            log.error("Failed getting object: " + ex.getLocalizedMessage());
            throw new Exception("Failed getting object.");
        }
    }

    /**
     * Get the specified field value, equivalent to {@code obj.setFieldName}. Before using
     * this method, please make sure that you have a <strong>setter</strong> for that field
     * you need.
     *
     * @param fieldName  Field name
     * @param obj        The object will be set
     * @param fieldValue Field Value
     * @throws Exception Any Exception could be generated while converting
     */
    public static void SetFieldValue(Object obj, String fieldName, Object fieldValue) throws Exception {
        try {
            String methodName = getMethodName("set", fieldName);
            Method method = getDeclaredMethod(obj, methodName, fieldValue.getClass());
            if (method != null) {
                method.setAccessible(true);
                method.invoke(obj, fieldValue);
            }
        } catch (Exception ex) {
            // log.error("设置对象值失败:{}", ex.getCause().toString());
            throw new Exception("Failed setting object.");
        }
    }

    /**
     * Get the name of method
     *
     * @param prefix    Prefix of method
     * @param fieldName Name of the field
     */
    private static String getMethodName(String prefix, String fieldName) {
        return prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }


    /**
     * Find methods according to objects, method names and parameter types.
     *
     * @param object         The object
     * @param methodName     The name of the method
     * @param parameterTypes The type of parameter
     */
    private static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        Method method;
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                return method;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Get the default value for the passed object.
     *
     * @param obj The object to be show
     * @return The string of this obj
     */
    private static String defaultObject(Object obj) {
        if (obj == null) {
            return "";
        } else {
            return String.valueOf(obj);
        }
    }

    /**
     * Cast the value to the required type. The value had better to be a
     * instance of {@code Object} or the super class of the required
     * class.
     *
     * @param value        The value.
     * @param requiredType The required type.
     * @param <T>          The required type.
     * @return The value in the form of the instance of the required type.
     */
    public static <T> T Cast(Object value, Class<T> requiredType) {
        if (requiredType.isInstance(value)) {
            return requiredType.cast(value);
        }
        return null;
    }
}
