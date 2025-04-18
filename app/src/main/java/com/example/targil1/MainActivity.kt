package com.example.targil1

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import kotlin.random.Random
import kotlin.math.abs

class MainActivity : Activity() {

    private lateinit var player: ImageView
    private lateinit var obstacle: ImageView
    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView

    private val lanesX = mutableListOf<Float>()
    private var currentLane = 1 // 0 = שמאל, 1 = אמצע, 2 = ימין
    private var obstacleLane = 0
    private var obstacleY = 0f
    private var lives = 3

    private val handler = Handler(Looper.getMainLooper())
    private val updateDelay: Long = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. מציאת רכיבי UI
        player   = findViewById(R.id.player)
        obstacle = findViewById(R.id.obstacle)
        btnLeft  = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        heart1   = findViewById(R.id.heart1)
        heart2   = findViewById(R.id.heart2)
        heart3   = findViewById(R.id.heart3)

        // 2. חישוב המסלולים (X) לאחר שה-layout נטען
        player.post {
            val screenWidth = player.rootView.width
            val laneWidth   = screenWidth / 3f

            lanesX.clear()
            lanesX.add(laneWidth / 2 - player.width  / 2) // שמאל
            lanesX.add(screenWidth / 2f - player.width / 2) // אמצע
            lanesX.add(screenWidth - laneWidth / 2 - player.width / 2) // ימין

            movePlayerToLane(currentLane)
            startGameLoop()
        }

        // 3. כפתורי תזוזה
        btnLeft.setOnClickListener {
            if (currentLane > 0) {
                currentLane--
                movePlayerToLane(currentLane)
            }
        }
        btnRight.setOnClickListener {
            if (currentLane < 2) {
                currentLane++
                movePlayerToLane(currentLane)
            }
        }
    }

    private fun movePlayerToLane(lane: Int) {
        player.x = lanesX[lane]
    }

    private fun startGameLoop() {
        resetObstacle()
        handler.post(updateRunnable)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            // 1. הזזת המכשול כלפי מטה
            obstacleY += 10f
            obstacle.y = obstacleY

            // 2. חישוב מיקום המכשול והשחקן
            val playerTop    = player.y
            val playerBottom = playerTop + player.height
            val obstacleTop  = obstacleY
            val obstacleBottom = obstacleTop + obstacle.height

            // 3. בדיקה אם המכשול באותו המסלול כמו השחקן
            val sameLane = (obstacleLane == currentLane) &&
                    abs(obstacle.x - lanesX[currentLane]) < 1f

            // 4. בדיקת התנגשות – רק אם יש חפיפה ורטיקלית (מכשול לא מעל השחקן)
            if (obstacleBottom >= playerTop && obstacleTop <= playerBottom && sameLane) {
                lives--
                when (lives) {
                    2 -> heart3.visibility = View.GONE
                    1 -> heart2.visibility = View.GONE
                    0 -> {
                        heart1.visibility = View.GONE
                        handler.removeCallbacks(this)
                        Toast.makeText(this@MainActivity, "Game Over", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                resetObstacle()
            }

            // 5. אתחול אם המכשול יצא מהמסך
            if (obstacleY > player.rootView.height) {
                resetObstacle()
            }

            // 6. לולאת עדכון מחודשת
            handler.postDelayed(this, updateDelay)
        }
    }

    private fun resetObstacle() {
        obstacleY = 0f
        obstacleLane = Random.nextInt(0, 3)
        obstacle.x = lanesX[obstacleLane]
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}
