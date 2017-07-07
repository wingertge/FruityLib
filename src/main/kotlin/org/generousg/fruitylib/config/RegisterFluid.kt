package org.generousg.fruitylib.config


annotation class RegisterFluid(val name: String, val unlocalizedName: String = "[default]", val luminosity: Int = 10, val density: Int = 800,
                               val viscosity: Int = 1500, val gaseous: Boolean = false, val temperature: Int = 20, val hasBucket: Boolean = false,
                               val enabled: Boolean = true, val configurable: Boolean = false)