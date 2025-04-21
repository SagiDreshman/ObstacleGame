package com.example.targil1

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import dev.tomco.a25b_11345a_l05.utilities.SignalManager
import kotlin.random.Random

class MainActivity : Activity() {

    private lateinit var gameLayout: FrameLayout
    private lateinit var player: ImageView
    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView

    private val numRows = 6
    private val numCols = 3
    private val matrix = Array(numRows) { arrayOfNulls<ImageView>(numCols) }

    private var playerCol = 1
    private var lives = 3
    private val handler = Handler(Looper.getMainLooper())
    private val obstacleDelay: Long = 800

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SignalManager
        SignalManager.init(this)

        gameLayout = findViewById(R.id.gameLayout)
        player     = findViewById(R.id.player)
        btnLeft    = findViewById(R.id.btnLeft)
        btnRight   = findViewById(R.id.btnRight)
        heart1     = findViewById(R.id.heart1)
        heart2     = findViewById(R.id.heart2)
        heart3     = findViewById(R.id.heart3)

        player.post {
            placePlayer()
            startGameLoop()
        }

        btnLeft.setOnClickListener {
            if (playerCol > 0 && lives > 0) {
                playerCol--
                placePlayer()
            }
        }

        btnRight.setOnClickListener {
            if (playerCol < numCols - 1 && lives > 0) {
                playerCol++
                placePlayer()
            }
        }
    }

    private fun placePlayer() {
        val laneWidth = gameLayout.width / numCols
        val cellHeight = gameLayout.height / numRows
        val bottomY = gameLayout.height - player.height
        val yOffset = cellHeight / 2
        val yPos = bottomY - yOffset
        val xPos = laneWidth * playerCol + laneWidth / 2 - player.width / 2
        player.x = xPos.toFloat()
        player.y = yPos.toFloat()
    }

    private fun startGameLoop() {
        handler.post(obstacleRunnable)
    }

    private val obstacleRunnable = object : Runnable {
        override fun run() {
            moveObstaclesDown()
            spawnNewObstacle()
            handler.postDelayed(this, obstacleDelay)
        }
    }

    private fun moveObstaclesDown() {
        for (row in numRows - 1 downTo 0) {
            for (col in 0 until numCols) {
                val current = matrix[row][col]
                if (current != null) {
                    if (row == numRows - 1) {
                        if (col == playerCol) {
                            loseLife()
                        }
                        gameLayout.removeView(current)
                        matrix[row][col] = null
                    } else {
                        matrix[row + 1][col] = current
                        matrix[row][col] = null
                        current.translationY += gameLayout.height / numRows
                    }
                }
            }
        }
    }

    private fun spawnNewObstacle() {
        if (lives <= 0) return
        val col = Random.nextInt(0, numCols)
        val view = ImageView(this)
        view.setImageResource(R.drawable.obstacle)
        val size = player.width
        val laneWidth = gameLayout.width / numCols
        val xPos = laneWidth * col + laneWidth / 2 - size / 2
        view.layoutParams = FrameLayout.LayoutParams(size, size)
        view.x = xPos.toFloat()
        view.y = 0f
        matrix[0][col] = view
        gameLayout.addView(view)
    }

    private fun loseLife() {
        lives--

        when (lives) {
            2 -> {
                heart3.visibility = ImageView.GONE
                SignalManager.getInstance().vibrate()
                SignalManager.getInstance().toast("You lost a life")
            }
            1 -> {
                heart2.visibility = ImageView.GONE
                SignalManager.getInstance().vibrate()
                SignalManager.getInstance().toast("You lost a life")
            }
            0 -> {
                heart1.visibility = ImageView.GONE
                handler.removeCallbacks(obstacleRunnable)
                btnLeft.isEnabled = false
                btnRight.isEnabled = false
                SignalManager.getInstance().vibrate()
                SignalManager.getInstance().toast("Game Over")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(obstacleRunnable)
    }
}
