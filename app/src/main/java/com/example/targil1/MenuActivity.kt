package com.example.targil1





import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import dev.tomco.a25b_11345a_l06.ui.HighScoreFragment

class MenuActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        ScoreManager.init(this)

        findViewById<Button>(R.id.btnSensorMode).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("MODE", "SENSOR")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSlowMode).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("MODE", "SLOW")
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnScores).setOnClickListener {
            val intent = Intent(this, ScoreboardActivity::class.java)
            startActivity(intent)

        }



        findViewById<Button>(R.id.btnFastMode).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("MODE", "FAST")
            startActivity(intent)
        }
    }
}
