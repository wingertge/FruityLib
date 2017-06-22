package org.generousg.fruitylib.client.gui

import net.minecraft.tileentity.TileEntity
import org.generousg.fruitylib.client.gui.components.GuiComponentCheckbox
import org.generousg.fruitylib.client.gui.components.GuiComponentLabel
import org.generousg.fruitylib.client.gui.components.GuiComponentTab
import org.generousg.fruitylib.client.gui.components.GuiComponentTabWrapper
import org.generousg.fruitylib.client.gui.logic.ValueCopyAction
import org.generousg.fruitylib.client.gui.misc.IConfigurableGuiSlots
import org.generousg.fruitylib.container.ContainerBase
import org.generousg.fruitylib.sync.ISyncMapProvider
import org.generousg.fruitylib.util.events.ValueChangedEvent


abstract class GuiConfigurableSlots<T, out C : ContainerBase<T>, E : Enum<E>>(container: C, width: Int, height: Int, name: String) : SyncedGuiContainer<C>(container, width, height, name) where T : TileEntity, T : ISyncMapProvider, T : IConfigurableGuiSlots<E> {

    protected abstract val slots: Iterable<E>

    protected abstract fun addCustomizations(root: BaseComposite)

    protected abstract fun createTab(slot: E): GuiComponentTab

    protected fun createCheckbox(slot: E): GuiComponentCheckbox {
        return GuiComponentCheckbox(10, 82, false, 0xFFFFFF)
    }

    protected abstract fun createLabel(slot: E): GuiComponentLabel

    override fun createRoot(): BaseComposite {
        val te = getContainer().owner

        val meta = te.getBlockMetadata()
        val block = te.getBlockType()

        val main = super.createRoot()
        addCustomizations(main)

        val tabs = GuiComponentTabWrapper(0, 0, main)

        for (slot in slots) {
            val tabTool = createTab(slot)
            tabs.addComponent(tabTool)

            val checkbox = createCheckbox(slot)

            setupCheckBox(checkbox, te.createAutoFlagProvider(slot), te.createAutoSlotReceiver(slot))

            tabTool.addComponent(checkbox)
            tabTool.addComponent(createLabel(slot))
        }

        return tabs
    }

    private fun setupCheckBox(checkbox: GuiComponentCheckbox, source: () -> ValueChangedEvent<Boolean>, updater: (ValueChangedEvent<Boolean>)->Unit) {
        checkbox.valueChangeEvent += updater

        addSyncUpdateListener(ValueCopyAction.create(checkbox, source, { checkbox.setValue(it.value) }))
    }
}