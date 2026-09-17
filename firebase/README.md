# Firebase setup

Create a Firebase project and add the Android application id `com.vjagarwal.veyra`.

Download `google-services.json` into `android/app/` only in your private/local checkout. It is ignored by git.

Enable Firebase Cloud Messaging for optional remote notifications and Firebase Analytics only after the local safety flow is validated.

The local alarm must not depend on FCM.