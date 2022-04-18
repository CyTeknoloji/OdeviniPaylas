package com.caneryildirim.odevinipaylas.Adaptors

import android.renderscript.ScriptGroup
import android.util.ArraySet
import android.view.MenuItem
import android.widget.ActionMenuView
import android.widget.TextView
import android.widget.Toolbar
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import java.util.*
import kotlin.collections.ArrayList


object Singleton{
    var dersFeed:String?=null
    var adaptorSoru:RecyclerOdevAdaptor?=null
    var activityWhere:String?=null
    var adaptorDers:RecyclerDersAdaptor?=null
    var adaptorFeed:RecyclerOdevAdaptor?=null
    var adaptorFav:RecyclerOdevAdaptor?=null


    var dateFragment:Long?=null
    var downloadUrlFragment:String?=null
    var selectedAciklamaFragment:String?=null

    var selectedDersFragment:String?=null
    var selectedKonuFragment:String?=null
    var timeFragment:String?=null
    var userDisplayNameFragment:String?=null
    var userPhotoUrlFragment:String?=null
    var userUidFragment:String?=null
    var docRefFragment:String?=null
    var docUuidFragment:String?=null
    var position:Int?=null
    var pIdFragment:String?=null
    var dogruCevapFragment:Boolean?=null
    var cevapYorumFragment:String?=null
    var dogruCevapStringFragment:String?=null
    var dogruCevapImageFragment:String?=null

    var downloadUrlCevap:String?=null
    var aciklamaCevap:String?=null


    const val ONESIGNAL_APP_ID = "62e842d6-b661-47bf-8c52-8f36bf8b4e46"


    var downloadUrlUpload:String?=null
    var selectedAciklamaUpload:String?=null
    var selectedSinifUpload:String?=null
    var selectedDersUpload:String?=null
    var selectedKonuUpload:String?=null
    var uuidUpload:String?=null
    var dogruCevapUpload:Boolean?=null
    var dogruCevapStringUpload:String?=null
    var dogruCevapImageUpload:String?=null

    var dogruCevapDuzenle:Boolean?=null

    var mInterstitialAd: InterstitialAd? = null


    var registiration: ListenerRegistration?=null
    var registirationFeed:ListenerRegistration?=null

    var takipciList=ArraySet<Takipci>()

    var positionDetail:Int?=null

    var filterFeedString:String?=null











}