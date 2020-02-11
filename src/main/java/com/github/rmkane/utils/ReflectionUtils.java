package com.github.rmkane.utils;

import java.lang.reflect.Field;

public class ReflectionUtils {
    public static final <E> void setField(E object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
