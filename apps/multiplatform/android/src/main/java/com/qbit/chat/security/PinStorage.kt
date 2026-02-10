package com.qbit.chat.security

interface PinStorage {
    fun getString(key: String, defValue: String?): String?
    fun putString(key: String, value: String)
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun clear()
    fun getAll(): Map<String, *>
}
