@file:Suppress("unused")

package org.genguava.fruitylib.util.events

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.util.EnumFacing
import org.genguava.fruitylib.client.gui.BaseComponent


class KeyPressedEvent(val component: BaseComponent, val typedKey: Char, val keyCode: Int)
class MouseClickEvent(val component: BaseComponent, val mouseX: Int, val mouseY: Int, val mouseButton: Int)
class MouseDragEvent(val component: BaseComponent, val mouseX: Int, val mouseY: Int, val mouseButton: Int, val elapsedTime: Long)
class ButtonClickedEvent(val player: EntityPlayer, val buttonId: Int)
class SideToggledEvent(val side: EnumFacing, val currentState: Boolean)

class InventoryChangedEvent(val inventory: IInventory)

class ValueChangedEvent<out T>(val value: T, val prev: T)