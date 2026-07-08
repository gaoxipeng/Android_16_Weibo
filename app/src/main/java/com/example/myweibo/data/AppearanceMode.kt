package com.example.myweibo.data

enum class AppearanceMode(
    val storageValue: String,
    val label: String,
    val description: String,
) {
    System("system", "跟随系统", "与系统深色模式设置保持一致"),
    Light("light", "浅色模式", "始终使用浅色界面"),
    Dark("dark", "深色模式", "始终使用深色界面");

    companion object {
        fun fromStorage(value: String?): AppearanceMode =
            entries.firstOrNull { it.storageValue == value } ?: System
    }
}
