package org.generousg.fruitylib.network.rpc


interface IRpcTargetProvider {
    fun createRpcTarget(): IRpcTarget
}