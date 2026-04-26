package com.helpmekiddo

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class OnboardingFragment : Fragment() {

    private val mainActivity get() = requireActivity() as MainActivity
    private val handler = Handler(Looper.getMainLooper())

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Wait 1.5 seconds for Xiaomi to settle the permission state
        handler.postDelayed({ checkAndProceed() }, 1500)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0D0D1A"))
            gravity = android.view.Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setPadding(160, 80, 160, 80)
        }

        val icon = TextView(requireContext()).apply {
            text = "🛡️"
            textSize = 48f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        val title = TextView(requireContext()).apply {
            text = "Un paso antes de empezar"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val body = TextView(requireContext()).apply {
            text = "Para instalar apps desde aquí, necesitas permitir que \"Help me, kiddo!\" pueda instalar aplicaciones.\n\n1. Pulsa \"Ir a ajustes\"\n2. Activa el interruptor junto a \"Help me, kiddo!\"\n3. Vuelve aquí\n\nEsto solo es necesario una vez."
            textSize = 16f
            setTextColor(Color.parseColor("#AAAACC"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 48)
            setLineSpacing(0f, 1.4f)
        }

        val btn = TextView(requireContext()).apply {
            text = "Ir a ajustes →"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f
                setColor(Color.parseColor("#3A6FD8"))
            }
            setPadding(48, 24, 48, 24)
            gravity = android.view.Gravity.CENTER
            isFocusable = true
            isFocusableInTouchMode = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = android.view.Gravity.CENTER }

            setOnFocusChangeListener { _, hasFocus ->
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(Color.parseColor(if (hasFocus) "#5A8FFF" else "#3A6FD8"))
                }
            }

            setOnClickListener {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                permissionLauncher.launch(intent)
            }
        }

        root.addView(icon)
        root.addView(title)
        root.addView(body)
        root.addView(btn)
        return root
    }

    override fun onResume() {
        super.onResume()
        // Don't check here — Xiaomi resets permission state briefly on resume
    }

    private fun checkAndProceed() {
        val granted = try {
            val appOpsManager = requireContext().getSystemService(android.app.AppOpsManager::class.java)
            val mode = appOpsManager.checkOpNoThrow(
                "android:request_install_packages",
                requireContext().applicationInfo.uid,
                requireContext().packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            requireContext().packageManager.canRequestPackageInstalls()
        }

        if (granted) {
            mainActivity.markLaunched()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, AppBrowserFragment())
                .commit()
        }
    }
}