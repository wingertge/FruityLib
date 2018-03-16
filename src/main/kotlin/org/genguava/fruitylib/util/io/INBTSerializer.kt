package org.genguava.fruitylib.util.io


interface INBTSerializer<T> : org.genguava.fruitylib.util.io.INbtReader<T>, org.genguava.fruitylib.util.io.INbtWriter<T>, org.genguava.fruitylib.util.io.INbtChecker

interface INbtChecker {
    fun checkTagType(tag: net.minecraft.nbt.NBTBase): Boolean
}

interface INbtReader<out T> {
    fun readFromNBT(tag: net.minecraft.nbt.NBTTagCompound, name: String): T
}

interface INbtWriter<in T> {
    fun writeToNBT(o: T, tag: net.minecraft.nbt.NBTTagCompound, name: String)
}