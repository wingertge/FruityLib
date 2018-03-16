package org.genguava.fruitylib.sync.drops

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import org.genguava.fruitylib.api.IPlacerAwareTile
import org.genguava.fruitylib.tileentity.ICustomHarvestDrops
import org.genguava.fruitylib.tileentity.ICustomPickItem
import org.genguava.fruitylib.tileentity.SyncedTileEntity


abstract class DroppableTileEntity : SyncedTileEntity(), IPlacerAwareTile, ICustomHarvestDrops, ICustomPickItem {
    init {
        dropSerializer.addFields(this)
    }

    fun suppressNormalHarvestDrops(): Boolean {
        return true
    }

    protected val rawDrop: ItemStack
        get() = ItemStack(getBlockType())

    override fun drops(superDrops: List<ItemStack>, fortune: Int): MutableList<ItemStack> {
        val list = arrayListOf<ItemStack>()
        list.addAll(superDrops)
        list.add(dropStack)
        return list
    }

    override fun getPickBlock(player: EntityPlayer): ItemStack {
        return dropStack
    }

    protected val dropStack: ItemStack
        get() = dropSerializer.write(rawDrop)

    override fun onBlockPlacedBy(placer: EntityLivingBase, stack: ItemStack) {
        dropSerializer.read(stack, true)
    }
}
