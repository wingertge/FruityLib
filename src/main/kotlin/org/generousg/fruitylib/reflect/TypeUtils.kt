package org.generousg.fruitylib.reflect

import com.google.common.base.Preconditions
import com.google.common.reflect.TypeToken
import java.lang.reflect.Field
import java.lang.reflect.TypeVariable


class TypeUtils {
    companion object {
        val MAP_TOKEN = TypeToken.of(Map::class.java)
        val SET_TOKEN = TypeToken.of(Set::class.java)
        val COLLECTION_TOKEN = TypeToken.of(Collection::class.java)
        val LIST_TOKEN = TypeToken.of(List::class.java)
        val FUNCTION_TOKEN = TypeToken.of(Function::class.java)
        val LIST_VALUE_PARAM = List::class.java.typeParameters[0]
        val MAP_KEY_PARAM: TypeVariable<*>
        val MAP_VALUE_PARAM: TypeVariable<*>
        val SET_VALUE_PARAM = Set::class.java.typeParameters[0]
        val COLLECTION_VALUE_PARAM = Collection::class.java.typeParameters[0]
        val FUNCTION_A_PARAM: TypeVariable<*>
        val FUNCTION_B_PARAM: TypeVariable<*>

        init {
            val mapVars = Map::class.java.typeParameters
            MAP_KEY_PARAM = mapVars[0]
            MAP_VALUE_PARAM = mapVars[1]
            val functionVars = Function::class.java.typeParameters
            FUNCTION_A_PARAM = functionVars[0]
            FUNCTION_B_PARAM = functionVars[1]
        }

        fun isInstance(o: Any, mainCls: Class<*>, vararg extraCls: Class<*>) {
            require(mainCls.isInstance(o)) { "$o is not instance of $mainCls" }
            for(cls in extraCls) {
                require(cls.isInstance(o)) { "$o is not instance of $cls" }
            }
        }

        fun resolveFieldType(cls: Class<*>, field: Field): TypeToken<*>? {
            val fieldType = field.genericType
            val parentType = TypeToken.of(cls)
            return parentType.resolveType(fieldType)
        }

        fun getTypeParameter(intfClass: Class<*>, instanceClass: Class<*>, index: Int = 0): TypeToken<*> {
            val typeParameters = intfClass.typeParameters
            Preconditions.checkElementIndex(index, typeParameters.size, intfClass.toString() + " type parameter index")
            val arg = typeParameters[index]
            val type = TypeToken.of(instanceClass)
            Preconditions.checkArgument(type.rawType != Any::class.java, "Type %s is no fully parametrized", instanceClass)
            return type.resolveType(arg)
        }
    }
}