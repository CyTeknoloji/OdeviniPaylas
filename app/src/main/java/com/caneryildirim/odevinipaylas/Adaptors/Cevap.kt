package com.caneryildirim.odevinipaylas.Adaptors

import com.google.firebase.Timestamp

class Cevap(val downloadUrl:String,
            val selectedAciklama:String,
            val soruUid:String,
            val userEmail:String,
            val userUid:String,
            val docUuid:String,
            val dogruCevap:Boolean,
            val cevapYorum:String,
            val pIdCevap:String?,
            val userPhotoUrl:String?,
            val date:Timestamp,
            val yanlisCevap:Boolean?) {
}