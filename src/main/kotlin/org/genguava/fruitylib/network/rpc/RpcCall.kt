package org.genguava.fruitylib.network.rpc

import java.lang.reflect.Method


class RpcCall(val target: IRpcTarget, val method: Method, val args: Array<Any?>)