package com.volodymyr.myapplication

import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class PlayService : Service() {

    val CHANNEL_ID = "10001"
    var player: MediaPlayer? = null
    var notification: NotificationCompat.Builder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == "stop") {
            player?.stop()
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(333, notification?.build())
            stopSelf()
            return START_NOT_STICKY
        }

        player?.stop()

        val url = intent!!.extras?.getString("mp3")
        player = MediaPlayer()
        player?.setDataSource(this, Uri.parse(url))
        player?.setOnPreparedListener { p ->
            if (p != player)
                return@setOnPreparedListener
            p.start()

            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    if (!p.isPlaying) {
                        timer.cancel()
                        return
                    }
                    notification?.setContentText("${p.currentPosition / 1000} sek / ${p.duration / 1000}")
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                        333,
                        notification?.build()
                    )
                }
            }, 1000, 1000)
        }

        player?.prepareAsync()


        val notificationIntent = Intent(
            this,
            MainActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val iStop = Intent(this, PlayService::class.java).setAction("stop")
        val pIStop = PendingIntent.getService(this, 0, iStop, PendingIntent.FLAG_UPDATE_CURRENT)
        val name = "textTitle"
        val descriptionText = "textContent"

        notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(name)
            .setContentText(descriptionText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAllowSystemGeneratedContextualActions(true)
            .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
            .addAction(R.mipmap.ic_launcher, "Stop", pIStop)
            .setAutoCancel(true)
            .setOngoing(false)

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(333, notification?.build())

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        //TODO("Not yet implemented")
        return null
    }

}