package com.jk.scopedstorage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.StatFs
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {

    /**
     * Get Image File
     *
     * Default it will take Camera folder as it's directory
     *
     * @param fileDir File Folder in which file needs tobe created.
     * @param extension String Image file extension.
     * @return Return Empty file to store camera image.
     * @throws IOException if permission denied of failed to create new file.
     */
    fun getImageFile(fileDir: File, extension: String? = null): File? {
        try {
            // Create an image file name
            val ext = extension ?: ".jpg"
            val fileName = getFileName()
            val imageFileName = "$fileName$ext"

            // Create Directory If not exist
            if (!fileDir.exists()) fileDir.mkdirs()

            // Create File Object
            val file = File(fileDir, imageFileName)

            // Create empty file
            file.createNewFile()

            return file
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    private fun getFileName() = "IMG_${getTimestamp()}"
    // private fun getFileName() = "IMAGE_PICKER"

    /**
     * Get Current Time in yyyyMMdd HHmmssSSS format
     *
     * 2019/01/30 10:30:20 000
     * E.g. 20190130_103020000
     */
    private fun getTimestamp(): String {
        val timeFormat = "yyyyMMdd_HHmmssSSS"
        return SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
    }


    /**
     * Get DocumentFile from Uri
     *
     * @param context Application Context
     * @param uri Source Uri
     * @return DocumentFile return DocumentFile from Uri
     */
    fun getDocumentFile(context: Context, uri: Uri): DocumentFile? {
        var file: DocumentFile? = null
        if (isFileUri(uri)) {
            val path = FileUriUtils.getRealPath(context, uri)
            if (path != null) {
                file = DocumentFile.fromFile(File(path))
            }
        } else {
            file = DocumentFile.fromSingleUri(context, uri)
        }
        return file
    }

    /**
     * Get Image Extension i.e. .png, .jpg
     *
     * @return extension of image with dot, or default .jpg if it none.
     */
    fun getImageExtension(uriImage: Uri): String {
        var extension: String? = null

        try {
            val imagePath = uriImage.path
            if (imagePath != null && imagePath.lastIndexOf(".") != -1) {
                extension = imagePath.substring(imagePath.lastIndexOf(".") + 1)
            }
        } catch (e: Exception) {
            extension = null
        }

        if (extension == null || extension.isEmpty()) {
            // default extension for matches the previous behavior of the plugin
            extension = "jpg"
        }

        return ".$extension"
    }

    /**
     * Check if provided URI is backed by File
     *
     * @return Boolean, True if Uri is local file object else return false
     */
    private fun isFileUri(uri: Uri): Boolean {
        return "file".equals(uri.scheme, ignoreCase = true)
    }
}
