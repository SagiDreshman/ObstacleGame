package com.example.targil1

import android.os.Bundle
import dev.tomco.a25b_11345a_l06.ui.MapFragment
import dev.tomco.a25b_11345a_l06.ui.HighScoreFragment
import androidx.appcompat.app.AppCompatActivity
import com.example.targil1.R



class ScoreboardActivity : AppCompatActivity(), Callback_HighScoreItemClicked {

    private lateinit var mapFragment: MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoreboard)

        val highScoreFragment = HighScoreFragment()
        mapFragment = MapFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.highScoreContainer, highScoreFragment)
            .replace(R.id.mapContainer, mapFragment)
            .commit()
    }

    override fun highScoreItemClicked(lat: Double, lon: Double) {
        mapFragment.zoom(lat, lon)
    }
}
