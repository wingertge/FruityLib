package org.generousg.fruitylib.serializable

import org.generousg.fruitylib.util.io.IStreamSerializer
import java.lang.reflect.Type


interface ISerializerProvider {
    fun getSerializer(cls: Class<*>): IStreamSerializer<*>?
}

interface IGenericSerializerProvider {
    fun getSerializer(type: Type): IStreamSerializer<*>?
}