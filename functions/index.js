const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

exports.onCallMade = functions.firestore
    .document("/INCOMING/{numberReceiver}")
    .onCreate(async (snap, context) => {
        const numberR = context.params.numberReceiver
        if (snap.exists) {
            const info = snap.data();
            const callerName = info["name"];
            const callerNumber = info["number"];
            const callerImage = info["image"];
            const callerUid = info["uid"];
            const callerChannel = info["channel"];
            const callTime = info["timestamp"]

            const tokenRef = db.collection("tokens").doc(numberR);

            const tokenData = await tokenRef.get();
            const notificationToken = tokenData.data().token;

            if (notificationToken) {
                const message = {
                    "token": notificationToken,
                    "data": {
                        "uid": `${callerUid}`,
                        "image": `${callerImage}`,
                        "name": `${callerName}`,
                        "number": `${callerNumber}`,
                        "channel": `${callerChannel}`,
                        "timestamp": `${callTime}`
                    }
                };
                admin.messaging().send(message).then(onvalue => {
                    console.log("Sent notification", onvalue);
                }).catch(error => {
                    console.log("error sending notification", error);
                });
            } else {
                console.log("No token for user");
            }
        }
    });

exports.onCallUpdate = functions.firestore
    .document("/INCOMING/{numberReceiver}")
    .onUpdate(async (snap, context) => {
        const numberR = context.params.numberReceiver
        const info = snap.after.data();
        const callerName = info["name"];
        const callerNumber = info["number"];
        const callerImage = info["image"];
        const callerUid = info["uid"];
        const callerChannel = info["channel"];
        const callTime = info["timestamp"]

        const tokenRef = db.collection("tokens").doc(numberR);

        const tokenData = await tokenRef.get();
        const notificationToken = tokenData.data().token;

        if (notificationToken) {
            const message = {
                "token": notificationToken,
                "data": {
                    "uid": `${callerUid}`,
                    "image": `${callerImage}`,
                    "name": `${callerName}`,
                    "number": `${callerNumber}`,
                    "channel": `${callerChannel}`,
                    "timestamp": `${callTime}`
                }
            };
            admin.messaging().send(message).then(onvalue => {
                console.log("Sent notification", onvalue);
            }).catch(error => {
                console.log("error sending notification", error);
            });
        } else {
            console.log("No token for user");
        }
    });