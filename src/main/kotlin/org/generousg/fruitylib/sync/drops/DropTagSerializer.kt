package org.generousg.fruitylib.sync.drops

import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.common.collect.Maps
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import org.generousg.fruitylib.reflect.FieldAccess
import org.generousg.fruitylib.sync.ISyncableObject
import org.generousg.fruitylib.util.ItemUtils


class DropTagSerializer {

    private val objects = Maps.newHashMap<String, ISyncableObject>()

    fun addObject(name: String, `object`: ISyncableObject) {
        val prev = objects.put(name, `object`)
        Preconditions.checkState(prev == null, "Duplicate on name %s, values = '%s' -> '%s'", name, prev, `object`)
    }

    fun addFields(target: Any?) {
        if (target == null) return
        var cls: Class<*> = target.javaClass
        while (cls != Object::class.java) {
            for (field in cls.declaredFields) {
                val marker = field.getAnnotation(StoreOnDrop::class.java) ?: continue

                Preconditions.checkArgument(ISyncableObject::class.java.isAssignableFrom(field.type),
                        "Field '%s' has SyncableDrop annotation, but isn't ISyncableObject", field)

                val wrappedField = FieldAccess.create<ISyncableObject>(field)
                val obj = wrappedField[target]
                Preconditions.checkNotNull(obj, "Field '%s' contains null", field)

                val suggestedName = field.name
                val name = if (Strings.isNullOrEmpty(marker.name)) suggestedName else marker.name

                addObject(name, obj)
            }
            cls = cls.superclass
        }
    }

    fun write(tag: NBTTagCompound) {
        for ((key, value) in objects)
            value.writeToNBT(tag, key)
    }

    fun read(tag: NBTTagCompound, skipEmpty: Boolean) {
        for ((key, value) in objects) {
            if (!skipEmpty || tag.hasKey(key)) value.readFromNBT(tag, key)
        }
    }

    fun write(stack: ItemStack): ItemStack {
        val tag = ItemUtils.getItemTag(stack)
        write(tag)
        return stack
    }

    fun read(stack: ItemStack, skipEmpty: Boolean) {
        val tag = ItemUtils.getItemTag(stack)
        read(tag, skipEmpty)
    }
}