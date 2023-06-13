package com.capstone.eco_route.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.Timer
import java.util.TimerTask

class TimerService: Service() {

    private val stopWatch = Timer()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val time = intent.getDoubleExtra(TIME_EXTRA, 0.0)
        stopWatch.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        return  START_NOT_STICKY

    }

    override fun onDestroy() {
        stopWatch.cancel()
        super.onDestroy()
    }

    private inner class TimeTask(private var timer: Double): TimerTask() {

        override fun run() {

            val intent = Intent(TIMER_UPDATED)
            timer++
            intent.putExtra(TIME_EXTRA,timer)
            sendBroadcast(intent)

        }

    }

    companion object {

        const val TIMER_UPDATED = "timer_updated"
        const val TIME_EXTRA = "time_extra"

    }

}