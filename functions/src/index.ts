import * as admin from "firebase-admin";
import { onDocumentCreated, onDocumentUpdated, onDocumentDeleted } from "firebase-functions/v2/firestore";

// Initialize Firebase Admin SDK
admin.initializeApp();

// Utility function to send FCM notifications
async function sendNotification(fcmToken: string, title: string, body: string): Promise<void> {
  const message = {
    notification: { title, body },
    token: fcmToken,
  };

  try {
    await admin.messaging().send(message);
    console.log("Notification sent successfully:", { title, body });
  } catch (error) {
    console.error("Error sending FCM notification:", error);
  }
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

  try {
    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    if (!userDoc.exists) {
      console.error(`User document not found for userId: ${userId}`);
      return;
    }

    const fcmToken = userDoc.data()?.fcmToken as string | undefined;
    if (!fcmToken) {
      console.error(`No FCM token for userId: ${userId}`);
      return;
    }

    const body = `Your new task "${title}" has been created.`;
    await sendNotification(fcmToken, "New Task Created", body);
  } catch (error) {
    console.error(`Error in notifyTaskCreated for taskId: ${taskId}`, error);
  }
});

// Notify user when a task is updated
export const notifyTaskUpdated = onDocumentUpdated("tasks/{taskId}", async (event) => {
  const after = event.data?.after?.data();
  const taskId = event.params.taskId;

  if (!after) {
    console.error(`No updated task data found for taskId: ${taskId}`);
    return;
  }

  const { userId, title } = after;

  if (!userId) {
    console.error(`Task does not have an associated user. taskId: ${taskId}`);
    return;
  }

  try {
    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    if (!userDoc.exists) {
      console.error(`User document not found for userId: ${userId}`);
      return;
    }

    const fcmToken = userDoc.data()?.fcmToken as string | undefined;
    if (!fcmToken) {
      console.error(`No FCM token for userId: ${userId}`);
      return;
    }

    const body = `Your task "${title}" has been updated.`;
    await sendNotification(fcmToken, "Task Updated", body);
  } catch (error) {
    console.error(`Error in notifyTaskUpdated for taskId: ${taskId}`, error);
  }
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

  try {
    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    if (!userDoc.exists) {
      console.error(`User document not found for userId: ${userId}`);
      return;
    }

    const fcmToken = userDoc.data()?.fcmToken as string | undefined;
    if (!fcmToken) {
      console.error(`No FCM token for userId: ${userId}`);
      return;
    }

    const body = `Your task "${title}" has been deleted.`;
    await sendNotification(fcmToken, "Task Deleted", body);
  } catch (error) {
    console.error(`Error in notifyTaskDeleted for taskId: ${taskId}`, error);
  }
});
