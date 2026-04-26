package com.helpmekiddo

import android.content.*
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

enum class AppType {
    APK,
    STORE
}

data class AppItem(
        val name: String,
        var version: String,
        val url: String,
        val drawableId: Int = 0,
        var installed: Boolean = false,
        var updateAvailable: Boolean = false,
        val type: AppType = AppType.APK,
        val packageId: String = "",
        val githubRepo: String = "",
        val knownLatestVersion: String = "",
        val description: String = ""
)

class AppBrowserFragment : Fragment() {

    private lateinit var grid: GridLayout
    private val handler = Handler(Looper.getMainLooper())
    private val mainActivity
        get() = requireActivity() as MainActivity

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val root =
                LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(Color.parseColor("#0D0D1A"))
                    layoutParams =
                            ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            )
                }

        val header =
                LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(80, 56, 80, 24)
                }
        val titleView =
                TextView(requireContext()).apply {
                    text = "INSTALADOR DE APPS PARA PADRES"
                    textSize = 34f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.WHITE)
                }
        val subtitleView =
                TextView(requireContext()).apply {
                    text = "Selecciona una aplicación para instalar"
                    textSize = 15f
                    setTextColor(Color.parseColor("#6666AA"))
                    setPadding(0, 8, 0, 0)
                }
        header.addView(titleView)
        header.addView(subtitleView)

        val divider =
                View(requireContext()).apply {
                    setBackgroundColor(Color.parseColor("#1A1A3A"))
                    layoutParams =
                            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2)
                                    .apply { setMargins(80, 8, 80, 0) }
                }

        val scroll = ScrollView(requireContext()).apply { setPadding(64, 40, 64, 56) }
        grid =
                GridLayout(requireContext()).apply {
                    columnCount = 3
                    useDefaultMargins = false
                }
        scroll.addView(grid)
        root.addView(header)
        root.addView(divider)
        root.addView(scroll)

        refreshList()
        fetchGithubVersions()
        return root
    }

    private fun fetchGithubVersions() {
        appList.forEachIndexed { index, app ->
            if (app.githubRepo.isEmpty()) return@forEachIndexed
            Thread {
                        try {
                            val url =
                                    URL(
                                            "https://api.github.com/repos/${app.githubRepo}/releases/latest"
                                    )
                            val conn = url.openConnection() as HttpURLConnection
                            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                            conn.connectTimeout = 5000
                            conn.readTimeout = 5000
                            val json = JSONObject(conn.inputStream.bufferedReader().readText())
                            val latestVersion = json.getString("tag_name").removePrefix("v")
                            handler.post {
                                val installedVersion = getInstalledVersion(app.packageId)
                                val hasUpdate =
                                        appList[index].installed &&
                                                installedVersion.isNotEmpty() &&
                                                isNewerVersion(latestVersion, installedVersion)
                                appList[index] =
                                        appList[index].copy(
                                                version = latestVersion,
                                                updateAvailable = hasUpdate
                                        )
                                refreshList()
                            }
                        } catch (e: Exception) {
                            handler.post {
                                appList[index] = appList[index].copy(version = "latest")
                                refreshList()
                            }
                        }
                    }
                    .start()
        }
    }

    private val appList by lazy {
        val abi = mainActivity.deviceAbi
        val is64bit = abi == "arm64-v8a" || abi == "x86_64"

        mutableListOf(
                AppItem(
                        name = "PPSSPP",
                        description = "La PSP pero en la tele.",
                        version = "...",
                        url = "https://f-droid.org/repo/org.ppsspp.ppsspp_119030000.apk",
                        drawableId = R.drawable.ppsspp,
                        packageId = "org.ppsspp.ppsspp",
                        githubRepo = "hrydgard/ppsspp"
                ),
                AppItem(
                        name = "RetroArch",
                        description = "Para los juegos viejos.",
                        version = "1.22.2",
                        url = "https://buildbot.libretro.com/stable/1.22.2/android/RetroArch.apk",
                        drawableId = R.drawable.retroarch,
                        packageId = if (is64bit) "com.retroarch" else "com.retroarch.ra32",
                        knownLatestVersion = "1.22.2"
                ),
                AppItem(
                        name = "NetherSX2",
                        description = "La Play 2 pero si el cacharro puede.",
                        version = "2.2a",
                        url =
                                "https://github.com/Trixarian/NetherSX2-classic/releases/download/2.2a/NetherSX2-v2.2a-3668.apk",
                        drawableId = R.drawable.nethersx2,
                        packageId = "xyz.aethersx2.android",
                        knownLatestVersion = "2.2a"
                ),
                AppItem(
                        name = "Stremio",
                        description = "Para ver pelis y series.",
                        version = "1.9.12",
                        url =
                                if (is64bit) "https://www.stremio.com/downloads/android/arm64"
                                else "https://www.stremio.com/downloads/android/arm",
                        drawableId = R.drawable.stremio,
                        packageId = "com.stremio.one",
                        knownLatestVersion = "1.9.12"
                ),
                AppItem(
                        name = "Kodi",
                        description = "Lo que te puse para ver la tele",
                        version = "...",
                        url =
                                if (is64bit)
                                        "https://mirrors.kodi.tv/releases/android/arm64-v8a/kodi-21.2-Omega-arm64-v8a.apk"
                                else
                                        "https://mirrors.kodi.tv/releases/android/arm-v7a/kodi-21.2-Omega-arm-v7a.apk",
                        drawableId = R.drawable.kodi,
                        packageId = "org.xbmc.kodi",
                        githubRepo = "xbmc/xbmc"
                ),
                AppItem(
                        name = "Downloader",
                        description = "Este ni tocarlo.",
                        version = "",
                        url = "",
                        drawableId = R.drawable.downloader,
                        packageId = "com.esaba.downloader",
                        type = AppType.STORE,
                        knownLatestVersion = "1.5.3"
                ),
                AppItem(
                        name = "File Manager",
                        description = "Para archivos, pero no lo toques.",
                        version = "",
                        url = "",
                        drawableId = R.drawable.filemanager,
                        packageId = "com.alphainventor.filemanager",
                        type = AppType.STORE,
                        knownLatestVersion = "3.7.2"
                ),
                AppItem(
                        name = "FX File Explorer",
                        version = "",
                        url = "",
                        drawableId = R.drawable.filemanager,
                        packageId = "nextapp.fx",
                        type = AppType.STORE,
                        knownLatestVersion = "",
                        description = "Otro para buscar archivos."
                ),
                AppItem(
                        name = "Play! Emulator",
                        version = "0.70",
                        url = "https://purei.org/downloads/play/stable/0.70/Play-release.apk",
                        drawableId = R.drawable.play,
                        packageId = "com.virtualapplications.play",
                        knownLatestVersion = "0.70",
                        description = "Para la PS2, pero va peor."
                ),
                AppItem(
                        name = "IPTV Pro",
                        version = "",
                        url = "",
                        drawableId = R.drawable.iptvpro,
                        packageId = "com.aloj22.iptvprostreamplayer",
                        type = AppType.STORE,
                        knownLatestVersion = "",
                        description = "Para ver la tele por internet."
                ),
        )
    }

    private fun getInstalledVersion(packageId: String): String {
        return try {
            requireContext().packageManager.getPackageInfo(packageId, 0).versionName ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun isNewerVersion(latest: String, installed: String): Boolean {
        return try {
            val l = latest.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
            val i = installed.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
            val maxLen = maxOf(l.size, i.size)
            for (x in 0 until maxLen) {
                val lv = l.getOrElse(x) { 0 }
                val iv = i.getOrElse(x) { 0 }
                if (lv > iv) return true
                if (lv < iv) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun refreshInstalledStatus() {
        val pm = requireContext().packageManager
        val installedPackages = pm.getInstalledPackages(0)
        appList.forEachIndexed { i, app ->
            val installedPkg = installedPackages.find { it.packageName == app.packageId }
            val installed = installedPkg != null
            val installedVersion = installedPkg?.versionName ?: ""
            val hasUpdate =
                    installed &&
                            app.knownLatestVersion.isNotEmpty() &&
                            isNewerVersion(app.knownLatestVersion, installedVersion)
            appList[i] =
                    app.copy(
                            installed = installed,
                            updateAvailable =
                                    if (app.githubRepo.isNotEmpty()) app.updateAvailable
                                    else hasUpdate
                    )
        }
    }

    private fun refreshList() {
        refreshInstalledStatus()
        grid.removeAllViews()

        appList.forEachIndexed { index, app ->
            val borderColor =
                    when {
                        app.updateAvailable -> "#8A2BE2"
                        app.installed -> "#1DB954"
                        else -> "#3A6FD8"
                    }
            val bgColor =
                    when {
                        app.updateAvailable -> "#160D22"
                        app.installed -> "#0D1F12"
                        else -> "#0D1225"
                    }

            val cardBg =
                    GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 16f
                        setColor(Color.parseColor(bgColor))
                        setStroke(2, Color.parseColor(borderColor))
                    }

            val card =
                    LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(28, 28, 28, 28)
                        background = cardBg
                        isFocusable = true
                        isFocusableInTouchMode = true
                        layoutParams =
                                GridLayout.LayoutParams().apply {
                                    width = 0
                                    height = GridLayout.LayoutParams.WRAP_CONTENT
                                    columnSpec = GridLayout.spec(index % 3, 1, 1f)
                                    rowSpec = GridLayout.spec(index / 3)
                                    setMargins(16, 16, 16, 16)
                                }
                        setOnFocusChangeListener { v, hasFocus ->
                            val focusBg =
                                    GradientDrawable().apply {
                                        shape = GradientDrawable.RECTANGLE
                                        cornerRadius = 16f
                                        setColor(
                                                Color.parseColor(
                                                        if (hasFocus) "#1E1E40" else bgColor
                                                )
                                        )
                                        setStroke(
                                                if (hasFocus) 3 else 2,
                                                Color.parseColor(
                                                        if (hasFocus) "#FFFFFF" else borderColor
                                                )
                                        )
                                    }
                            v.background = focusBg
                        }
                        setOnClickListener { handleClick(app) }
                    }

            val iconView =
                    ImageView(requireContext()).apply {
                        try {
                            setImageResource(app.drawableId)
                        } catch (e: Exception) {}
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        layoutParams =
                                LinearLayout.LayoutParams(96, 96).apply { setMargins(0, 0, 0, 20) }
                        adjustViewBounds = true
                    }

            val nameView =
                    TextView(requireContext()).apply {
                        text = app.name
                        textSize = 17f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(Color.WHITE)
                    }

            val versionView =
                    TextView(requireContext()).apply {
                        text = if (app.type == AppType.STORE) "Play Store" else "v${app.version}"
                        textSize = 12f
                        setTextColor(Color.parseColor("#6666AA"))
                        setPadding(0, 4, 0, 14)
                    }

            val descView =
                    TextView(requireContext()).apply {
                        text = app.description
                        textSize = 11f
                        setTypeface(null, Typeface.ITALIC)
                        setTextColor(Color.parseColor("#444466"))
                        setPadding(0, 2, 0, 10)
                    }
            card.addView(iconView)
            card.addView(nameView)
            card.addView(descView)
            card.addView(versionView)
            card.addView(badge)

            val badgeText =
                    when {
                        app.updateAvailable -> "↑  Actualizar"
                        app.installed -> "✓  Instalado"
                        app.type == AppType.STORE -> "Ver en Play Store"
                        else -> "Disponible"
                    }
            val badgeColor =
                    when {
                        app.updateAvailable -> "#8A2BE2"
                        app.installed -> "#1DB954"
                        app.type == AppType.STORE -> "#FF8C00"
                        else -> "#3A6FD8"
                    }
            val badgeBgColor =
                    when {
                        app.updateAvailable -> "#1A0A2E"
                        app.installed -> "#0A1F0D"
                        app.type == AppType.STORE -> "#1A0A00"
                        else -> "#0A1030"
                    }

            val badgeBg =
                    GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 20f
                        setColor(Color.parseColor(badgeBgColor))
                        setStroke(1, Color.parseColor(badgeColor))
                    }

            val badge =
                    TextView(requireContext()).apply {
                        text = badgeText
                        textSize = 11f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(Color.parseColor(badgeColor))
                        background = badgeBg
                        setPadding(16, 6, 16, 6)
                        layoutParams =
                                LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                    }

            card.addView(iconView)
            card.addView(nameView)
            card.addView(versionView)
            card.addView(badge)
            grid.addView(card)
        }
    }

    private fun handleClick(app: AppItem) {
        when {
            app.type == AppType.STORE -> openInStore(app.packageId)
            app.installed && !app.updateAvailable -> {
                Toast.makeText(
                                requireContext(),
                                "${app.name} ya está instalado.",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
            else -> downloadAndInstall(app)
        }
    }

    private fun openInStore(packageId: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageId")))
        } catch (e: Exception) {
            try {
                startActivity(
                        Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://play.google.com/store/apps/details?id=$packageId"
                                )
                        )
                )
            } catch (e2: Exception) {
                Toast.makeText(requireContext(), "No se pudo abrir la tienda.", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun downloadAndInstall(app: AppItem) {
        val destFile = File(requireContext().filesDir, "${app.name.replace(" ", "_")}.apk")

        if (destFile.exists()) {
            triggerInstall(destFile)
            return
        }

        val progressDialog =
                android.app.ProgressDialog(requireContext()).apply {
                    setTitle("Descargando ${app.name}")
                    setMessage("Por favor espera…")
                    setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
                    setCancelable(false)
                    max = 100
                    show()
                }

        Thread {
                    try {
                        var finalUrl = app.url
                        var conn = URL(finalUrl).openConnection() as HttpURLConnection
                        conn.connectTimeout = 10000
                        conn.readTimeout = 60000
                        conn.instanceFollowRedirects = false
                        conn.connect()

                        var responseCode = conn.responseCode
                        while (responseCode in 300..399) {
                            val location = conn.getHeaderField("Location")
                            conn.disconnect()
                            finalUrl =
                                    if (location.startsWith("http")) location
                                    else "https://${URL(finalUrl).host}$location"
                            conn = URL(finalUrl).openConnection() as HttpURLConnection
                            conn.connectTimeout = 10000
                            conn.readTimeout = 60000
                            conn.instanceFollowRedirects = false
                            conn.connect()
                            responseCode = conn.responseCode
                        }

                        val fileSize = conn.contentLength
                        val input = conn.inputStream
                        val output = destFile.outputStream()
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (fileSize > 0) {
                                val progress = (totalRead * 100 / fileSize).toInt()
                                handler.post { progressDialog.progress = progress }
                            }
                        }
                        output.flush()
                        output.close()
                        input.close()
                        conn.disconnect()

                        handler.post {
                            progressDialog.dismiss()
                            triggerInstall(destFile)
                        }
                    } catch (e: Exception) {
                        handler.post {
                            progressDialog.dismiss()
                            Toast.makeText(
                                            requireContext(),
                                            "Error al descargar: ${e.message}",
                                            Toast.LENGTH_LONG
                                    )
                                    .show()
                        }
                    }
                }
                .start()
    }

    private fun triggerInstall(file: File) {
        if (mainActivity.hasRoot) {
            installWithRoot(file)
        } else {
            doInstallIntent(file)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    override fun onPause() {
        super.onPause()
        // If we're leaving the fragment without a pending install, clear the state
        if (mainActivity.installingFile == null) {
            mainActivity.installStartedAt = 0L
        }
    }

    private fun doInstallIntent(file: File) {
        try {
            val uri =
                    androidx.core.content.FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.fileprovider",
                            file
                    )
            val intent =
                    Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                        data = uri
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al instalar: ${e.message}", Toast.LENGTH_LONG)
                    .show()
        }
    }

    private fun installWithRoot(file: File) {
        Thread {
                    try {
                        val tmpFile = "/data/local/tmp/${file.name}"
                        val copy =
                                Runtime.getRuntime()
                                        .exec(
                                                arrayOf(
                                                        "su",
                                                        "-c",
                                                        "cp '${file.absolutePath}' $tmpFile && chmod 644 $tmpFile"
                                                )
                                        )
                        copy.waitFor()
                        val install =
                                Runtime.getRuntime()
                                        .exec(arrayOf("su", "-c", "pm install -r $tmpFile"))
                        val result = install.inputStream.bufferedReader().readText()
                        install.waitFor()
                        Runtime.getRuntime().exec(arrayOf("su", "-c", "rm $tmpFile"))
                        handler.post {
                            if (result.contains("Success")) {
                                file.delete()
                                Toast.makeText(
                                                requireContext(),
                                                "¡Instalado correctamente!",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                                refreshList()
                            } else {
                                Toast.makeText(
                                                requireContext(),
                                                "Error al instalar: $result",
                                                Toast.LENGTH_LONG
                                        )
                                        .show()
                            }
                        }
                    } catch (e: Exception) {
                        handler.post {
                            Toast.makeText(
                                            requireContext(),
                                            "Error: ${e.message}",
                                            Toast.LENGTH_LONG
                                    )
                                    .show()
                        }
                    }
                }
                .start()
    }

    private fun openInDownloader(url: String) {
        android.app.AlertDialog.Builder(requireContext())
                .setTitle("Abriendo Downloader")
                .setMessage(
                        "Downloader se abrirá para instalar la app. Cuando termines, vuelve aquí con el botón de inicio."
                )
                .setPositiveButton("Continuar") { _, _ ->
                    val intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                setPackage("com.esaba.downloader")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                    try {
                        requireContext().startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(
                                        requireContext(),
                                        "No se pudo abrir Downloader.",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
    }
}
