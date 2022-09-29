package com.example.tonguemask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuCompat

class MainActivity : AppCompatActivity() {
    companion object{
        val REQUEST_ALBUM = 50
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun camera_on (view: View){
        val cameraX = Intent(this,CameraXActivity::class.java)
        startActivity(cameraX)
    }

    fun to_album(view: View){
        val album = Intent(this,AlbumActivity::class.java)
        startActivityForResult(album, REQUEST_ALBUM)
    }

    override fun onCreateOptionsMenu(menu3: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu3,menu3)
        MenuCompat.setGroupDividerEnabled(menu3,true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.info->{
                AlertDialog
                    .Builder(this)
                    .setTitle("操作說明")
                    .setMessage("""
                        使用相機拍攝時請將舌頭對準於邊框內。                       

                        本系統之辨識結果不具正式醫療診斷效力。
                    """.trimIndent())
                    .setPositiveButton("OK") {_, _ ->
                    }
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}