package com.example.tonguemask

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import kotlinx.android.synthetic.main.activity_result.*
import java.io.IOException
import android.widget.Toast

import androidx.core.content.FileProvider
import androidx.core.util.ObjectsCompat.requireNonNull
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*


class ResultActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName
    var bitmap : Bitmap? = null
    var str : String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        GetPermission()
        if (intent.hasExtra("Result_image_cam")){
            //convert to bitmap
            val byteArray = intent.getByteArrayExtra("Result_image_cam")
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
            user_image.setImageBitmap(bitmap)
            //處理msg
            str = intent.getStringExtra("Result_msg_cam")
            var strlen : String? = ""
            for ( i in 0 .. 2) {
                strlen += str!![i]
            }
            var count = strlen!!.toInt()
            var true_msg = str!!.substring(3)
            var value_str : String? = ""
            var mode = '0'
            for( i in 0 .. count-1){
                if(true_msg[i] == 'n'){
                    mode = 'n'
                }
                else if (true_msg[i] == 'w'){
                    mode = 'w'
                }
                else if (true_msg[i] == 'g'){
                    mode = 'g'
                }
                else if (true_msg[i] == 'y'){
                    mode = 'y'
                }
                else if ((true_msg[i] >= '0' && true_msg[i] <= '9') || true_msg[i] == '.')
                {
                    value_str += true_msg[i]
                }
                else{
                    var value = value_str!!.toDouble()
                    if(mode == 'n'){
                        normal_val.text = value.toString()+'%'
                    }
                    else if (mode == 'w'){
                        white_val.text = value.toString()+'%'
                    }
                    else if (mode == 'g'){
                        gray_val.text = value.toString()+'%'
                    }
                    else if (mode == 'y'){
                        yellow_val.text = value.toString()+'%'
                    }
                    value_str = ""
                }
            }
        }

        else if (intent.hasExtra("Result_image_alb")){
            //convert to bitmap
            val byteArray = intent.getByteArrayExtra("Result_image_alb")
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
            user_image.setImageBitmap(bitmap)
            //處理msg
            str = intent.getStringExtra("Result_msg_alb")
            var strlen : String? = ""
            for ( i in 0 .. 2) {
                strlen += str!![i]
            }
            var count = strlen!!.toInt()
            var true_msg = str!!.substring(3)
            var value_str : String? = ""
            var mode = '0'
            for( i in 0 .. count-1){
                if(true_msg[i] == 'n'){
                    mode = 'n'
                }
                else if (true_msg[i] == 'w'){
                    mode = 'w'
                }
                else if (true_msg[i] == 'g'){
                    mode = 'g'
                }
                else if (true_msg[i] == 'y'){
                    mode = 'y'
                }
                else if ((true_msg[i] >= '0' && true_msg[i] <= '9') || true_msg[i] == '.')
                {
                    value_str += true_msg[i]
                }
                else{
                    var value = value_str!!.toDouble()
                    if(mode == 'n'){
                        normal_val.text = value.toString()+'%'
                    }
                    else if (mode == 'w'){
                        white_val.text = value.toString()+'%'
                    }
                    else if (mode == 'g'){
                        gray_val.text = value.toString()+'%'
                    }
                    else if (mode == 'y'){
                        yellow_val.text = value.toString()+'%'
                    }
                    value_str = ""
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu,menu)
        MenuCompat.setGroupDividerEnabled(menu,true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.tongue_help -> {
                //Toast.makeText(this,"舌苔顏色說明",Toast.LENGTH_LONG).show()
            }
            R.id.tongue_help_normal -> {
                AlertDialog
                    .Builder(this)
                    .setTitle("正常苔")
                    .setMessage("無舌苔 -> 屬正常舌苔，舌體柔軟，活動自如，顏色淡紅。")
                    .setPositiveButton("OK") {_, _ ->
                    }
                    .show()
            }
            R.id.tongue_help_white-> {
                AlertDialog
                    .Builder(this)
                    .setTitle("白苔")
                    .setMessage("""
                        薄白苔 -> 舌面鋪有薄薄的乾濕適中的白苔。屬素體氣虛，風寒外襲。主要由於口腔與唾液的綜合作用，使舌黏膜的物質與口腔上皮不斷被清除脫落，使舌苔僅有薄白一層。
                        
                        厚白濕苔 -> 苔色白而厚且濕膩，可布滿全舌。屬主濕、主痰、主寒。多見於中焦脾胃的陽氣不振，以致飲食停滯，或為濕濁痰積之候。
                        
                        厚白燥裂苔 -> 白苔和燥裂兩者亦可同現於舌面。屬內熱暴起。多見於急性溫熱病，燥熱暴起，常見於暑溫，暑熱傷氣。
                    """.trimIndent())
                    .setPositiveButton("OK") {_, _ ->
                    }
                    .show()
            }
            R.id.tongue_help_yellow -> {
                AlertDialog
                    .Builder(this)
                    .setTitle("黃苔")
                    .setMessage("""
                        薄黃苔 -> 舌面鋪有薄薄的黃苔。淡黃屬為熱輕；深黃屬為熱重；焦黃屬為熱結。多見於風熱在表或風寒化熱。 

                        厚黃濕苔 -> 舌面苔色黃而粘膩，顆粒緊密膠粘。屬苔黃為熱，苔膩為濕、痰。多見於邪熱與痰涎濕濁交結而形成。                        

                        厚黃燥裂苔 -> 黃苔和燥裂兩者亦可同現於舌面。屬邪熱傷津之病變，氣分熱盛。多見於體溫升高、炎症感染、消化道功能紊亂。
                    """.trimIndent())
                    .setPositiveButton("OK") {_, _ ->
                    }
                    .show()
            }
            R.id.tongue_help_gray -> {
                AlertDialog
                    .Builder(this)
                    .setTitle("灰黑或不確定")
                    .setMessage("""
                        被分為此類不代表舌苔為灰黑苔，會因環境燈光過於灰暗或因被其他特殊燈光影響，都會被分為此類。                        

                        灰黑苔 -> 舌上苔色呈現灰中帶黑者，苔灰主病略輕，苔黑主病較重，隨病情發展與轉歸，兩者又密切相關。屬熱極傷陰，陽虚陰盛或腎陰虧損，痰濕久鬱化熱。多見於白苔或黃苔轉化而成可能會為寒證或微熱證，但灰黑為病情均較嚴重。
                    """.trimIndent())
                    .setPositiveButton("OK") {_, _ ->
                    }
                    .show()
            }
            R.id.share -> {
                val draw = this.user_image.drawable as BitmapDrawable
                val bitmap = draw.bitmap
                shareImageandText(bitmap)
            }
            R.id.store -> {
                val bitmap = user_image.drawToBitmap()
                saveImageToGallery(bitmap)
            }
            R.id.return_home -> {
                val return_home = Intent(this, MainActivity::class.java)
                return_home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(return_home)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun GetPermission()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            val REQUEST_CODE_CONTACT = 101
            val permissions = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            //驗證權限
            for (str in permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申請權限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT)
                }
            }
        }
    }

    fun shareImageandText(bitmap: Bitmap) {
        val uri: Uri? = getImageToShare(bitmap)
        val intent = Intent(Intent.ACTION_SEND)

        // putting uri of image to be shared
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        // setting type to image
        intent.type = "image/jpg"

        // calling startactivity() to share
        startActivity(Intent.createChooser(intent, "分享至"))
    }

    fun getImageToShare(bitmap: Bitmap): Uri? {
        val imagefolder = File(cacheDir, "images")
        var uri: Uri? = null
        try {
            imagefolder.mkdirs()
            val file = File(imagefolder, "shared_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            uri = FileProvider.getUriForFile(this, "com.example.tonguemask", file)
        } catch (e: Exception) {
            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
        }
        return uri
    }

    fun saveImageToGallery(bitmap: Bitmap){
        val store_image : OutputStream
        try{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                val resolver = contentResolver
                val  contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"Image"+".jpg")
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DCIM+File.separator+"Camera")
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
                store_image = resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)!!
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,store_image)
                Objects.requireNonNull<OutputStream?>(store_image)
                Toast.makeText(this,"Image Saved",Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            Toast.makeText(this,"Image Not Saved",Toast.LENGTH_SHORT).show()
        }
    }
}