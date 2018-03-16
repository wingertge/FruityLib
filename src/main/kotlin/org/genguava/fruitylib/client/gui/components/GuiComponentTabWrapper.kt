package org.genguava.fruitylib.client.gui.components

import net.minecraft.client.Minecraft
import org.genguava.fruitylib.client.gui.BaseComponent
import org.genguava.fruitylib.client.gui.BaseComposite




open class GuiComponentTabWrapper(x: Int, y: Int, protected val mainComponent: BaseComponent) : BaseComposite(x, y) {
    protected var activeTab: GuiComponentTab? = null

    init {
        addComponentWithTT(mainComponent)
    }

    fun addComponentWithTT(component: BaseComponent): BaseComposite {
        super.addComponent(component)
        if(component is GuiComponentTab) {
            component.x += mainComponent.width
            component.mouseDownEvent += {
                if(activeTab != component) {
                    activeTab?.setActive(false)
                    activeTab = component
                    activeTab!!.setActive(true)
                } else if(activeTab != null && activeTab!!.isOrigin(x, y)) {
                    component.setActive(false)
                    activeTab = null
                }
            }
        }
        return this
    }

    override fun renderComponentBackground(minecraft: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        realignTabsVertically()
    }

    private fun realignTabsVertically(): Int {
        var oY = mainComponent.y + 4

        for (component in components) {
            if (component is GuiComponentTab) {
                component.y = oY
                oY += component.height - 1
            }
        }
        return oY
    }

    fun onTabClicked(tab: GuiComponentTab) {
        if (tab !== activeTab) {
            if (activeTab != null) {
                activeTab!!.setActive(false)
            }
            tab.setActive(true)
            activeTab = tab
        } else {
            tab.setActive(false)
            activeTab = null
        }
    }

    override val width: Int get() {
            var maxTabWidth = 0
            components
                    .asSequence()
                    .filter { it.x + it.width > maxTabWidth }
                    .forEach { maxTabWidth = it.x + it.width }
            return mainComponent.width + maxTabWidth
        }
    override val height: Int
        get() {
            var maxTabHeight = 0
            for (component in components) {
                if (component.y + component.height > maxTabHeight) {
                    maxTabHeight = component.y + component.height
                }
            }
            return mainComponent.height + maxTabHeight
        }
}
