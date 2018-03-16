package org.generousg.fruitylib.config

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraftforge.fluids.Fluid


interface BlockInstances : InstanceContainer<Block>
interface ItemInstances : InstanceContainer<Item>
interface FluidInstances: InstanceContainer<Fluid>