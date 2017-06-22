package org.generousg.fruitylib.util


class SneakyThrower {
    companion object {
        class DummyException : RuntimeException()
        class Thrower<T : Throwable> {
            fun sneakyThrow(exception: Throwable): DummyException? {
                HELPER.sneakyThrow(exception)
                return null
            }
        }
        private val HELPER = Thrower<DummyException>()

        fun sneakyThrow(exception: Throwable): DummyException {
            HELPER.sneakyThrow(exception)
            return DummyException()
        }

        fun <T : Throwable> sneakyThrows(cls: Class<out T>) = Unit
    }
}