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

object ChatUtils {
    private const val TAG = "ChatUtils"
    private val storage = FirebaseStorage.getInstance()

    /**
     * Opens an attachment based on its URI or file path
     * Handles both text content and file attachments
     */
    fun openAttachment(context: Context, attachmentUri: String) {
        try {
            // Check if it's text content (quotation text stored directly)
            if (attachmentUri.startsWith("==========") || attachmentUri.length > 500) {
                // It's text content, show in a text viewer or create temp file
                openTextContent(context, attachmentUri)
                return
            }

            // It's a file path or URI
            val file = File(attachmentUri)
            if (file.exists()) {
                openFile(context, file)
            } else {
                // Try as Firebase Storage URL
                if (attachmentUri.startsWith("https://")) {
                    openUrl(context, attachmentUri)
                } else {
                    Toast.makeText(context, "Attachment not found", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "Attachment not found: $attachmentUri")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening attachment: ${e.message}", e)
            Toast.makeText(context, "Error opening attachment: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens a text content attachment
     */
    private fun openTextContent(context: Context, textContent: String) {
        try {
            // Create temp file for viewing
            val tempFile = File(context.cacheDir, "quotation_${System.currentTimeMillis()}.txt")
            tempFile.writeText(textContent)
            openFile(context, tempFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening text content: ${e.message}", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens a file using FileProvider and appropriate intent
     */
    private fun openFile(context: Context, file: File) {
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

            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file: ${e.message}", e)
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens a URL (for Firebase Storage URLs)
     */
    private fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL: ${e.message}", e)
            Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Get MIME type from file extension
     */
    private fun getMimeType(path: String): String {
        val extension = path.substringAfterLast('.', "")
        return when (extension.lowercase()) {
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        }
    }

    /**
     * Upload file to Firebase Storage and return download URL
     * Use this for sharing files between users
     */
    fun uploadAttachment(
        file: File,
        userEmail: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val fileName = "${System.currentTimeMillis()}_${file.name}"
            val storageRef = storage.reference
                .child("chat_attachments")
                .child(encodeEmail(userEmail))
                .child(fileName)

            storageRef.putFile(Uri.fromFile(file))
                .addOnSuccessListener {
                    storageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            onSuccess(uri.toString())
                            Log.d(TAG, "File uploaded successfully: $uri")
                        }
                        .addOnFailureListener { e ->
                            onFailure(e)
                            Log.e(TAG, "Failed to get download URL: ${e.message}", e)
                        }
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                    Log.e(TAG, "Upload failed: ${e.message}", e)
                }
        } catch (e: Exception) {
            onFailure(e)
            Log.e(TAG, "Error uploading file: ${e.message}", e)
        }
    }

    /**
     * Get display text for attachment message
     */
    fun getAttachmentDisplayText(attachmentUri: String, originalMessage: String): String {
        if (attachmentUri.startsWith("==========")) {
            return "📄 Quotation Attachment"
        }

        val extension = attachmentUri.substringAfterLast('.', "").lowercase()
        val icon = when (extension) {
            "pdf" -> "📄"
            "txt" -> "📝"
            "jpg", "jpeg", "png", "gif" -> "🖼️"
            "doc", "docx" -> "📃"
            "xls", "xlsx" -> "📊"
            else -> "📎"
        }

        return if (originalMessage.isEmpty() || originalMessage.startsWith("📄")) {
            "$icon Attachment"
        } else {
            originalMessage
        }
    }

    private fun encodeEmail(email: String): String = email.replace(".", ",")
}
// (GeeksForGeeks, 2024).