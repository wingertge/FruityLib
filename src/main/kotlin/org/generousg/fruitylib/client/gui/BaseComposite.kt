package org.generousg.fruitylib.client.gui

import com.google.common.collect.ImmutableList
import net.minecraft.client.Minecraft
import org.generousg.fruitylib.util.events.KeyPressedEvent
import org.generousg.fruitylib.util.events.MouseClickEvent
import org.generousg.fruitylib.util.events.MouseDragEvent


@Suppress("UNUSED_PARAMETER")
abstract class BaseComposite(x: Int, y: Int) : BaseComponent(x, y) {
    protected val components = arrayListOf<BaseComponent>()
    protected val tickingComponents = arrayListOf<BaseComponent>()

    companion object {
        private fun isComponentEnabled(component: BaseComponent) = component.enabled
        private fun isComponentCapturingMouse(component: BaseComponent, mouseX: Int, mouseY: Int) = isComponentEnabled(component) && component.isMouseOver(mouseX, mouseY)
    }

    init {
        keyEvent += { event: KeyPressedEvent -> if(areChildrenActive()) {
            components.filter { isComponentEnabled(it) }.forEach { it.keyEvent.fire(event) }
        }}
        mouseDownEvent += { event: MouseClickEvent -> if(areChildrenActive()) {
            components.filter { isComponentEnabled(it) && it.isMouseOver(event.mouseX - it.x, event.mouseY - it.y) }.forEach { it.mouseDownEvent.fire(event) }
        }}
        mouseUpEvent += { event: MouseClickEvent -> if(areChildrenActive()) {
            components.filter { isComponentEnabled(it) }.forEach { it.mouseUpEvent.fire(event) }
        }}
        mouseDragEvent += { event: MouseDragEvent -> if(areChildrenActive()) {
            components.filter { isComponentEnabled(it) }.forEach { it.mouseDragEvent.fire(event) }
        }}
    }

    open fun areChildrenActive(): Boolean = true

    open fun addComponent(component: BaseComponent): BaseComposite {
        components.add(component)
        if(component.isTicking()) tickingComponents.add(component)
        return this
    }

    protected abstract fun renderComponentBackground(minecraft: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int)
    protected open fun renderComponentForeground(minecraft: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {}
    protected fun renderComponentOverlay(minecraft: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {}

    override fun render(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        renderComponentBackground(mc, offsetX, offsetY, mouseX, mouseY)

        if(!areChildrenActive()) return

        val ownX = offsetX + x
        val ownY = offsetY + y
        val relMouseX = mouseX - x
        val relMouseY = mouseY - y

        components.filter { isComponentEnabled(it) }.forEach { it.render(mc, ownX, ownY, relMouseX, relMouseY) }
        renderComponentForeground(mc, offsetX, offsetY, mouseX, mouseY)
    }

    override fun renderOverlay(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        renderComponentOverlay(mc, offsetX, offsetY, mouseX, mouseY)

        if(!areChildrenActive()) return

        val ownX = offsetX + x
        val ownY = offsetY + y
        val relMouseX = mouseX - x
        val relMouseY = mouseY - y

        components.filter { isComponentEnabled(it) }.forEach { it.renderOverlay(mc, ownX, ownY, relMouseX, relMouseY) }
    }

    private fun selectComponentsCapturingMouse(mouseX: Int, mouseY: Int): List<BaseComponent> {
        val result = ImmutableList.builder<BaseComponent>()

        components.filter { isComponentCapturingMouse(it, mouseX, mouseY) }.forEach { result.add(it) }
        return result.build()
    }

    override fun isTicking() = !tickingComponents.isEmpty()
    override fun tick() {
        super.tick()
        components.forEach { it.tick() }
    }
}