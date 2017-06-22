package org.generousg.fruitylib.network.rpc

import net.minecraft.entity.player.EntityPlayer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException


interface IRpcTarget {
    val target: Any?
    @Throws(IOException::class)
    fun writeToStream(output: DataOutput)
    @Throws(IOException::class)
    fun readFromStream(player: EntityPlayer, input: DataInput)
    fun afterCall()
}