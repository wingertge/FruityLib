package org.genguava.fruitylib.client.gui.components

import net.minecraft.client.Minecraft
import org.genguava.fruitylib.client.gui.BaseComponent
import org.genguava.fruitylib.util.events.Event
import org.genguava.fruitylib.util.events.ValueChangedEvent
import org.lwjgl.opengl.GL11


class GuiComponentCheckbox(x: Int, y: Int, initialValue: Boolean, protected var color: Int) : BaseComponent(x, y) {
    var value: Boolean = false
        private set

    val valueChangeEvent = Event<ValueChangedEvent<Boolean>>()

    init {
        this.value = initialValue
        mouseDownEvent += {
            value = !value
            valueChangeEvent.fire(ValueChangedEvent(value, !value))
        }
    }

    override fun render(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        GL11.glColor4f(1f, 1f, 1f, 1f)
        bindComponentSheet()
        drawTexturedModalRect(offsetX + x, offsetY + y, if (value) 16 else 0, 62, 8, 8)
    }

    override fun renderOverlay(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {}

    private var _height = 8
    override val height: Int
        get() = _height

    private var _width = 8
    override val width: Int
        get() = _width

    fun setValue(value: Boolean?) {
        this.value = value!!
    }
}