package com.veggieshop.unit.auth;


// ========== TestUtils helper for reflection field set ==========
import java.lang.reflect.Field;

class TestUtils {
    public static void setField(Object obj, String fieldName, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
