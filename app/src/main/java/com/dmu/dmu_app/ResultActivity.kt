package com.dmu.dmu_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dmu.dmu_app.model.SongInfo
import android.content.Intent
import android.widget.Button

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val songInfo = intent.getSerializableExtra("songInfo") as? SongInfo

        val titleView = findViewById<TextView>(R.id.textTitle)
        val artistView = findViewById<TextView>(R.id.textArtist)
        val albumView = findViewById<TextView>(R.id.textAlbum)
        val labelView = findViewById<TextView>(R.id.textLabel)
        val dateView = findViewById<TextView>(R.id.textDate)

        songInfo?.let {
            titleView.text = "제목: ${it.title}"
            artistView.text = "아티스트: ${it.artists}"
            albumView.text = "앨범: ${it.album}"
            labelView.text = "레이블: ${it.label}"
            dateView.text = "발매일: ${it.releaseDate}"

            val backButton = findViewById<Button>(R.id.btnBackToRecord)
            backButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
        }
    }
}