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

// Cache to store user FCM tokens temporarily
const fcmTokenCache = new Map<string, { token: string; expiry: number }>();

// Function to get FCM token with caching and expiry
async function getUserFcmToken(userId: string): Promise<string | undefined> {
  const currentTimestamp = Date.now();

  if (fcmTokenCache.has(userId)) {
    const cachedData = fcmTokenCache.get(userId);
    if (cachedData && cachedData.expiry > currentTimestamp) {
      return cachedData.token;
    }
  }

  const userDoc = await admin.firestore().collection("users").doc(userId).get();
  if (!userDoc.exists) {
    console.error(`User document not found for userId: ${userId}`);
    return undefined;
  }

  const fcmToken = userDoc.data()?.fcmToken as string | undefined;
  if (fcmToken) {
    fcmTokenCache.set(userId, { token: fcmToken, expiry: currentTimestamp + 300000 }); // Cache for 5 minutes
  }
  return fcmToken;
}

// Notify user when a task is created
export const notifyTaskCreated = onDocumentCreated("tasks/{taskId}", async (event) => {
  const task = event.data?.data();
  const taskId = event.params.taskId;

  if (!task) {
    console.error(`No task data found for taskId: ${taskId}`);
    return;
  }

  const { userId, title } = task;

  if (!userId) {
    console.error(`Task does not have an associated user. taskId: ${taskId}`);
    return;
  }

  const fcmToken = await getUserFcmToken(userId);
  if (!fcmToken) {
    console.warn(`No FCM token for userId: ${userId}`);
    return;
  }

  const body = `Your new task "${title}" has been created.`;
  await sendNotification(fcmToken, "New Task Created", body);
});

// Notify user when a task is updated
export const notifyTaskUpdated = onDocumentUpdated("tasks/{taskId}", async (event) => {
  const after = event.data?.after?.data();
  const before = event.data?.before?.data();
  const taskId = event.params.taskId;

  if (!after || !before) {
    console.error(`No updated task data found for taskId: ${taskId}`);
    return;
  }

  const { userId, title } = after;

  if (!userId) {
    console.error(`Task does not have an associated user. taskId: ${taskId}`);
    return;
  }

  const fcmToken = await getUserFcmToken(userId);
  if (!fcmToken) {
    console.warn(`No FCM token for userId: ${userId}`);
    return;
  }

  const body = `Your task "${title}" has been updated.`;
  await sendNotification(fcmToken, "Task Updated", body);
});

// Notify user when a task is deleted
export const notifyTaskDeleted = onDocumentDeleted("tasks/{taskId}", async (event) => {
  const task = event.data?.data();
  const taskId = event.params.taskId;

  if (!task) {
    console.error(`No task data found for taskId: ${taskId}`);
    return;
  }

  const { userId, title } = task;

  if (!userId) {
    console.error(`Task does not have an associated user. taskId: ${taskId}`);
    return;
  }

  const fcmToken = await getUserFcmToken(userId);
  if (!fcmToken) {
    console.warn(`No FCM token for userId: ${userId}`);
    return;
  }

  const body = `Your task "${title}" has been deleted.`;
  await sendNotification(fcmToken, "Task Deleted", body);
});
