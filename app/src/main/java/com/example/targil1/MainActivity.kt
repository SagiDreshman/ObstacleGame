package com.example.targil1

import android.media.MediaPlayer

import android.Manifest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager

import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView



import dev.tomco.a25b_11345a_l05.utilities.SignalManager
import dev.tomco.a25b_11345a_l06.ui.HighScoreFragment
import kotlin.random.Random

class MainActivity :AppCompatActivity() {

    private lateinit var gameLayout: FrameLayout
    private lateinit var player: ImageView
    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView

    private var distance = 0
    private lateinit var distanceText: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val LOCATION_PERMISSION_REQUEST = 1001


    private var sensorMode = true
    private var speedMultiplier = 1.0
    private val baseDelay: Long = 800


    private val numRows = 6
    private val numCols = 5
    private val matrix = Array(numRows) { arrayOfNulls<ImageView>(numCols) }

    private var coins = 0

    private lateinit var hitSound: MediaPlayer

    private var playerCol = numCols / 2
    private var lives = 3
    private val handler = Handler(Looper.getMainLooper())
    private val obstacleDelay: Long = 800

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ScoreManager.init(this)

        val mode = intent.getStringExtra("MODE")?: "SENSOR"
        sensorMode = (mode == "SENSOR")
        hitSound = MediaPlayer.create(this, R.raw.hit_sound)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        // הגדרת מצב ושליטה במהירות
        when (mode) {
            "SENSOR" -> {
                sensorMode = true
                speedMultiplier = 1.0
            }
            "SLOW" -> {
                sensorMode = false
                speedMultiplier = 0.5
            }
            "FAST" -> {
                sensorMode = false
                speedMultiplier = 1.5
            }
        }

        SignalManager.init(this)

        gameLayout = findViewById(R.id.gameLayout)
        player     = findViewById(R.id.player)
        btnLeft    = findViewById(R.id.btnLeft)
        btnRight   = findViewById(R.id.btnRight)
        if(sensorMode){
            btnLeft.visibility = View.GONE
            btnRight.visibility = View.GONE
        }
        heart1     = findViewById(R.id.heart1)
        heart2     = findViewById(R.id.heart2)
        heart3     = findViewById(R.id.heart3)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        distanceText = findViewById(R.id.distanceText)

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

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!sensorMode) return

            val x = event.values[0]

            if (x > 2 && playerCol > 0) {
                playerCol--
                placePlayer()
            } else if (x < -2 && playerCol < numCols - 1) {
                playerCol++
                placePlayer()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            lastKnownLocation = location
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        }
    }


    override fun onResume() {
        super.onResume()
        if (sensorMode) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        if (sensorMode) {
            sensorManager.unregisterListener(sensorListener)
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
            handler.postDelayed(this, (baseDelay / speedMultiplier).toLong()) // ← שינוי כאן
        }
    }


    private fun moveObstaclesDown() {
        for (row in numRows - 1 downTo 0) {
            for (col in 0 until numCols) {
                val current = matrix[row][col]
                if (current != null) {
                    if (row == numRows - 1) {
                        if (col == playerCol) {
                            if (current.tag == "coin") {
                                collectCoin()
                            } else {
                                loseLife()
                            }
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
        distance++
        distanceText.text = "Distance: $distance"
    }
    private fun collectCoin() {
        coins++
        SignalManager.getInstance().toast("Collected coin! Total: $coins")
    }
    private fun spawnNewObstacle() {
        if (lives <= 0) return
        val col = Random.nextInt(0, numCols)
        val isCoin = Random.nextFloat() < 0.3f

        val view = ImageView(this)
        view.setImageResource(if (isCoin) R.drawable.coin else R.drawable.obstacle)
        view.tag = if (isCoin) "coin" else "obstacle"
        view.scaleType = ImageView.ScaleType.FIT_CENTER  // תמנע חיתוך

        val size = player.width
        val laneWidth = gameLayout.width / numCols
        val xPos = laneWidth * col + laneWidth / 2 - size / 2

        view.layoutParams = FrameLayout.LayoutParams(size, size)
        view.x = xPos.toFloat()
        view.y = 0f
        matrix[0][col] = view
        gameLayout.addView(view)
    }
    private fun showNameInputDialog(finalDistance: Int, coins: Int) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game Over")
        builder.setMessage("הכנס את שמך:")

        val input = EditText(this)
        input.hint = "השם שלך"
        input.requestFocus()
        builder.setView(input)

        // שלב 1: קבלת מיקום (Mock ברירת מחדל כרגע)
        val lat = 32.0853  // תוכל להחליף בנתונים אמיתיים
        val lng = 34.7818
        builder.setPositiveButton("אישור") { _, _ ->
            val playerName = input.text.toString().ifBlank { "שחקן" }
            val finalScore = ScoreManager.Score(
                name = playerName, distance = finalDistance, coins = coins,
                lat = lastKnownLocation?.latitude ?: 0.0,
            lng= lastKnownLocation?.longitude ?: 0.0
            )
            ScoreManager.addScore(finalScore)



            val intent = Intent(this, ScoreboardActivity::class.java)
            intent.putExtra("player_name", playerName)
            intent.putExtra("score", finalDistance)
            intent.putExtra("coins", coins)
            intent.putExtra("lat", lastKnownLocation?.latitude ?: 0.0)
            intent.putExtra("lng", lastKnownLocation?.longitude ?: 0.0)
            startActivity(intent)

        }

        builder.setNegativeButton("ביטול") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()

        input.post {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }
    }


    private fun loseLife() {
        lives--
        hitSound.start()


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

                showNameInputDialog(distance, coins)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(obstacleRunnable)

        if (::hitSound.isInitialized) {
            hitSound.release()
        }
    }

}
