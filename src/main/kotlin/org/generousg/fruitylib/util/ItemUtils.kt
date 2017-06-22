package org.generousg.fruitylib.util

import net.minecraft.item.Item.getByNameOrId
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import org.generousg.fruitylib.emptyItemStack


object ItemUtils {
    fun readStack(nbt: NBTTagCompound): ItemStack {
        val item = getByNameOrId(nbt.getString("id")) ?: return emptyItemStack

        val stackSize = nbt.getByte("Count")
        val itemDamage = nbt.getShort("Damage")

        val result = ItemStack(item, stackSize.toInt(), itemDamage.toInt())

        if (nbt.hasKey("tag", Constants.NBT.TAG_COMPOUND)) {
            result.tagCompound = nbt.getCompoundTag("tag")
        }
        return result
    }

    fun writeStack(stack: ItemStack): NBTTagCompound {
        val result = NBTTagCompound()
        stack.writeToNBT(result)

        // if possible, replace with string representation
        val item = stack.item
        if (item != null) {
            val id = item.registryName
            if (id != null) {
                result.setString("id", id.toString())
            }
        }

        return result
    }

    fun getItemTag(stack: ItemStack): NBTTagCompound {
        if (stack.tagCompound == null) stack.tagCompound = NBTTagCompound()
        return stack.tagCompound!!
    }
}