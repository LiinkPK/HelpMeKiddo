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
import androidx.fragment.app.Fragment

class DownloaderRequiredFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private val mainActivity get() = requireActivity() as MainActivity
    private val checkRunnable = object : Runnable {
        override fun run() {
            if (mainActivity.isDownloaderInstalled()) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_browse_fragment, AppBrowserFragment())
                    .commit()
            } else {
                handler.postDelayed(this, 2000)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0D0D1A"))
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setPadding(160, 80, 160, 80)
        }

        val icon = TextView(requireContext()).apply {
            text = "⬇️"
            textSize = 48f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        val title = TextView(requireContext()).apply {
            text = "Necesitas Downloader"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val body = TextView(requireContext()).apply {
            text = "Para instalar aplicaciones en este dispositivo necesitas la app \"Downloader\" de AFTVnews.\n\nEs gratuita y solo tienes que instalarla una vez.\n\n1. Pulsa el botón de abajo\n2. Instala Downloader desde la tienda\n3. Vuelve aquí — la app continuará sola"
            textSize = 16f
            setTextColor(Color.parseColor("#AAAACC"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
            setLineSpacing(0f, 1.4f)
        }

        val btn = TextView(requireContext()).apply {
            text = "Instalar Downloader →"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f
                setColor(Color.parseColor("#FF8C00"))
            }
            setPadding(48, 24, 48, 24)
            gravity = Gravity.CENTER
            isFocusable = true
            isFocusableInTouchMode = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER }

            setOnFocusChangeListener { _, hasFocus ->
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(Color.parseColor(if (hasFocus) "#FFA040" else "#FF8C00"))
                }
            }

            setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.esaba.downloader")))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.esaba.downloader")))
                }
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
        handler.post(checkRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(checkRunnable)
    }
}