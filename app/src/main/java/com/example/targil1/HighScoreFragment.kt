package dev.tomco.a25b_11345a_l06.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.targil1.Callback_HighScoreItemClicked
import com.example.targil1.R
import com.example.targil1.ScoreManager

class HighScoreFragment : Fragment() {

    private lateinit var container: LinearLayout
    private var callback: Callback_HighScoreItemClicked? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callback_HighScoreItemClicked) {
            callback = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_high_score, container, false)
        this.container = view.findViewById(R.id.highScore_container)
        showScores()
        return view
    }

    private fun showScores() {
        val scores = ScoreManager.getScores()

        for ((index, score) in scores.withIndex()) {
            val textView = TextView(requireContext()).apply {
                text = "${index + 1}. שם: ${score.name} | מרחק: ${score.distance} מטר | מטבעות: ${score.coins}"
                textSize = 16f
                setPadding(16, 8, 16, 8)

                // ✅ הקריאה לממשק בלחיצה
                setOnClickListener {
                    callback?.highScoreItemClicked(score.lat, score.lng)
                }
            }
            container.addView(textView)
        }
    }


}

