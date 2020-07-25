const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

exports.onCallMade = functions.firestore
    .document("/users/{numberReceiver}/INCOMING/{numberCaller}")
    .onCreate(async (snap, context) => {
        const numberR = context.params.numberReceiver
        const numberC = context.params.numberCaller
        if(snap.exists){
            console.log(snap.data.toString + " " + numberR + " " + numberC);
        }
    });