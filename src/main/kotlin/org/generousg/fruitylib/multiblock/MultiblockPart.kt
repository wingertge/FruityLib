package org.generousg.fruitylib.multiblock

import net.minecraft.util.IStringSerializable


enum class MultiblockPart: IStringSerializable {
    SINGLE,
    END,
    LINE_EDGE,
    CORNER,
    EDGE,
    CENTER,
    BOTTOM_END,
    BOTTOM_EDGE,
    BOTTOM_LINE_EDGE,
    BOTTOM_CORNER,
    BOTTOM_CENTER,
    BOTTOM_PILLAR,
    CENTER_END,
    CENTER_CORNER,
    CENTER_EDGE,
    CENTER_LINE_EDGE,
    CENTER_PILLAR,
    TOP_END,
    TOP_CORNER,
    TOP_EDGE,
    TOP_LINE_EDGE,
    TOP_PILLAR,
    TOP_CENTER,
    HIDDEN;

    override fun getName(): String {
        return this.name.toLowerCase()
    }
}