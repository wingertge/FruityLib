package org.generousg.fruitylib.util;

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.common.config.Property;

import java.util.Map;

public class ConfigTypes {
    public static final Map<Class<?>, Property.Type> CONFIG_TYPES = ImmutableMap.<Class<?>, Property.Type> builder()
            .put(Integer.class, Property.Type.INTEGER)
            .put(int.class, Property.Type.INTEGER)
            .put(Boolean.class, Property.Type.BOOLEAN)
            .put(boolean.class, Property.Type.BOOLEAN)
            .put(Byte.class, Property.Type.INTEGER)
            .put(byte.class, Property.Type.INTEGER)
            .put(Double.class, Property.Type.DOUBLE)
            .put(double.class, Property.Type.DOUBLE)
            .put(Float.class, Property.Type.DOUBLE)
            .put(float.class, Property.Type.DOUBLE)
            .put(Long.class, Property.Type.INTEGER)
            .put(long.class, Property.Type.INTEGER)
            .put(Short.class, Property.Type.INTEGER)
            .put(short.class, Property.Type.INTEGER)
            .put(String.class, Property.Type.STRING)
            .build();
}
