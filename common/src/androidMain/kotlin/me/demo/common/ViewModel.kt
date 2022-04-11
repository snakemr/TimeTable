package me.demo.common


actual open class ViewModel: androidx.lifecycle.ViewModel() {
    var preferences: android.content.SharedPreferences? = null

    actual fun saveString(key: String, value: String) {
        preferences?.edit()?.apply {
            putString(key, value)
            apply()
        }
    }

    actual fun getString(key: String): String? {
        return preferences?.getString(key, null)
    }
}

