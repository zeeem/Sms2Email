package com.naimurrahmaninfo.sms2email

/**
 * Store Gmail credentials and SMTP configuration here.
 * Consider using encrypted storage or Android Keystore for prod.
 */
object EmailConfig {
    const val GMAIL_USERNAME = "YOUR_EMAIL_USER@GMAIL.com" // <-- Gmail address
    const val GMAIL_APP_PASSWORD = "16 DIGIT APP PASS HERE" // <-- Gmail App Password

    /** Enable 2-step verification on google account settings>security
    and then goto https://myaccount.google.com/apppasswords to add new 16-digit app pass **/

    const val SMTP_HOST = "smtp.gmail.com"
    const val SMTP_PORT = "587"
}
