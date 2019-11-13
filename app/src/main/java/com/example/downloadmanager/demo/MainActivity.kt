package com.example.downloadmanager.demo

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class MainActivity : AppCompatActivity(),EasyPermissions.PermissionCallbacks, View.OnClickListener{
    override fun onClick(view: View?) {
        when(view){
            btn_cancel->{
                manager.remove(downloadId)
                btn_check_status.isEnabled = false
                btn_check_status.alpha = 0.5F
                Toast.makeText(this, "Download Cancelled", Toast.LENGTH_SHORT).show()

            }
            btn_check_status->{
                var query = DownloadManager.Query()
                query.setFilterById(downloadId)

                var cursor = manager.query(query)

                if(cursor.moveToFirst()){
                    checkStatus(cursor)
                }
            }

            btn_download->{
                val destPath = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}sample.pdf"
                if(EasyPermissions.hasPermissions(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    downloadByDownloadManager("http://androhub.com/demo/demo.pdf", destPath);
            }
        }
    }

    val EXTERNAL_STORAGE_REQUEST_CODE :Int = 24
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
    }

    private var downloadId : Long = 0
    private lateinit var onDownloadComplete:BroadcastReceiver
    lateinit var manager:DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_cancel.isEnabled = false
        btn_cancel.alpha = 0.5F
        btn_check_status.isEnabled = false
        btn_check_status.alpha = 0.5F

        manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        EasyPermissions.requestPermissions(this, "This app needs permission to add pictures to your external storage.", EXTERNAL_STORAGE_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        btn_download.setOnClickListener (this)
        btn_cancel.setOnClickListener (this)
        btn_check_status.setOnClickListener(this)

        var intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

        onDownloadComplete = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intentData: Intent?) {
                var id = intentData?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1)
                if(id == downloadId){
                    btn_cancel.isEnabled = false
                    btn_cancel.alpha = 0.5F
                    Toast.makeText(applicationContext,"File downloaded successfully",Toast.LENGTH_SHORT).show()

                }

            }
        }

        registerReceiver(onDownloadComplete,intentFilter)
    }

    fun downloadByDownloadManager(url: String, outputFileName: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDescription("PDF")
        request.setTitle("Sonam PDF Download")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, outputFileName)

        downloadId = manager.enqueue(request)
        btn_cancel.isEnabled = true
        btn_cancel.alpha = 1F
        btn_check_status.isEnabled = true
        btn_check_status.alpha = 1F
    }

    private fun checkStatus(cursor: Cursor?) {
        //column for status
        val columnIndex= cursor?.getColumnIndex(DownloadManager.COLUMN_STATUS);
        val status = cursor?.getInt(columnIndex!!)
        //column for reason code if the download failed or paused
        val columnReason = cursor?.getColumnIndex(DownloadManager.COLUMN_REASON);
        val reason = cursor?.getInt(columnReason!!)
        //get the download filename
        val filenameIndex = cursor?.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
        var filename = cursor?.getString(filenameIndex!!)?.replace("file://","");

        var statusText = ""
        var reasonText = ""

        when(status){
            DownloadManager.STATUS_FAILED -> {
                statusText = "STATUS_FAILED";
                when (reason) {
                    DownloadManager.ERROR_CANNOT_RESUME ->
                        reasonText = "ERROR_CANNOT_RESUME";

                    DownloadManager.ERROR_DEVICE_NOT_FOUND ->
                        reasonText = "ERROR_DEVICE_NOT_FOUND"

                    DownloadManager.ERROR_FILE_ALREADY_EXISTS ->
                        reasonText = "ERROR_FILE_ALREADY_EXISTS"

                    DownloadManager.ERROR_FILE_ERROR ->
                        reasonText = "ERROR_FILE_ERROR"

                    DownloadManager.ERROR_HTTP_DATA_ERROR ->
                        reasonText = "ERROR_HTTP_DATA_ERROR"

                    DownloadManager.ERROR_INSUFFICIENT_SPACE ->
                        reasonText = "ERROR_INSUFFICIENT_SPACE"

                    DownloadManager.ERROR_TOO_MANY_REDIRECTS ->
                        reasonText = "ERROR_TOO_MANY_REDIRECTS"

                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE ->
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE"

                    DownloadManager.ERROR_UNKNOWN ->
                        reasonText = "ERROR_UNKNOWN"

                }
            }

            DownloadManager.STATUS_PAUSED -> {
                statusText = "STATUS_PAUSED";
                when (reason) {
                    DownloadManager.PAUSED_QUEUED_FOR_WIFI ->
                        reasonText = "PAUSED_QUEUED_FOR_WIFI"

                    DownloadManager.PAUSED_UNKNOWN ->
                        reasonText = "PAUSED_UNKNOWN";

                    DownloadManager.PAUSED_WAITING_FOR_NETWORK ->
                        reasonText = "PAUSED_WAITING_FOR_NETWORK"

                    DownloadManager.PAUSED_WAITING_TO_RETRY ->
                        reasonText = "PAUSED_WAITING_TO_RETRY";

                }
            }

            DownloadManager.STATUS_PENDING ->
                statusText = "STATUS_PENDING"

            DownloadManager.STATUS_RUNNING ->
                statusText = "STATUS_RUNNING"

            DownloadManager.STATUS_SUCCESSFUL -> {
                statusText = "STATUS_SUCCESSFUL"
                reasonText = "Filename:\n" + filename;
            }
        }

        if(TextUtils.isEmpty(reasonText))
            reasonText = "Error: ${reason.toString()}"


        var toast = Toast.makeText(this,
        statusText + "\n" +
                reasonText,
        Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 25, 400);
        toast.show();

    }


}
