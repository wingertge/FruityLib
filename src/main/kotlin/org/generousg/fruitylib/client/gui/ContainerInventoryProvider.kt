package org.generousg.fruitylib.client.gui

import net.minecraft.inventory.IInventory
import org.generousg.fruitylib.container.ContainerBase
import org.generousg.fruitylib.inventory.IInventoryProvider


open class ContainerInventoryProvider<out T: IInventoryProvider>(playerInventory: IInventory, owner: T) : ContainerBase<T>(playerInventory, owner.inventory, owner) {
}