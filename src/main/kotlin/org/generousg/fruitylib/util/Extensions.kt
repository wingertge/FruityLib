@file:Suppress("PackageDirectoryMismatch")

package org.generousg.fruitylib

import mcjty.lib.tools.ItemStackTools
import net.minecraft.item.ItemStack


@Suppress("UNCHECKED_CAST")
inline fun <reified T> createFilledArray(size: Int, initializer: (Int)->T): Array<T> {
    val tempArray = arrayOfNulls<T>(size)
    (0..tempArray.size-1).forEach { tempArray[it] = initializer.invoke(it) }
    return tempArray as Array<T>
}

fun ItemStack.isNullOrEmpty(): Boolean {
    return ItemStackTools.isEmpty(this)
}

fun ItemStack.empty() = ItemStackTools.makeEmpty(this)
val emptyItemStack get() = ItemStackTools.getEmptyStack()