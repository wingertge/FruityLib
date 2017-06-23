package org.generousg.fruitylib.util

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.*
import net.minecraft.item.Item.getByNameOrId
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.IFuelHandler
import net.minecraftforge.fml.common.registry.GameRegistry
import org.generousg.fruitylib.emptyItemStack
import org.generousg.fruitylib.isNullOrEmpty


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

    fun fixVanillaFuelValues() {
        GameRegistry.registerFuelHandler(IFuelHandler { fuel ->
            if (fuel.isNullOrEmpty()) {
                0
            } else {
                val item = fuel.item

                if (item is ItemBlock && Block.getBlockFromItem(item) !== Blocks.AIR) {
                    val block = Block.getBlockFromItem(item)

                    if (block === Blocks.SAPLING) return@IFuelHandler 100
                    if (block === Blocks.COAL_BLOCK) return@IFuelHandler 14400
                }

                //Vanilla burn times
                if (item is ItemTool && item.toolMaterialName == "WOOD") return@IFuelHandler 200
                if (item is ItemSword && item.toolMaterialName == "WOOD") return@IFuelHandler 200
                if (item is ItemHoe && item.materialName == "WOOD") return@IFuelHandler 200
                if (item === Items.STICK) return@IFuelHandler 100
                if (item === Items.COAL) return@IFuelHandler 1600
                if (item === Items.LAVA_BUCKET) return@IFuelHandler 20000
                if (item === Items.BLAZE_ROD) return@IFuelHandler 2400
                0
            }
        })
    }
}