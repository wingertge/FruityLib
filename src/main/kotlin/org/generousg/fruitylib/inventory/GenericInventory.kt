package org.generousg.fruitylib.inventory

import mcjty.lib.tools.ItemStackTools
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import org.generousg.fruitylib.createFilledArray
import org.generousg.fruitylib.empty
import org.generousg.fruitylib.emptyItemStack
import org.generousg.fruitylib.isNullOrEmpty
import org.generousg.fruitylib.util.ItemUtils
import org.generousg.fruitylib.util.events.Event
import org.generousg.fruitylib.util.events.InventoryChangedEvent

// TODO: Rewrite for IItemHandler
open class GenericInventory(protected val inventoryName: String, protected val isInvNameLocalized: Boolean, protected var size: Int) : IInventory {
    override fun getField(id: Int): Int = 0
    override fun clear() = clearAndSetSlotCount(size)
    override fun isEmpty(): Boolean = !inventoryContents.any { it != emptyItemStack }
    override fun getDisplayName(): ITextComponent = TextComponentString(name)

    override fun setField(id: Int, value: Int) { }

    override fun removeStackFromSlot(index: Int): ItemStack {
        val itemStack = inventoryContents[index]

        if(itemStack.isNullOrEmpty()) return emptyItemStack
        else {
            inventoryContents[index] = emptyItemStack
            return itemStack
        }
    }

    override fun getFieldCount(): Int = 0

    val TAG_SLOT = "Slot"
    val TAG_ITEMS = "Items"
    val TAG_SIZE = "size"

    val inventoryChangedEvent = Event<InventoryChangedEvent>()
    protected val inventoryContents = createFilledArray(size, { ItemStackTools.getEmptyStack() })

    override fun decrStackSize(par1: Int, par2: Int): ItemStack {
        if (!this.inventoryContents[par1].isNullOrEmpty()) {
            val itemstack: ItemStack

            if (this.inventoryContents[par1].stackSize <= par2) {
                itemstack = this.inventoryContents[par1]
                this.inventoryContents[par1].empty()
                onInventoryChanged(par1)
                return itemstack
            }
            itemstack = this.inventoryContents[par1].splitStack(par2)
            if (this.inventoryContents[par1].stackSize == 0) {
                this.inventoryContents[par1].empty()
            }

            onInventoryChanged(par1)
            return itemstack
        }
        return emptyItemStack
    }

    override fun getInventoryStackLimit(): Int {
        return 64
    }

    override fun getSizeInventory(): Int {
        return size
    }

    override fun getStackInSlot(i: Int): ItemStack {
        return this.inventoryContents[i]
    }

    fun getStackInSlot(i: Enum<*>): ItemStack {
        return getStackInSlot(i.ordinal)
    }

    fun getStackInSlotOnClosing(i: Int): ItemStack {
        if (i >= this.inventoryContents.size) {
            return emptyItemStack
        }
        if (!this.inventoryContents[i].isNullOrEmpty()) {
            val itemstack = this.inventoryContents[i]
            this.inventoryContents[i].empty()
            return itemstack
        }
        return emptyItemStack
    }

    fun isItem(slot: Int, item: Item): Boolean {
        return !inventoryContents[slot].isNullOrEmpty() && inventoryContents[slot].item === item
    }

    override fun isItemValidForSlot(i: Int, stack: ItemStack): Boolean {
        return true
    }

    override fun isUsableByPlayer(entityplayer: EntityPlayer): Boolean {
        return true
    }

    fun onInventoryChanged(slotNumber: Int) {
        inventoryChangedEvent.fire(InventoryChangedEvent(this, slotNumber))
    }

    fun clearAndSetSlotCount(amount: Int) {
        this.size = amount
        inventoryContents.forEach { it.empty() }
        onInventoryChanged(0)
    }

    fun readFromNBT(tag: NBTTagCompound) {
        if (tag.hasKey(TAG_SIZE)) {
            this.size = tag.getInteger(TAG_SIZE)
        }
        val nbttaglist = tag.getTagList(TAG_ITEMS, 10)
        inventoryContents.forEach { it.empty() }
        for (i in 0..nbttaglist.tagCount() - 1) {
            val stacktag = nbttaglist.getCompoundTagAt(i)
            val j = stacktag.getByte(TAG_SLOT)
            if (j >= 0 && j < inventoryContents.size) {
                inventoryContents[j.toInt()] = ItemUtils.readStack(stacktag)
            }
        }
    }

    override fun setInventorySlotContents(i: Int, itemstack: ItemStack) {
        this.inventoryContents[i] = itemstack

        if (!itemstack.isNullOrEmpty() && itemstack.stackSize > inventoryStackLimit) {
            itemstack.stackSize = inventoryStackLimit
        }

        onInventoryChanged(i)
    }

    fun writeToNBT(tag: NBTTagCompound) {
        tag.setInteger(TAG_SIZE, sizeInventory)
        val nbttaglist = NBTTagList()
        for (i in inventoryContents.indices) {
            if (!inventoryContents[i].isNullOrEmpty()) {
                val stacktag = ItemUtils.writeStack(inventoryContents[i])
                stacktag.setByte(TAG_SLOT, i.toByte())
                nbttaglist.appendTag(stacktag)
            }
        }
        tag.setTag(TAG_ITEMS, nbttaglist)
    }

    /**
     * This bastard never even gets called, so don't rely on it.
     */
    override fun markDirty() {
        onInventoryChanged(0)
    }

    fun copyFrom(inventory: IInventory) {
        for (i in 0..inventory.sizeInventory - 1) {
            if (i < sizeInventory) {
                val stack = inventory.getStackInSlot(i)
                if (!stack.isNullOrEmpty()) {
                    setInventorySlotContents(i, stack.copy())
                } else {
                    setInventorySlotContents(i, emptyItemStack)
                }
            }
        }
    }

    fun contents(): List<ItemStack> {
        return inventoryContents.toList()
    }

    override fun hasCustomName(): Boolean = this.isInvNameLocalized

    override fun openInventory(player: EntityPlayer) {}

    override fun closeInventory(player: EntityPlayer) {}

    override fun getName(): String = inventoryName

}