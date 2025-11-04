package vcmsa.projects.fkj_consultants.extensions

import java.io.File

/**
 * Extension functions for File operations
 * Provides utility methods for file type detection, validation, and formatting
 */

/**
 * Safely check if file exists and is readable
 */
fun File.isReadableFile(): Boolean {
    return this.exists() && this.isFile && this.canRead()
}

/**
 * Safely check if file exists and is writable
 */
fun File.isWritableFile(): Boolean {
    return this.exists() && this.isFile && this.canWrite()
}

/**
 * Get file extension in lowercase
 */
fun File.getExtension(): String {
    return this.name.substringAfterLast('.', "").lowercase()
}

/**
 * Get file name without extension
 */
fun File.getNameWithoutExtension(): String {
    return this.name.substringBeforeLast('.', this.name)
}

/**
 * Check if file is an image
 */
fun File.isImage(): Boolean {
    val extension = this.getExtension()
    return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico")
}

/**
 * Check if file is a document
 */
fun File.isDocument(): Boolean {
    val extension = this.getExtension()
    return extension in listOf("pdf", "doc", "docx", "txt", "rtf", "odt", "md")
}

/**
 * Check if file is a spreadsheet
 */
fun File.isSpreadsheet(): Boolean {
    val extension = this.getExtension()
    return extension in listOf("xls", "xlsx", "csv", "ods")
}

/**
 * Check if file is a presentation
 */
fun File.isPresentation(): Boolean {
    val extension = this.getExtension()
    return extension in listOf("ppt", "pptx", "odp")
}

/**
 * Check if file is an archive
 */
fun File.isArchive(): Boolean {
    val extension = this.getExtension()
    return extension in listOf("zip", "rar", "7z", "tar", "gz", "jar")
}

/**
 * Check if file is audio
 */
fun File.isAudio(): Boolean {
    val extension = this.getExtension()
    return extension in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a", "wma")
}

/**
 * Check if file is video
 */
fun File.isVideo(): Boolean {
    val extension = this.getExtension()
    return extension in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v")
}

/**
 * Get human-readable file size
 */
