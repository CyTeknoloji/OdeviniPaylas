package com.caneryildirim.odevinipaylas.Activity

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.TextView

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.size
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.adaptorDers
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.mInterstitialAd
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.registiration

import com.caneryildirim.odevinipaylas.Fragments.FavorilerFragment
import com.caneryildirim.odevinipaylas.Fragments.FragmentDersMain
import com.caneryildirim.odevinipaylas.Fragments.FragmentDersProfil
import com.caneryildirim.odevinipaylas.Fragments.SorularimFragment
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.ActivityDersBinding
import com.caneryildirim.odevinipaylas.databinding.NavigationBaslikBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso


class DersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDersBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navKontrol:String
    private lateinit var item:MenuItem
    private lateinit var itemNotification:MenuItem
    private lateinit var itemFilter:MenuItem
    private lateinit var db:FirebaseFirestore
    private var cartBadgeTextview: TextView?=null




    fun getNotData(){
        registiration=db.collection("Users").document(auth.currentUser!!.uid).collection("Bildirimler")
            .whereNotEqualTo("okundu",true)
            .addSnapshotListener { it, error ->

                if (it!=null){
                    if (it.isEmpty){
                        cartBadgeTextview?.visibility= View.INVISIBLE
                    }else{
                        if (it.size()<100){
                            cartBadgeTextview?.visibility= View.VISIBLE
                            cartBadgeTextview?.setText(it.size().toString())

                        }else{
                            cartBadgeTextview?.visibility= View.VISIBLE
                            cartBadgeTextview?.setText("99")
                        }
                    }
                }else{
                    cartBadgeTextview?.visibility= View.INVISIBLE
                }
            }
    }

    override fun onPause() {
        super.onPause()
        registiration?.remove()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDersBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        auth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()
        Singleton.activityWhere="Ders"

        binding.toolbarDersActivity.title="SORUNU PAYLAŞ"

        binding.toolbarDersActivity.setTitleTextColor(getColor(R.color.white))
        setSupportActionBar(binding.toolbarDersActivity)
        val toogle=ActionBarDrawerToggle(this,binding.drawerDers,binding.toolbarDersActivity,0,0)
        toogle.drawerArrowDrawable.color=getColor(R.color.white)
        binding.drawerDers.addDrawerListener(toogle)

        toogle.syncState()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent=Intent(this,NotificationActivity::class.java)
            startActivity(intent)
        }

        if (mInterstitialAd!=null){
            mInterstitialAd?.show(this)
            mInterstitialAd=null
        }

        val bindingNav=NavigationBaslikBinding.inflate(layoutInflater)
        bindingNav.textViewNavMenu.text="${auth.currentUser!!.displayName}"
        if (auth.currentUser!!.photoUrl!=null){
            Picasso.get().load(auth.currentUser!!.photoUrl).into(bindingNav.imageViewNavMenu)
        }else{
            bindingNav.imageViewNavMenu.setImageResource(R.drawable.usernullprofileimage)
        }
        binding.navigationViewDers.addHeaderView(bindingNav.root)



        supportFragmentManager.beginTransaction().replace(R.id.framelayoutDers, FragmentDersMain()).commit()
        navKontrol="önde"

        binding.navigationViewDers.setNavigationItemSelectedListener {
            if (it.itemId== R.id.menu_id_profile){
                supportFragmentManager.beginTransaction().replace(R.id.framelayoutDers, FragmentDersProfil()).commit()
                binding.drawerDers.closeDrawer(GravityCompat.START)
                navKontrol="yanda"
                binding.toolbarDersActivity.title="Profil Ayarları"
                item.isVisible=false
                itemNotification.isVisible=false
                itemFilter.isVisible=false
            }else if (it.itemId== R.id.menu_id_puanla){
                val packageName="com.caneryildirim.odevinipaylas"
                val intent=Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
                startActivity(intent)

                navKontrol="yanda"
            }else if (it.itemId==R.id.menu_id_paylas){

                val shareBody="Sorunu Paylaş uygulamasını Play Store'dan yükle : https://play.google.com/store/apps/details?id=com.caneryildirim.odevinipaylas"
                val shareIntent=Intent(Intent.ACTION_SEND)
                shareIntent.type="text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT,shareBody)
                startActivity(Intent.createChooser(shareIntent,"Paylaş"))

                navKontrol="yanda"
            }else if (it.itemId== R.id.menu_id_cikis){
                    navKontrol="önde"
                    if (auth.currentUser!=null){
                        auth.signOut()
                        val intent=Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }

            }else if (it.itemId==R.id.menu_id_sorularim){
                supportFragmentManager.beginTransaction().replace(R.id.framelayoutDers, SorularimFragment()).commit()
                binding.drawerDers.closeDrawer(GravityCompat.START)
                navKontrol="yanda"
                binding.toolbarDersActivity.title="Sorularım"
                item.isVisible=false
                itemNotification.isVisible=false
                itemFilter.isVisible=false
            }else if (it.itemId==R.id.menu_id_favori){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.framelayoutDers, FavorilerFragment()).commit()
                binding.drawerDers.closeDrawer(GravityCompat.START)
                navKontrol="yanda"
                binding.toolbarDersActivity.title="Favoriler"
                item.isVisible=false
                itemNotification.isVisible=false
                itemFilter.isVisible=false
            }

            true
        }

    }



    override fun onBackPressed() {
        if (binding.drawerDers.isDrawerOpen(GravityCompat.START)){
            binding.drawerDers.closeDrawer(GravityCompat.START)
            navKontrol="önde"
            binding.toolbarDersActivity.title="SORUNU PAYLAŞ"
        }else{
            if (navKontrol=="yanda"){
                supportFragmentManager.beginTransaction().replace(R.id.framelayoutDers, FragmentDersMain()).commit()
                navKontrol="önde"
                binding.toolbarDersActivity.title="SORUNU PAYLAŞ"
                item.isVisible=false
                itemNotification.isVisible=true
                itemFilter.isVisible=true
            }else if (navKontrol=="önde"){
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Singleton.activityWhere="Ders"
        if (auth.currentUser==null){
            finish()
        }else{
            getNotData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

            menuInflater.inflate(R.menu.menu_feed, menu)
            item = menu!!.findItem(R.id.menu_search)
            item.isVisible=false
            itemNotification=menu.findItem(R.id.menu_notification)
            itemFilter=menu.findItem(R.id.menu_filter)


            val viewNotification=itemNotification.actionView
            cartBadgeTextview=viewNotification?.findViewById<TextView>(R.id.cart_badge_textview)
            getNotData()

            viewNotification?.setOnClickListener {
                onOptionsItemSelected(item)
                val intentNot=Intent(this,NotificationActivity::class.java)
                startActivity(intentNot)
            }

            val searchView = item.actionView as androidx.appcompat.widget.SearchView
            searchView.imeOptions= EditorInfo.IME_ACTION_DONE
            searchView.queryHint="Ara..."



            searchView.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    adaptorDers!!.filter.filter(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adaptorDers!!.filter.filter(newText)
                    return false
                }

            })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.filter_gkgy){
            adaptorDers!!.filter.filter("(GKGY)")
        }else if (item.itemId==R.id.filter_eb){
            adaptorDers!!.filter.filter("(EB)")
        }else if (item.itemId==R.id.filter_oabt){
            adaptorDers!!.filter.filter("(ÖABT)")
        }else if (item.itemId==R.id.filter_agrubu){
            adaptorDers!!.filter.filter("(A Grubu)")
        }else if(item.itemId==R.id.filter_tum){
            adaptorDers!!.filter.filter("")
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onDestroy() {
        super.onDestroy()
        registiration?.remove()
    }


}