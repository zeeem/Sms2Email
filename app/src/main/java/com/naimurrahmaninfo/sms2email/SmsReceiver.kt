package com.naimurrahmaninfo.sms2email

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentLinkedQueue

// Thread-safe queue for passing SMS data to the service
object SmsQueue {
    val queue: ConcurrentLinkedQueue<SmsData> = ConcurrentLinkedQueue()
}

data class SmsData(
    val sender: String,
    val message: String,
    val timestamp: Long,
    val gmt6: String,
    val mountain: String,
    val label: String
)

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val fullMessage = StringBuilder()
            var sender = ""
            var timestamp: Long = System.currentTimeMillis()
            for (msg in messages) {
                sender = msg.originatingAddress ?: sender
                fullMessage.append(msg.messageBody)
                timestamp = msg.timestampMillis
            }
            // Format times using java.util.Date and SimpleDateFormat for Android 7+ compatibility
            val date = Date(timestamp)
            val sdfGmt6 = SimpleDateFormat("d MMM yyyy h:mm:ss a", Locale.US)
            sdfGmt6.timeZone = TimeZone.getTimeZone("GMT+6")
            val gmt6 = sdfGmt6.format(date)
            val sdfMountain = SimpleDateFormat("d MMM yyyy h:mm:ss a", Locale.US)
            sdfMountain.timeZone = TimeZone.getTimeZone("America/Denver")
            val mountain = sdfMountain.format(date)
            // Get label from shared preferences (default to SMS_FWD_26106)
            val prefs = context.getSharedPreferences("sms2email_prefs", Context.MODE_PRIVATE)
            val label = prefs.getString("sms_label", "SMS_FWD_26106") ?: "SMS_FWD"
            // Add SMS data to queue for service to process
            SmsQueue.queue.add(
                SmsData(sender, fullMessage.toString(), timestamp, gmt6, mountain, label)
            )
        }
    }
}
