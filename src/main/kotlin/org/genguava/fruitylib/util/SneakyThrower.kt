package org.genguava.fruitylib.util


class SneakyThrower {
    companion object {
        class DummyException : RuntimeException()
        class Thrower {
            fun sneakyThrow(exception: Throwable): DummyException? {
                HELPER.sneakyThrow(exception)
                return null
            }
        }
        private val HELPER = Thrower()

        fun sneakyThrow(exception: Throwable): DummyException {
            HELPER.sneakyThrow(exception)
            return DummyException()
        }
    }
}