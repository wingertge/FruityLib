package org.genguava.fruitylib.client.gui.components

import org.genguava.fruitylib.client.gui.BaseComponent


abstract class GuiComponentResizable(x: Int, y: Int, override var width: Int, override var height: Int) : BaseComponent(x, y)