package org.generousg.fruitylib.container

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import org.generousg.fruitylib.util.InventoryUtils
import org.generousg.fruitylib.util.events.ButtonClickedEvent
import org.generousg.fruitylib.util.events.Event


abstract class ContainerBase<out T>(val playerInventory: IInventory, ownerInventory: IInventory, val owner: T) : Container() {
    val inventory = ownerInventory
    val inventorySize = inventory.sizeInventory
    val players: Set<EntityPlayer> get() {
        return playerList
    }

    val buttonClickedEvent = Event<ButtonClickedEvent>()

    protected class RestrictedSlot(inventory: IInventory, slot: Int, x: Int, y: Int) : Slot(inventory, slot, x, y) {
        private val inventoryIndex = slot
        override fun isItemValid(stack: ItemStack): Boolean = inventory.isItemValidForSlot(inventoryIndex, stack)
    }

    protected fun addInventoryGrid(xOffset: Int, yOffset: Int, width: Int) {
        val height = Math.ceil(inventorySize.toDouble() / width).toInt()
        var slotId = 0
        for(y in 0..height-1) {
            for (x in 0..width-1) {
                addSlotToContainer(RestrictedSlot(inventory, slotId, xOffset + x * 18, yOffset + y * 18))
                slotId++
            }
        }
    }

    protected fun addInventoryLine(xOffset: Int, yOffset: Int, start: Int, count: Int) = addInventoryLine(xOffset, yOffset, start, count, 0)
    protected fun addInventoryLine(xOffset: Int, yOffset: Int, start: Int, count: Int, margin: Int) {
        var slotId = start
        for(x in 0..count-1) {
            addSlotToContainer(RestrictedSlot(inventory, slotId, xOffset + x * (18 + margin), yOffset))
            slotId++
        }
    }

    protected fun addPlayerInventorySlots(offsetY: Int) = addPlayerInventorySlots(8, offsetY)
    protected fun addPlayerInventorySlots(offsetX: Int, offsetY: Int) {
        for(row in 0..2) for(column in 0..8)
            addSlotToContainer(Slot(playerInventory, column + row * 9 + 9, offsetX + column * 18, offsetY + row * 18))

        for(slot in 0..8)
            addSlotToContainer(Slot(playerInventory, slot, offsetX + slot * 18, offsetY + 58))
    }

    override fun canInteractWith(playerIn: EntityPlayer): Boolean = inventory.isUsableByPlayer(playerIn)

    protected fun mergeItemStackSafe(stackToMerge: ItemStack, start: Int, stop: Int, reverse: Boolean): Boolean {
        var inventoryChanged = false

        val delta = if(reverse) -1 else 1

        if(stackToMerge.isStackable) {
            var slotId = if(reverse) stop - 1 else start
            while (stackToMerge.count > 0 && ((!reverse && slotId < stop) || (reverse && slotId >= start))) {
                val slot = inventorySlots[slotId]

                if(canTransferItemsIn(slot)) {
                    val stackInSlot = slot.stack

                    if(InventoryUtils.tryMergeStacks(stackToMerge, stackInSlot)) {
                        slot.onSlotChanged()
                        inventoryChanged = true
                    }
                }

                slotId += delta
            }
        }

        if(stackToMerge.count > 0) {
            var slotId = if(reverse) stop - 1 else start

            while ((!reverse && slotId < stop) || (reverse && slotId >= start)) {
                val slot = inventorySlots[slotId]
                val stackInSlot = slot.stack

                if(stackInSlot.isEmpty && canTransferItemsIn(slot) && slot.isItemValid(stackToMerge)) {
                    slot.putStack(stackToMerge.copy())
                    slot.onSlotChanged()
                    stackToMerge.count = 0
                    return true
                }

                slotId += delta
            }
        }

        return inventoryChanged
    }

    override fun transferStackInSlot(playerIn: EntityPlayer, slotId: Int): ItemStack {
        val slot = inventorySlots[slotId]

        if(slot != null && canTransferItemOut(slot) &&slot.hasStack) {
            val itemToTransfer = slot.stack
            val copy = itemToTransfer.copy()
            if(slotId < inventorySize) {
                if(!mergeItemStackSafe(itemToTransfer, inventorySize, inventorySlots.size, true)) return ItemStack.EMPTY
            } else if(!mergeItemStackSafe(itemToTransfer, 0, inventorySize, false)) return ItemStack.EMPTY

            if(itemToTransfer.count == 0) slot.putStack(ItemStack.EMPTY)
            else slot.onSlotChanged()
            if(itemToTransfer.count != copy.count) return copy
        }
        return ItemStack.EMPTY
    }

    protected fun canTransferItemOut(slot: Slot): Boolean {
        if(slot is ICustomSlot) return slot.canTransferItemsOut()
        return true
    }

    protected fun canTransferItemsIn(slot: Slot): Boolean {
        if(slot is ICustomSlot) return slot.canTransferItemsIn()
        return true
    }

    override fun enchantItem(playerIn: EntityPlayer, id: Int): Boolean {
        buttonClickedEvent.fire(ButtonClickedEvent(playerIn, id))
        return false
    }

    override fun slotClick(slotId: Int, dragType: Int, clickTypeIn: ClickType, player: EntityPlayer): ItemStack {
        if(slotId >= 0 && slotId < inventorySlots.size) {
            val slot = getSlot(slotId)
            if(slot is ICustomSlot) return slot.onClick(player, clickTypeIn, dragType)
        }

        return super.slotClick(slotId, dragType, clickTypeIn, player)
    }

    override fun canDragIntoSlot(slot: Slot): Boolean {
        if(slot is ICustomSlot) return slot.canDrag()

        return super.canDragIntoSlot(slot)
    }
}