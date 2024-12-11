# semester-project-cis357-task-notification
---

# Task Notification App Using Firebase Cloud Messaging (FCM)

This tutorial demonstrates how to build a **Task Notification App** using Firebase Cloud Messaging (FCM), Firebase Authentication, Firebase Firestore, and Firebase Cloud Functions. The app helps users manage tasks and receive real-time notifications about task events, such as task creation, updates, and deadline reminders.

## Overview

The app integrates Firebase services to provide the following features:
- **User Authentication**: Secure login and session management using Firebase Authentication.
- **Task Management**: Real-time task storage, retrieval, and updates with Firebase Firestore.
- **Notifications**: Real-time notifications triggered by task events via Firebase Cloud Messaging and Cloud Functions.

---

## Getting Started

### Prerequisites
To follow this tutorial, ensure you have the following:
- Android Studio (version 2024.x or later)
- A Firebase account
- Basic knowledge of Firebase Authentication, Firestore, and FCM

### Project Setup

1. **Create a Firebase Project**:
    - Go to the [Firebase Console](https://console.firebase.google.com/).
    - Create a new project and enable **Authentication**, **Firestore**, and **Cloud Messaging**.

2. **Add Firebase to Your App**:
    - Add your Android app's package name to the Firebase project.
    - Download the `google-services.json` file and place it in your app's `app/` directory.

3. **Add Dependencies**:
   Update your `build.gradle` file with the following dependencies:
   ```gradle
   implementation 'com.google.firebase:firebase-auth:23.1.2'
   implementation 'com.google.firebase:firebase-firestore:24.7.1'
   implementation 'com.google.firebase:firebase-messaging:23.1.2'
   implementation 'com.google.firebase:firebase-functions:20.3.2'
   ```

4. **Sync the Project**: Sync your project to ensure all dependencies are installed.

---

## Step 1: Firebase Authentication

### Configure Firebase Authentication
1. In the Firebase Console, go to the **Authentication** section.
2. Enable the **Email/Password** sign-in method.

### Implement Authentication in the App
Add the following code in your login activity:
```kotlin
 // Initialize FirebaseAuth
auth = FirebaseAuth.getInstance()

// Check if the user is already logged in
val currentUser = auth.currentUser
        if (currentUser != null) {
updateFcmTokenForLoggedInUser(currentUser.uid)
navigateToTaskList() // Skip login screen if already logged in
        } else {
setContent {
    SemesterProjectCIS357TaskNotificationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                    auth = auth,
                    onLoginSuccess = { handleSuccessfulLogin() },
                    onRegister = { navigateToRegister() }
            )
        }
    }
}
        }
                }
```
---

## Step 2: Task Management with Firestore

### Firestore Structure
Create a Firestore collection named `tasks`. Each document represents a task:
```json
{
    "title": "Complete project",
    "description": "Finish the project by Friday",
    "dueDate": "2024-12-15T10:00:00Z",
    "userId": "user123",
     "id": "id123",
     "fcmtoken": "fcmtoken123",
}
```

### Add Tasks to Firestore
```kotlin
    private val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    private val taskListState = mutableStateOf<List<Task>>(emptyList())
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null
    private var taskListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            logout()
            return
        }

```

---

## Step 3: Notifications with FCM

### Configure Firebase Cloud Messaging
Implement a custom `FirebaseMessagingService`:
```kotlin
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    Log.d(TAG, "Message received from: ${remoteMessage.from}")

    // Handle notification payload
    remoteMessage.notification?.let {
        val title = it.title ?: "Task Notification"
        val body = it.body ?: "You have a new task update."
        sendNotification(title, body, null)
    }
```

Here’s the extracted **Step 3: Deploy with Cloud Function** section in README format:

---

# Step 3: Deploy with Cloud Function

## Initialize the Cloud Function

To set up and deploy a Cloud Function for notifications, follow these steps:

1. Install Firebase CLI
Ensure you have the Firebase CLI installed on your system. Run the following command to install it:
```bash
npm install -g firebase-tools
```

2. Log in to Firebase
Log in to your Firebase account:
```bash
firebase login
```

3. Initialize Cloud Functions
Navigate to your project directory and initialize Cloud Functions:
```bash
firebase init functions
```
- Select your Firebase project.
- Choose **JavaScript** or **TypeScript** as the language.
- Install dependencies when prompted.

4. Add the Notification Function
Edit the `functions/index.js` file and add the following code:
```typescript

import * as admin from "firebase-admin";
import {
  onDocumentCreated,
  onDocumentUpdated,
  onDocumentDeleted,
} from "firebase-functions/v2/firestore";

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp();
}

// Utility function to send FCM notifications
async function sendNotification(fcmToken: string, title: string, body: string): Promise<void> {
  const message = {
    notification: { title, body },
    token: fcmToken,
  };

  try {
    await admin.messaging().send(message);
    console.log("Notification sent successfully:", { title, body });
  } catch (error: any) {
    if (error.code === "messaging/registration-token-not-registered") {
      console.warn("Invalid FCM token. Notification skipped.");
    } else {
      console.error("Error sending FCM notification:", error);
    }
  }
}

```
5. Deploy the Function
Deploy the function to Firebase:
```bash
firebase deploy --only functions
```

### 6. Verify Deployment
- Go to the Firebase Console and navigate to the **Functions** tab.
---

## Testing the Function

1. **Use the Firebase Emulator Suite**:
   Test the Cloud Function locally using the Firebase Emulator Suite.

2. **Verify Notifications**:
    - Check the Firebase Console for logs and status updates.

---

Feel free to use this in your project documentation! Let me know if you need any more refinements.

## Testing the App

1. **Firebase Emulator Suite**: Test Firestore and Cloud Functions locally.
2. **Firebase Console**: Send test notifications to ensure the app handles them correctly.

---

## Further Enhancements
- Add grouped notifications for better organization.
- Improve the UI with a calendar view for tasks.
- Allow users to set custom notification schedules.

---

## References
- [Firebase Documentation](https://firebase.google.com/docs)
- [Firestore Guide](https://firebase.google.com/docs/firestore)
- [Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Cloud Functions](https://firebase.google.com/docs/functions)

---
