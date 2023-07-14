package com.example.videoplayerapp

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videoplayerapp.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MainActivity : AppCompatActivity(), VideoAdapter.OnItemClickListener {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var videoAdapter: VideoAdapter
    private var videoList: ArrayList<String> = ArrayList()

    private var updatedFile: File? = null

    private val permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionGranted ->
            val grant = permissionGranted.entries.all { it.value }
            if (grant) {
                showContent()
            } else {
                Snackbar.make(
                    binding.root,
                    "The permission is necessary",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        permissionResult.launch(permissions)


    }

    private fun showContent() {
        videoList = fetchVideos()
//        Toast.makeText(this@MainActivity, videoList.toString(), Toast.LENGTH_SHORT).show()
        videoAdapter = VideoAdapter(videoList, this)
        binding.recyclerView.adapter = videoAdapter
    }

    private fun fetchVideos(): ArrayList<String> {
        val columns = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media._ID
        )
        val imagecursor: Cursor = managedQuery(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null,
            null, ""
        )
        for (i in 0 until imagecursor.count) {
            imagecursor.moveToPosition(i)
            val dataColumnIndex =
                imagecursor.getColumnIndex(MediaStore.Video.Media.DATA)
            videoList.add(imagecursor.getString(dataColumnIndex))
        }
        return videoList
    }

    override fun onItemClick(position: Int) {
        showBottomSheetDialog(position)
    }

    private fun renameFileDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Rename File")
        val dialogLayout = inflater.inflate(R.layout.dialog_text, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)
        editText.setText(getVideName(position))
        builder.setView(dialogLayout)
        builder.setPositiveButton("Rename") { dialogInterface, i ->
            if (renameFile(videoList[position], editText.text.toString())) {
                triggerMediaScan(updatedFile!!.absolutePath.toString())
                triggerMediaScan(videoList[position])
                updateVideoList(position)
                videoAdapter.notifyDataSetChanged()
                Toast.makeText(applicationContext, "File is renamed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Not Renamed", Toast.LENGTH_SHORT).show()
            }

        }
        builder.setNegativeButton("Cancel") { dialogInterface, i -> }
        builder.show()
    }

    private fun renameFile(filePath: String, newName: String): Boolean {
        val file = File(filePath)
        val parentDir = file.parentFile
        val extension = getVideoExtension(filePath)
        val newFile = File(parentDir, "$newName$extension")
        updatedFile = newFile
        return file.renameTo(newFile)
    }


    private fun updateVideoList(position: Int) {
        videoList[position] = updatedFile!!.absolutePath.toString()
    }

    private fun triggerMediaScan(filePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val file = File(filePath)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        applicationContext.sendBroadcast(mediaScanIntent)
    }

    private fun getVideName(position: Int): String {
        return videoList[position].substring(
            videoList[position].lastIndexOf('/') + 1,
            videoList[position].lastIndexOf('.')
        )
    }

    private fun getVideoExtension(filePath: String): String {
        return filePath.substring(filePath.lastIndexOf('.'))
    }

    private fun shareVideoToOtherApps(position: Int) {
        val videoUri = Uri.parse(videoList[position])
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri)
        startActivity(Intent.createChooser(shareIntent, "Share video using"))
    }

    private fun deleteSelectedVideo(position: Int) {
        val file = File(videoList[position])
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(applicationContext, "File Deleted", Toast.LENGTH_SHORT).show()
                triggerMediaScan(videoList[position])
                videoList.remove(videoList[position])
                videoAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(applicationContext, "File Not Deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Are you sure you want to delete?")
        dialogBuilder.setPositiveButton("Yes") { dialog, i ->
            deleteSelectedVideo(position)
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("No") { dialog, i ->
            dialog.dismiss()
        }


        val alert = dialogBuilder.create()
        alert.setTitle("Delete Confirmation")
        alert.show()
    }

    private fun showBottomSheetDialog(position: Int) {

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        bottomSheetDialog.show()


        val openVideo = bottomSheetDialog.findViewById<LinearLayout>(R.id.openVideo)
        val renameVideo = bottomSheetDialog.findViewById<LinearLayout>(R.id.renameVideo)
        val shareVideo = bottomSheetDialog.findViewById<LinearLayout>(R.id.shareVideo)
        val deleteVideo = bottomSheetDialog.findViewById<LinearLayout>(R.id.deleteVideo)

        openVideo!!.setOnClickListener {

            bottomSheetDialog.dismiss()
            val intent = Intent(this@MainActivity, VideoActivity::class.java)
            intent.putExtra("video", position)
            intent.putExtra("list", videoList)
            startActivity(intent)
        }

        renameVideo!!.setOnClickListener {

            bottomSheetDialog.dismiss()
            renameFileDialog(position)

        }

        shareVideo!!.setOnClickListener {

            bottomSheetDialog.dismiss()
            shareVideoToOtherApps(position)

        }

        deleteVideo!!.setOnClickListener {

            bottomSheetDialog.dismiss()
            showDeleteConfirmationDialog(position)
        }

    }


}