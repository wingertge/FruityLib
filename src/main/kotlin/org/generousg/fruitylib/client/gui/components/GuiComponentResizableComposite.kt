package org.generousg.fruitylib.client.gui.components

import org.generousg.fruitylib.client.gui.BaseComposite


abstract class GuiComponentResizableComposite(x: Int, y: Int, width: Int, height: Int) : BaseComposite(x, y) {
    protected var _width = width
    protected var _height = height

    override val width: Int get() = _width
    override val height: Int get() = _height
}