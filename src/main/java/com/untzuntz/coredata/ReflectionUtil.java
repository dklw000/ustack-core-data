package com.untzuntz.coredata;

import com.untzuntz.coredata.anno.DBTableMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by arhimmel on 4/19/17.
 */
public class ReflectionUtil {

    /**
     * Get all fields of class and its super classes
     * @param clazz
     * @return
     */
    public static List<Field> getFields(Class<?> clazz) {
        return getFields(null, clazz);
    }

    /**
     * Returns all fields from the object (and super classes if configured to do so)
     * @param tbl
     * @param obj
     * @return
     */
    public static List<Field> getFields(DBTableMap tbl, Object obj)
    {
        return getFields(tbl, obj.getClass());
    }

    /**
     * Returns all fields from the object (and super classes if configured to do so)
     * @param tbl
     * @param cls
     * @return
     */
    public static List<Field> getFields(DBTableMap tbl, Class cls)
    {
        List<Field> fields = new ArrayList<Field>(Arrays.asList(cls.getDeclaredFields()));

        if (tbl == null)
            tbl = (DBTableMap)cls.getAnnotation(DBTableMap.class);

        if (tbl != null && tbl.includeParent())
        {
            Class superCls = cls.getSuperclass();
            while (superCls != null)
            {
                fields.addAll(Arrays.asList(superCls.getDeclaredFields()));
                superCls = superCls.getSuperclass();
            }
        }

        return fields;
    }
}
