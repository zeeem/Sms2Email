package com.naimurrahmaninfo.sms2email

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.naimurrahmaninfo.sms2email.EmailConfig
import kotlinx.coroutines.*
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SmsForwardService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var running = true
    private val CHANNEL_ID = "sms_forward_channel"
    private val NOTIFICATION_ID = 1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        serviceScope.launch {
            while (running) {
                val sms = SmsQueue.queue.poll()
                if (sms != null) {
                    sendEmail(sms)
                } else {
                    delay(1000)
                }
            }
        }
    }

    override fun onDestroy() {
        running = false
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS Forwarding Active")
            .setContentText("Listening for incoming SMS and forwarding to email.")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SMS Forwarding",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendEmail(sms: SmsData) {
        val username = EmailConfig.GMAIL_USERNAME
        val password = EmailConfig.GMAIL_APP_PASSWORD
        val recipient = username
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", EmailConfig.SMTP_HOST)
            put("mail.smtp.port", EmailConfig.SMTP_PORT)
        }
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                subject = "SMS_FW from ${sms.sender}"
                setText(
                    "<b>Sender:</b> ${sms.sender}<br>" +
                            "<b>Message:</b> ${sms.message}<br><br>" +
                            "<b>Label:</b> ${sms.label}<br>" +
                            "<b>Timestamp:</b> ${sms.timestamp}<br>" +
                            "<b>BD:</b> ${sms.gmt6}<br>" +
                            "<b>AB:</b> ${sms.mountain}<br>",
                    "utf-8",
                    "html"
                )
            }
            Transport.send(message)
            incrementEmailSentCount(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
            incrementEmailFailedCount(applicationContext)
        }
    }

    companion object {
        private const val PREFS_NAME = "sms2email_prefs"
        private const val EMAIL_COUNT_KEY = "email_sent_count"
        private const val EMAIL_FAIL_COUNT_KEY = "email_failed_count"
        fun getEmailSentCount(context: Context): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getInt(EMAIL_COUNT_KEY, 0)
        }
        fun incrementEmailSentCount(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getInt(EMAIL_COUNT_KEY, 0)
            prefs.edit().putInt(EMAIL_COUNT_KEY, current + 1).apply()
        }
        fun getEmailFailedCount(context: Context): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getInt(EMAIL_FAIL_COUNT_KEY, 0)
        }
        fun incrementEmailFailedCount(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getInt(EMAIL_FAIL_COUNT_KEY, 0)
            prefs.edit().putInt(EMAIL_FAIL_COUNT_KEY, current + 1).apply()
        }
    }
}
