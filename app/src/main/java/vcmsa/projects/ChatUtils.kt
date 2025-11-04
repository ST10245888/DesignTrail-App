package vcmsa.projects.fkj_consultants.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ChatUtils {
    private const val TAG = "ChatUtils"
    private val storage = FirebaseStorage.getInstance()

    /**
     * Opens an attachment based on its URI or file path
     * Handles both text content and file attachments
     */
    fun openAttachment(context: Context, attachmentUri: String) {
        try {
            Log.d(TAG, "Opening attachment: $attachmentUri")

            // Check if it's text content (quotation text stored directly)
            if (isTextContent(attachmentUri)) {
                openTextContent(context, attachmentUri)
                return
            }

            // It's a file path or URI
            val file = File(attachmentUri)
            if (file.exists() && file.isFile) {
                openLocalFile(context, file)
            } else {
                // Try as Firebase Storage URL or web URL
                if (isValidUrl(attachmentUri)) {
                    openUrl(context, attachmentUri)
                } else {
                    Log.w(TAG, "Attachment not found or invalid: $attachmentUri")
                    showErrorToast(context, "Attachment not found or unavailable")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening attachment: ${e.message}", e)
            showErrorToast(context, "Unable to open attachment: ${e.message}")
        }
    }

    /**
     * Checks if the content is text-based (quotation text)
     */
    private fun isTextContent(content: String): Boolean {
        return content.startsWith("==========") ||
                content.length > 500 &&
                content.contains("FKJ CONSULTANTS", ignoreCase = true)
    }

    /**
     * Checks if the string is a valid URL
     */
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") ||
                url.startsWith("https://") ||
                url.startsWith("content://") ||
                url.startsWith("file://")
    }

    /**
     * Opens a text content attachment
     */
    private fun openTextContent(context: Context, textContent: String) {
        try {
            // Create temp file for viewing
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val tempFile = File(context.cacheDir, "quotation_$timestamp.txt")
            tempFile.writeText(textContent)

            Log.d(TAG, "Created temp file for text content: ${tempFile.absolutePath}")
            openLocalFile(context, tempFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening text content: ${e.message}", e)
            showErrorToast(context, "Error displaying quotation: ${e.message}")
        }
    }

    /**
     * Opens a local file using FileProvider and appropriate intent
     */
    private fun openLocalFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val mimeType = getMimeType(file.absolutePath)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Verify there's an app that can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Open with"))
                Log.d(TAG, "Successfully opened file: ${file.name} with MIME type: $mimeType")
            } else {
                Log.w(TAG, "No app found to handle MIME type: $mimeType")
                showErrorToast(context, "No app found to open this file type")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening local file: ${e.message}", e)
            showErrorToast(context, "Error opening file: ${e.message}")
        }
    }

    /**
     * Opens a URL (for Firebase Storage URLs or web links)
     */
    private fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Log.d(TAG, "Successfully opened URL: $url")
            } else {
                Log.w(TAG, "No app found to handle URL: $url")
                showErrorToast(context, "No app found to open this link")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL: ${e.message}", e)
            showErrorToast(context, "Error opening link: ${e.message}")
        }
    }

    /**
     * Get MIME type from file extension
     */
    fun getMimeType(path: String): String {
        val extension = path.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when (extension) {
            "pdf" -> "application/pdf"
            "txt", "text" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "mp3" -> "audio/mpeg"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        }
    }

    /**
     * Upload file to Firebase Storage and return download URL
     */
    fun uploadAttachment(
        file: File,
        userEmail: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            if (!file.exists() || !file.isFile) {
                onFailure(IllegalArgumentException("File does not exist or is not a file"))
                return
            }

            val fileName = "${System.currentTimeMillis()}_${file.name}"
            val storageRef = storage.reference
                .child("chat_attachments")
                .child(encodeEmail(userEmail))
                .child(fileName)

            Log.d(TAG, "Uploading file: ${file.absolutePath} to Firebase Storage")

            storageRef.putFile(Uri.fromFile(file))
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    Log.d(TAG, "Upload progress: $progress%")
                }
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.d(TAG, "File uploaded successfully: $uri")
                            onSuccess(uri.toString())
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to get download URL: ${e.message}", e)
                            onFailure(e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Upload failed: ${e.message}", e)
                    onFailure(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file: ${e.message}", e)
            onFailure(e)
        }
    }

    /**
     * Download file from Firebase Storage URL
     */
    fun downloadAttachment(
        context: Context,
        downloadUrl: String,
        fileName: String,
        onSuccess: (File) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val storageRef = storage.getReferenceFromUrl(downloadUrl)
            val localFile = File(context.cacheDir, fileName)

            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    Log.d(TAG, "File downloaded successfully: ${localFile.absolutePath}")
                    onSuccess(localFile)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Download failed: ${e.message}", e)
                    onFailure(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file: ${e.message}", e)
            onFailure(e)
        }
    }

    /**
     * Get display text for attachment message
     */
    fun getAttachmentDisplayText(attachmentUri: String, originalMessage: String): String {
        if (isTextContent(attachmentUri)) {
            return "📄 Quotation Document"
        }

        val fileName = getFileNameFromUri(attachmentUri)
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)

        val icon = when (extension) {
            "pdf" -> "📄"
            "txt", "text" -> "📝"
            "jpg", "jpeg", "png", "gif" -> "🖼️"
            "doc", "docx" -> "📃"
            "xls", "xlsx" -> "📊"
            "ppt", "pptx" -> "📽️"
            "zip", "rar" -> "📦"
            "mp3", "wav" -> "🎵"
            "mp4", "avi" -> "🎬"
            else -> "📎"
        }

        return if (originalMessage.isEmpty() || originalMessage.startsWith(icon)) {
            "$icon $fileName"
        } else {
            originalMessage
        }
    }

    /**
     * Extract file name from URI
     */
    private fun getFileNameFromUri(uri: String): String {
        return when {
            uri.contains("/") -> uri.substringAfterLast('/')
            File(uri).exists() -> File(uri).name
            else -> "attachment"
        }
    }

    /**
     * Validate if attachment URI is accessible
     */
    fun isAttachmentAccessible(attachmentUri: String): Boolean {
        return when {
            isTextContent(attachmentUri) -> true
            File(attachmentUri).exists() -> true
            isValidUrl(attachmentUri) -> true
            else -> false
        }
    }

    /**
     * Get file size for display
     */
    fun getFileSizeDisplay(file: File): String {
        return when {
            !file.exists() -> "Unknown size"
            file.length() < 1024 -> "${file.length()} B"
            file.length() < 1024 * 1024 -> "%.1f KB".format(file.length() / 1024.0)
            else -> "%.1f MB".format(file.length() / (1024.0 * 1024.0))
        }
    }

    /**
     * Clean up temporary files in cache directory
     */
    fun cleanupTempFiles(context: Context, olderThanDays: Int = 7) {
        try {
            val cacheDir = context.cacheDir
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)

            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("quotation_") && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        Log.d(TAG, "Deleted temp file: ${file.name}")
                    } else {
                        Log.w(TAG, "Failed to delete temp file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up temp files: ${e.message}", e)
        }
    }

    /**
     * Show error toast with consistent formatting
     */
    private fun showErrorToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Encode email for Firebase compatibility
     */
    private fun encodeEmail(email: String): String = email.replace(".", ",")

    /**
     * Decode email from Firebase format
     */
    fun decodeEmail(encodedEmail: String): String = encodedEmail.replace(",", ".")
}