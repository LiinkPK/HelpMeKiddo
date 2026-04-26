package com.helpmekiddo

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    var hasRoot = false

    var installingFile: String?
        get() = getSharedPreferences("installer", MODE_PRIVATE).getString("installing_file", null)
        set(value) = getSharedPreferences("installer", MODE_PRIVATE).edit()
            .putString("installing_file", value).apply()

    var installStartedAt: Long
        get() = getSharedPreferences("installer", MODE_PRIVATE).getLong("install_started_at", 0L)
        set(value) = getSharedPreferences("installer", MODE_PRIVATE).edit()
            .putLong("install_started_at", value).apply()

    val deviceAbi: String
        get() {
            val abis = android.os.Build.SUPPORTED_ABIS
            return when {
                abis.contains("arm64-v8a") -> "arm64-v8a"
                abis.contains("armeabi-v7a") -> "armeabi-v7a"
                abis.contains("x86_64") -> "x86_64"
                abis.contains("x86") -> "x86"
                else -> "armeabi-v7a"
            }
        }

    fun isFirstLaunch(): Boolean {
        return getSharedPreferences("installer", MODE_PRIVATE)
            .getBoolean("first_launch", true)
    }

    fun markLaunched() {
        getSharedPreferences("installer", MODE_PRIVATE)
            .edit()
            .putBoolean("first_launch", false)
            .apply()
    }

    fun isDownloaderInstalled(): Boolean {
        return try {
            packageManager.getApplicationInfo("com.esaba.downloader", 0)
            true
        } catch (e: Exception) { false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread {
            android.util.Log.d("HMK", "Starting root check")
            hasRoot = try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                android.util.Log.d("HMK", "su process started")
                val completed = process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)
                android.util.Log.d("HMK", "su completed: $completed")
                if (completed) {
                    val result = process.inputStream.bufferedReader().readText()
                    android.util.Log.d("HMK", "su result: $result")
                    result.contains("uid=0")
                } else {
                    process.destroy()
                    false
                }
            } catch (e: Exception) {
                android.util.Log.d("HMK", "su exception: ${e.message}")
                false
            }
            android.util.Log.d("HMK", "hasRoot: $hasRoot")

            if (hasRoot) {
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c",
                        "appops set $packageName REQUEST_INSTALL_PACKAGES allow"))
                        .waitFor()
                } catch (e: Exception) { }
            }

            runOnUiThread {
                if (savedInstanceState == null) {
                    val fragment = when {
                        hasRoot -> AppBrowserFragment()
                        !packageManager.canRequestPackageInstalls() -> OnboardingFragment()
                        else -> AppBrowserFragment()
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_browse_fragment, fragment)
                        .commit()
                }
            }
        }.start()
    }
}