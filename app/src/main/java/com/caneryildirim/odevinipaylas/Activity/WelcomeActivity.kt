package com.caneryildirim.odevinipaylas.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import com.caneryildirim.odevinipaylas.Adaptors.Singleton.mInterstitialAd


import com.caneryildirim.odevinipaylas.databinding.ActivityWelcomeBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding:ActivityWelcomeBinding

    private var reklamInfo:Int?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWelcomeBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()

        //test id ca-app-pub-3940256099942544/1033173712
        //bununla değiştir ca-app-pub-8642310051732821/6038363962
        InterstitialAd.load(this,"ca-app-pub-8642310051732821/6038363962", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd=null


            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {

                mInterstitialAd = interstitialAd
            }
        })

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {

            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {

            }

            override fun onAdShowedFullScreenContent() {

                mInterstitialAd = null
            }
        }


        Handler(Looper.getMainLooper()).postDelayed({
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        },4000)







    }

    override fun onBackPressed() {

    }
}