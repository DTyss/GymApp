import admin from "firebase-admin";
import fs from "fs";

const path = process.env.FIREBASE_SERVICE_ACCOUNT_PATH!;
if (!admin.apps.length) {
  const serviceAccount = JSON.parse(fs.readFileSync(path, "utf8"));
  admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
}

export async function sendToToken(token: string, title: string, body: string) {
  return admin.messaging().send({
    token,
    notification: { title, body },
  });
}

export async function sendToTokens(tokens: string[], title: string, body: string) {
  if (tokens.length === 0) return { successCount: 0, failureCount: 0 };
  return admin.messaging().sendEachForMulticast({
    tokens,
    notification: { title, body },
  });
}
