package org.generousg.fruitylib.inventory

import net.minecraft.inventory.IInventory


interface IInventoryProvider {
    val inventory: IInventory
}