package com.nicha.eventticketing.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for image picking functionality
 */
object ImagePickerUtil {
    
    // Lưu URI tạm thời cho camera
    var tempImageUri: Uri? = null
    
    /**
     * Remember a launcher for image picking
     * @param onImageSelected Callback when an image is selected
     * @return Launcher for image picking
     */
    @Composable
    fun rememberImagePicker(
        onImageSelected: (File) -> Unit
    ): ManagedActivityResultLauncher<Intent, ActivityResult> {
        val context = androidx.compose.ui.platform.LocalContext.current
        
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Convert URI to File
                    val file = createTempFileFromUri(context, uri)
                    file?.let { onImageSelected(it) }
                }
            }
        }
    }
    
    /**
     * Launch the image picker
     * @param launcher The launcher to use
     */
    fun launchImagePicker(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        launcher.launch(intent)
    }
    
    /**
     * Create a temporary file from a URI
     * @param context The context
     * @param uri The URI
     * @return The temporary file
     */
    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            // Create a temporary file
            val tempFile = File.createTempFile("image_", ".jpg", context.cacheDir)
            
            // Copy the content from the URI to the temporary file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Tạo URI cho ảnh mới từ camera
     */
    fun createImageUri(context: Context): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert Uri to File
     */
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 