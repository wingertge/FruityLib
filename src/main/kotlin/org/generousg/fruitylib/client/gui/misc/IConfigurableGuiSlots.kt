package org.generousg.fruitylib.client.gui.misc

import net.minecraft.util.EnumFacing
import org.generousg.fruitylib.util.bitmap.IWriteableBitMap
import org.generousg.fruitylib.util.events.ValueChangedEvent


interface IConfigurableGuiSlots<T : Enum<T>> {
    fun createAllowedDirectionsProvider(slot: T): (Unit)->Set<EnumFacing>
    fun createAllowedDirectionsReceiver(slot: T): IWriteableBitMap<EnumFacing>
    fun createAutoFlagProvider(slot: T): ()->ValueChangedEvent<Boolean>
    fun createAutoSlotReceiver(slot: T): (ValueChangedEvent<Boolean>)->Unit
}