fun File.getHumanReadableSize(): String {
    val bytes = this.length()
    return when {
        bytes <= 0 -> "0 B"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

/**
 * Get file creation date as formatted string
 */
fun File.getFormattedCreationDate(): String {
    return try {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        formatter.format(java.util.Date(this.lastModified()))
    } catch (e: Exception) {
        "Unknown date"
    }
}

/**
 * Get file modification date as formatted string
 */
fun File.getFormattedModificationDate(): String {
    return try {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        formatter.format(java.util.Date(this.lastModified()))
    } catch (e: Exception) {
        "Unknown date"
    }
}

/**
 * Check if file is older than specified days
 */
fun File.isOlderThan(days: Int): Boolean {
    val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
    return this.lastModified() < cutoff
}

/**
 * Check if file is newer than specified days
 */
fun File.isNewerThan(days: Int): Boolean {
    val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
    return this.lastModified() > cutoff
}

/**
 * Get file MIME type based on extension
 */
fun File.getMimeType(): String {
    val extension = this.getExtension()
    return when (extension) {
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
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
        else -> "application/octet-stream"
    }
}

/**
 * Get appropriate icon resource ID for file type (you'll need to create these drawables)
 */
fun File.getIconResourceId(): Int {
    val extension = this.getExtension()
    return when {
        this.isImage() -> android.R.drawable.ic_menu_gallery // Replace with your own drawable
        this.isDocument() -> android.R.drawable.ic_menu_edit // Replace with your own drawable
        this.isSpreadsheet() -> android.R.drawable.ic_menu_agenda // Replace with your own drawable
        this.isPresentation() -> android.R.drawable.ic_menu_slideshow // Replace with your own drawable
        this.isAudio() -> android.R.drawable.ic_media_play // Replace with your own drawable
        this.isVideo() -> android.R.drawable.ic_media_play // Replace with your own drawable
        this.isArchive() -> android.R.drawable.ic_menu_more // Replace with your own drawable
        else -> android.R.drawable.ic_menu_save // Replace with your own drawable
    }
}

/**
 * Safely delete file with error handling
 */
fun File.safeDelete(): Boolean {
    return try {
        if (this.exists()) {
            this.delete()
        } else {
            true
        }
    } catch (e: Exception) {
        false
    }
}

/**
 * Create a copy of the file in specified directory
 */
fun File.copyToDirectory(destinationDir: File, newName: String? = null): File? {
    return try {
        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }

        val destFile = File(destinationDir, newName ?: this.name)
        this.copyTo(destFile, overwrite = true)
        destFile
    } catch (e: Exception) {
        null
    }
}

/**
 * Check if file size is within limit (in MB)
 */
fun File.isWithinSizeLimit(maxSizeMB: Double): Boolean {
    val sizeInMB = this.length() / (1024.0 * 1024.0)
    return sizeInMB <= maxSizeMB
}

/**
 * Get file size in MB
 */
fun File.getSizeInMB(): Double {
    return this.length() / (1024.0 * 1024.0)
}

/**
 * Get file size in KB
 */
fun File.getSizeInKB(): Double {
    return this.length() / 1024.0
}

/**
 * Check if file has valid image dimensions (basic check)
 */
fun File.hasValidImageDimensions(maxWidth: Int = 5000, maxHeight: Int = 5000): Boolean {
    return try {
        // This is a basic implementation - you might want to use BitmapFactory for actual dimensions
        this.isImage() && this.length() > 0 && this.length() < 50 * 1024 * 1024 // 50MB max for images
    } catch (e: Exception) {
        false
    }
}

/**
 * Get file permissions string (e.g., "rw-r--r--")
 */
fun File.getPermissionsString(): String {
    return buildString {
        append(if (this@getPermissionsString.canRead()) "r" else "-")
        append(if (this@getPermissionsString.canWrite()) "w" else "-")
        append(if (this@getPermissionsString.canExecute()) "x" else "-")
    }
}

/**
 * Extension for List<File> to get total size
 */
fun List<File>.getTotalSize(): Long {
    return this.sumOf { it.length() }
}

/**
 * Extension for List<File> to get human readable total size
 */
fun List<File>.getHumanReadableTotalSize(): String {
    val totalBytes = this.getTotalSize()
    return when {
        totalBytes <= 0 -> "0 B"
        totalBytes < 1024 -> "$totalBytes B"
        totalBytes < 1024 * 1024 -> "%.1f KB".format(totalBytes / 1024.0)
        totalBytes < 1024 * 1024 * 1024 -> "%.1f MB".format(totalBytes / (1024.0 * 1024.0))
        else -> "%.1f GB".format(totalBytes / (1024.0 * 1024.0 * 1024.0))
    }
}

/**
 * Extension for List<File> to filter by type
 */
fun List<File>.filterImages(): List<File> = this.filter { it.isImage() }

fun List<File>.filterDocuments(): List<File> = this.filter { it.isDocument() }

fun List<File>.filterSpreadsheets(): List<File> = this.filter { it.isSpreadsheet() }

fun List<File>.filterPresentations(): List<File> = this.filter { it.isPresentation() }

fun List<File>.filterAudio(): List<File> = this.filter { it.isAudio() }

fun List<File>.filterVideo(): List<File> = this.filter { it.isVideo() }

fun List<File>.filterArchives(): List<File> = this.filter { it.isArchive() }

/**
 * Extension for List<File> to sort by different criteria
 */
fun List<File>.sortByName(ascending: Boolean = true): List<File> {
    return if (ascending) this.sortedBy { it.name } else this.sortedByDescending { it.name }
}

fun List<File>.sortBySize(ascending: Boolean = true): List<File> {
    return if (ascending) this.sortedBy { it.length() } else this.sortedByDescending { it.length() }
}

fun List<File>.sortByDate(ascending: Boolean = true): List<File> {
    return if (ascending) this.sortedBy { it.lastModified() } else this.sortedByDescending { it.lastModified() }
}

fun List<File>.sortByType(): List<File> {
    return this.sortedWith(compareBy<File> { it.getExtension() }.thenBy { it.name })
}