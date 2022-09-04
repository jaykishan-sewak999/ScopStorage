package com.jk.scopedstorage

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jk.scopedstorage.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    //If this image not exist then please replace with any other URL
    private val IMAGE_URL =
        "https://i.picsum.photos/id/737/200/200.jpg?hmac=YPktyFzukhcmeW3VgULbam5iZTWOMXfwf6WIBPpJD50"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadImageInGlide(IMAGE_URL, binding.ivUrlImage)
        binding.btnMediaStore.setOnClickListener {
            storeFileInDownloadFolder()


        }
    }

    private fun loadImageInGlide(imageUrl: String?, imageView: AppCompatImageView) {
        Glide.with(this).load(imageUrl).into(imageView)
    }

    private fun storeFileInDownloadFolder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                System.currentTimeMillis().toString()
            )
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + File.separator + "JKIMAGE"
            )

            val uri: Uri? =
                resolver.insert(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                    contentValues
                )
            uri?.let {
                val path = FileUriUtils.getRealPath(this, uri)
                val newFile = File(path)
                storeFileOnGivenPath(newFile)

            } ?: run { showToast() }

        }
        else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
            ) {
             getDownloadLocationAndStoreFile()
            } else {
                requestPermission()

            }
        }
    }

    private fun getDownloadLocationAndStoreFile() {
        val downloadDir =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + "JKIMAGE")
        if (downloadDir.exists().not()) {
            if (downloadDir.mkdirs()) {
                val newFile = File(downloadDir.absolutePath + File.separator+System.currentTimeMillis().toString()+".jpg")
                storeFileOnGivenPath(newFile)

            } else showToast()
        }else{
            val newFile = File(downloadDir.absolutePath + File.separator+System.currentTimeMillis().toString()+".jpg")
            storeFileOnGivenPath(newFile)
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var count = 0
            var totalPermission = 0
            val rejectPermission = arrayListOf<String>()
            for (entry in result.entries) {
                totalPermission++
                val permissionName = entry.key
                if (entry.value) {
                    count++
                    if (count == result.entries.size) {
                        getDownloadLocationAndStoreFile()
                    }
                } else {
                    rejectPermission.add(permissionName)
                    if (totalPermission == result.entries.size) {
                        showDialog()
                    }
                }
            }
        }

    private fun showDialog() {
        MaterialAlertDialogBuilder(this, 0)
            .apply {
                setTitle("Permission Required")
                setMessage("Please accept permission to let us save file on your device.")
                setPositiveButton("Ok") { _, _ ->
                    Toast.makeText(this@MainActivity, "Please try again", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this@MainActivity, "Please try again", Toast.LENGTH_SHORT).show()
                }
                setCancelable(false)
                create()
                show()
            }
    }
    private fun showToast() {
        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
    }

    private fun storeFileOnGivenPath(newFile: File?) {
        GlobalScope.launch(Dispatchers.IO) {
            URL(IMAGE_URL).openStream().use {
                Channels.newChannel(it).use { rbc ->
                    FileOutputStream(newFile).use { fos ->
                        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                    }
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            Glide.with(this)
                .load(newFile)
                .apply(RequestOptions().override(100))
                .into(binding.ivLocalImage)
            binding.tvStoredPath.text = "File store at:".plus(newFile?.absolutePath)
        },1000)
    }


}
