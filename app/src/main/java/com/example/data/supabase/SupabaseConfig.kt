package com.example.data.supabase

import com.example.BuildConfig

object SupabaseConfig {
    val URL: String
        get() {
            return try {
                val configUrl = BuildConfig.SUPABASE_URL
                if (!configUrl.isNullOrEmpty() && configUrl != "null") configUrl
                else "https://ljlynxhrdoesurrmaody.supabase.co"
            } catch (e: Throwable) {
                "https://ljlynxhrdoesurrmaody.supabase.co"
            }
        }

    val ANON_KEY: String
        get() {
            return try {
                val configKey = BuildConfig.SUPABASE_KEY
                if (!configKey.isNullOrEmpty() && configKey != "null") configKey
                else "sb_publishable_XgArKzuKegfVJ7vGpdpzGg_SmxJ9pk4"
            } catch (e: Throwable) {
                "sb_publishable_XgArKzuKegfVJ7vGpdpzGg_SmxJ9pk4"
            }
        }

    const val ADMIN_EMAIL = "mdrayhn3331@gmail.com"
}
