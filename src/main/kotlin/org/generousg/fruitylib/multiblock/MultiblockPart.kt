package org.generousg.fruitylib.multiblock

import net.minecraft.util.IStringSerializable


enum class MultiblockPart: IStringSerializable {
    SINGLE,
    END,
    LINE_EDGE,
    CORNER,
    EDGE,
    CENTER,
    CENTER_END,
    CENTER_CORNER,
    CENTER_EDGE,
    TOP_END,
    TOP_CORNER,
    TOP_EDGE,
    TOP_CENTER,
    HIDDEN;

    override fun getName(): String {
        return this.name.toLowerCase()
    }
}