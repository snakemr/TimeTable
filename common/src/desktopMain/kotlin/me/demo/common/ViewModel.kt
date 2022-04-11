package me.demo.common

actual open class ViewModel {
    actual fun saveString(key: String, value: String) {
        java.io.File("$key.cfg").writeText(value)
    }

    actual fun getString(key: String): String? {
        return java.io.File("$key.cfg").run {
            if (canRead()) readText() else null
        }
    }
}
