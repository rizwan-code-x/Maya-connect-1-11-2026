package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper utility to share the complete application APK file along with the official
 * rizwanr.ai.studio link underneath.
 */
object AppShareHelper {

    const val OFFICIAL_SHARE_DOMAIN = "rizwanr.ai.studio"
    const val OFFICIAL_SHARE_URL = "https://rizwanr.ai.studio"

    /**
     * Shares the complete installed APK file via FileProvider along with the
     * rizwanr.ai.studio link in the share message below.
     */
    fun shareFullApkWithLink(context: Context) {
        val shareMessage = buildString {
            appendLine("Maya Connect — REXLIFY Official Application (Full APK)")
            appendLine()
            appendLine("Official Link:")
            appendLine(OFFICIAL_SHARE_URL)
            append(OFFICIAL_SHARE_DOMAIN)
        }

        try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (sourceApk.exists() && sourceApk.canRead()) {
                val shareDir = File(context.cacheDir, "shared_apk")
                if (!shareDir.exists()) {
                    shareDir.mkdirs()
                }
                val targetApk = File(shareDir, "MayaConnect_Rexlify.apk")
                // Copy APK if not already copied or if size differs
                if (!targetApk.exists() || targetApk.length() != sourceApk.length()) {
                    sourceApk.inputStream().use { input ->
                        targetApk.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val apkUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    targetApk
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_SUBJECT, "Maya Connect APK • $OFFICIAL_SHARE_DOMAIN")
                    putExtra(Intent.EXTRA_STREAM, apkUri)
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    clipData = ClipData.newRawUri("MayaConnect_Rexlify.apk", apkUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Share Complete APK & $OFFICIAL_SHARE_DOMAIN").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(chooser)
                return
            }
        } catch (_: Exception) {
            // Fallback below if FileProvider or sourceDir copy encounters a restriction
        }

        // Fallback text share with rizwanr.ai.studio if APK stream fails
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Maya Connect • $OFFICIAL_SHARE_DOMAIN")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        context.startActivity(
            Intent.createChooser(fallbackIntent, "Share Maya Connect • $OFFICIAL_SHARE_DOMAIN").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    /**
     * Copies https://rizwanr.ai.studio to the clipboard.
     */
    fun copyOfficialLink(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("Maya Connect Official Link", OFFICIAL_SHARE_URL))
            Toast.makeText(context, "Copied: $OFFICIAL_SHARE_DOMAIN", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }
}
