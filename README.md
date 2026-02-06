# Sms2Email

Sms2Email is an Android application that automatically forwards incoming SMS messages to a specified Gmail address. It's designed to help you stay updated with your SMS notifications even when you don't have your phone handy, by delivering them directly to your inbox.

## Features

-   **Automatic Forwarding:** Forwards all incoming SMS messages in real-time.
-   **Email Metadata:** Includes sender information, message content, and timestamps in both GMT+6 and Mountain Time.
-   **Customizable Label:** Allows you to set a custom label suffix (e.g., `SMS_FWD_26106`) to help filter and organize forwarded emails in your inbox.
-   **Foreground Service:** Runs as a foreground service with a persistent notification to ensure reliable operation and transparency.
-   **Retry Logic:** (Implicitly handled by the service architecture) designed to process the SMS queue.

## Configuration

Before running the app, you must configure your Gmail credentials.

1.  Open `EmailConfig.kt`.
2.  Update the following constants:
    -   `GMAIL_USERNAME`: Your Gmail address (e.g., `yourname@gmail.com`).
    -   `GMAIL_APP_PASSWORD`: A 16-digit App Password generated from your Google Account.

### How to get a Gmail App Password:
1.  Enable **2-Step Verification** on your Google Account settings under the Security tab.
2.  Go to [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords).
3.  Select "Mail" and "Android Device" (or choose "Other" and give it a custom name like "Sms2Email").
4.  Copy the generated 16-digit password and paste it into `GMAIL_APP_PASSWORD` in `EmailConfig.kt`.

## Steps to Run the App

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/yourusername/Sms2Email.git
    ```
2.  **Open in Android Studio:**
    Open the project folder in Android Studio.
3.  **Configure Credentials:**
    Update `EmailConfig.kt` as described in the [Configuration](#configuration) section.
4.  **Build and Install:**
    Connect your Android device or start an emulator and click the **Run** button in Android Studio.
5.  **Grant Permissions:**
    When the app opens, click **"Request Permissions"**. You will need to grant:
    -   `RECEIVE_SMS`: To detect incoming messages.
    -   `READ_SMS`: To read the message content.
    -   `POST_NOTIFICATIONS` (Android 13+): To show the foreground service notification.
6.  **Set Label (Optional):**
    Enter a suffix in the "Label Suffix" field and click **"Set Label"**.
7.  **Start Forwarding:**
    Click the **"Start SMS Forwarding"** button. The button will turn red, and the service status will change to "Active".

## Technical Details

-   **Language:** Kotlin
-   **UI Framework:** Jetpack Compose
-   **Email Library:** [JavaMail for Android](https://github.com/javaee/javamail) (`com.sun.mail:android-mail`)
-   **Background Processing:** Foreground Service + BroadcastReceiver

## Disclaimer

This app handles sensitive information (SMS messages). Ensure you keep your `GMAIL_APP_PASSWORD` secure and do not commit it to public repositories. This app is for personal use and should be used responsibly.


