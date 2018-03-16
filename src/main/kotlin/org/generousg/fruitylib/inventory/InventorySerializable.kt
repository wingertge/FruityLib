package org.generousg.fruitylib.inventory

import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.NonNullList
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import org.generousg.fruitylib.emptyItemStack
import org.generousg.fruitylib.util.events.Event
import org.generousg.fruitylib.util.events.InventoryChangedEvent


open class InventorySerializable(title: String, customTitle: Boolean, size: Int) : InventoryBasic(title, customTitle, size), INBTSerializable<NBTTagCompound> {
    init {
        addInventoryChangeListener { inventoryChangedEvent.fire(InventoryChangedEvent(this)) }
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        setSize(if (nbt.hasKey("Size", Constants.NBT.TAG_INT)) nbt.getInteger("Size") else inventoryContents.size)
        val tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND)
        for (i in 0..tagList.tagCount() - 1) {
            val itemTags = tagList.getCompoundTagAt(i)
            val slot = itemTags.getInteger("Slot")

            if (slot >= 0 && slot < inventoryContents.size) {
                inventoryContents[slot] = ItemStack(itemTags)
            }
        }
    }

    private fun setSize(size: Int) {
        if(size == sizeInventory) return
        slotsCount = size
        val newContents = NonNullList.withSize(size, emptyItemStack)
        newContents.addAll(inventoryContents)
        inventoryContents = newContents
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbtTagList = NBTTagList()
        for (i in inventoryContents.indices) {
            if (!inventoryContents[i].isEmpty) {
                val itemTag = NBTTagCompound()
                itemTag.setInteger("Slot", i)
                inventoryContents[i].writeToNBT(itemTag)
                nbtTagList.appendTag(itemTag)
            }
        }
        val nbt = NBTTagCompound()
        nbt.setTag("Items", nbtTagList)
        nbt.setInteger("Size", inventoryContents.size)
        return nbt
    }

    val inventoryChangedEvent = Event<InventoryChangedEvent>()
}