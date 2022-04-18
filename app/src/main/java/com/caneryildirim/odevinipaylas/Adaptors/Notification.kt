package com.caneryildirim.odevinipaylas.Adaptors

import com.google.firebase.Timestamp

class Notification( val senderUsername:String,
                    val senderUserUid:String,
                    val docBildirim:String,
                    val soruUid:String,
                    val docUid:String,
                    val time:String,
                    val date:Timestamp,
                    val okundu:Boolean?
                    ) {
}