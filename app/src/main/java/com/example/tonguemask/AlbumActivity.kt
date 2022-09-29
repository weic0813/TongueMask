package com.example.tonguemask

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuCompat
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.dialog_item.view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.*
import java.net.Socket
import java.net.UnknownHostException

class AlbumActivity : AppCompatActivity() {

    val PERMISSION_ALBUM = 300
    var imageUri: Uri? = null
    var path1: String = ""
    var filePath: String? = null
    var bmp: Bitmap? = null
    val TAG = javaClass.simpleName
    var result_data: ByteArray? = null
    var result_msg: String? = ""
    var sendfinish:Int = 0

    private companion object {
        val PHOTO_FROM_GALLERY = 50
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
                PERMISSION_ALBUM
            )
        } else {
            toAlbum()
        }
    }

    fun toAlbum() {
        val album = Intent(Intent.ACTION_GET_CONTENT)
        album.setType("image/*")
        startActivityForResult(album, PHOTO_FROM_GALLERY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (imageUri != null) {
            val uriString = imageUri.toString()
            outState.putString("saveUri", uriString)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PHOTO_FROM_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val uri = data!!.data
                        Log.d(TAG, "URI: ${uri}")
                        //抽象資料的接口
                        val cr = this.contentResolver
                        //由抽象資料接口轉換圖檔路徑為Bitmap
                        val bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri!!))
                        val uriPathHelper = URIPathHelper()
                        filePath = uriPathHelper.getPath(this, uri)
                        //Log.d(TAG,"filepath:${filePath}")
                        path1 = filePath.toString()
                        Log.d(TAG, "path1:${path1}")
                        imageView.setImageBitmap(bitmap)
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.wtf("getImageResult", resultCode.toString())
                    }
                }
            }
        }
    }

    fun upload_img(view: View) {
        val view = LayoutInflater.from(this@AlbumActivity).inflate(R.layout.dialog_item, null)
        send()
        showView(view)
        AlertDialog
            .Builder(this@AlbumActivity)
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
            }
            .show()
    }

    private fun showView(view: View) {
        view.progress.visibility = VISIBLE
        Thread {
            for (i in 1..100) {
                if(sendfinish == 1){
                    break
                }
                Thread.sleep(500)
                runOnUiThread {
                    view.progress_msg.text = "processing"
                    view.progress.progress = i
                }
            }
            runOnUiThread {
                view.progress_msg.text = "finish"
                view.progress.visibility = GONE
            }
            val result = Intent(this@AlbumActivity, ResultActivity::class.java)
            result.putExtra("Result_image_alb", result_data)
            result.putExtra("Result_msg_alb", result_msg)
            startActivity(result)
        }.start()
    }

    fun send() {
        Thread {
            try {
                val socket = Socket("120.113.173.217", 5050)
                val out = DataOutputStream(socket.getOutputStream())
//              Log.d(TAG,"filepath:${path1}")
                val fis: InputStream = FileInputStream(path1)
                //發送圖片大小
                var size: Int = fis.available()
                var s: String = size.toString()
                while (s.length < 10) {
                    s = s + " "
                }
                var bytes: ByteArray = s.toByteArray()
                out.write(bytes)
                out.flush()
                //發送圖片
                //讀取圖片到ByteArrayOutputStream
                var sendBytes: ByteArray = ByteArray(1024)
                var length: Int = 0

                while (fis.read(sendBytes, 0, sendBytes.size).also { length = it } > 0) {
                    out.write(sendBytes, 0, length)
                    out.flush()
                }
                fis.close()
                //準備讀入
                //接收圖片
                val dataInput = DataInputStream(socket.getInputStream())
                val br: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val num: CharArray = CharArray(10)
                var message: Int
                message = br.read(num, 0, 10)
                s = String(num).trim { it <= ' ' }
                size = s.toInt()
                result_data = ByteArray(size)
                var len: Int = 0
                while (true) {
                    if (size == len) {
                        break
                    }
                    if (size - len <= 8192) {
                        message = dataInput.read(result_data, len, size - len)
                    } else {
                        message = dataInput.read(result_data, len, 8192)
                    }
                    len += message
                }
                bmp = BitmapFactory.decodeByteArray(result_data, 0, result_data!!.size)
                Log.d(TAG, "BMP_alb: ${bmp}")
                //接收訊息
                val br_msg = BufferedReader(InputStreamReader(socket.getInputStream()))
                val inMessage = CharArray(30)
                val a = br_msg.read(inMessage)
                result_msg = String(inMessage, 0, a) //用string的構造方法來轉換字符數组为字符串
                Log.d(TAG, "Result_MSG: ${result_msg}")

                out.close()
                dataInput.close()
                socket.close()
                //mhandler.sendEmptyMessage(0)
                sendfinish = 1
            } catch (e: UnknownHostException) {
                Toast.makeText(this@AlbumActivity, "連接失敗", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    class URIPathHelper {
        fun getPath(context: Context, uri: Uri): String? {
            val isKitKatorAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            // DocumentProvider
            if (isKitKatorAbove && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return getDataColumn(context, contentUri!!, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        fun getDataColumn(
            context: Context,
            uri: Uri,
            selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = context.getContentResolver()
                    .query(uri, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } finally {
                if (cursor != null) cursor.close()
            }
            return null
        }

        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }
    }

    override fun onCreateOptionsMenu(menu2: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu2,menu2)
        MenuCompat.setGroupDividerEnabled(menu2,true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.back->{
                val back = Intent(this, MainActivity::class.java)
                back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(back)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}