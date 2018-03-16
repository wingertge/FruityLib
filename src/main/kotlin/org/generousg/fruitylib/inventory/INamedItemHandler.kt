package org.generousg.fruitylib.inventory

import net.minecraftforge.items.IItemHandler


interface INamedItemHandler : IItemHandler {
    var name: String
}