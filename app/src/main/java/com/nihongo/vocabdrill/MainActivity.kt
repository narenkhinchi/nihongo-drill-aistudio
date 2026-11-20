package com.nihongo.vocabdrill

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.nihongo.vocabdrill.model.ThemeMode
import com.nihongo.vocabdrill.ui.screens.MainScreen
import com.nihongo.vocabdrill.update.AppUpdateViewModel
import com.nihongo.vocabdrill.viewmodel.NihongoViewModel
import java.io.File

class MainActivity : ComponentActivity() {
    private var pendingUpdateInstall: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[NihongoViewModel::class.java]
        val appUpdateViewModel = ViewModelProvider(this)[AppUpdateViewModel::class.java]

        setContent {
            val settings by viewModel.appSettings.collectAsState()
            SideEffect {
                val dark = settings.themeMode == ThemeMode.DARK
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                )
            }
            MainScreen(
                viewModel = viewModel,
                appUpdateViewModel = appUpdateViewModel,
                onInstallUpdate = ::requestUpdateInstall
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val pendingFile = pendingUpdateInstall ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls()) {
            pendingUpdateInstall = null
            launchPackageInstaller(pendingFile)
        }
    }

    private fun requestUpdateInstall(apkFile: File) {
        if (!apkFile.isFile) {
            Toast.makeText(this, "Update file not found. Please download it again.", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            pendingUpdateInstall = apkFile
            Toast.makeText(
                this,
                "Please allow Nihongo Drill to install its downloaded update.",
                Toast.LENGTH_LONG
            ).show()
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:$packageName")
            )
            startActivity(settingsIntent)
            return
        }

        launchPackageInstaller(apkFile)
    }

    private fun launchPackageInstaller(apkFile: File) {
        try {
            val apkUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = apkUri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
            }
            startActivity(installIntent)
        } catch (error: Exception) {
            Toast.makeText(
                this,
                "Could not open Android installer. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
