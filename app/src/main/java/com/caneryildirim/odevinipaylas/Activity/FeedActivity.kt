package com.caneryildirim.odevinipaylas.Activity


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.adaptorFeed
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dersFeed
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.filterFeedString
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.mInterstitialAd
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.registirationFeed
import com.caneryildirim.odevinipaylas.Fragments.*
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.ActivityFeedBinding
import com.caneryildirim.odevinipaylas.databinding.NavigationBaslikFeedBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso


class FeedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navKontrol: String
    private lateinit var item:MenuItem
    private lateinit var itemNotification:MenuItem
    private lateinit var itemFilter:MenuItem
    private lateinit var db:FirebaseFirestore
    private var cartBadgeTextview:TextView?=null



    override fun onPause() {
        super.onPause()
        registirationFeed?.remove()
    }

    fun getNotData(){
        registirationFeed=db.collection("Users").document(auth.currentUser!!.uid).collection("Bildirimler")
            .whereNotEqualTo("okundu",true)
            .addSnapshotListener { it, error ->
                if (it!=null){
                    if (it.isEmpty){
                        cartBadgeTextview?.visibility=View.INVISIBLE
                    }else{
                        if (it.size()<100){
                            cartBadgeTextview?.visibility=View.VISIBLE
                            cartBadgeTextview?.setText(it.size().toString())

                        }else{
                            cartBadgeTextview?.visibility=View.VISIBLE
                            cartBadgeTextview?.setText("99")
                        }
                    }
                }else{
                    cartBadgeTextview?.visibility=View.INVISIBLE
                }


            }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()
        Singleton.activityWhere="Feed"

        val intent = intent
        val ders = intent.getStringExtra("ders")
        dersFeed = ders!!

        binding.toolbarFeed.title = ders
        binding.toolbarFeed.setTitleTextColor(getColor(R.color.white))

        setSupportActionBar(binding.toolbarFeed)

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent=Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        if (mInterstitialAd!=null){
            mInterstitialAd?.show(this)
            mInterstitialAd=null
        }

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val pId= OneSignal.getDeviceState()?.userId.toString()
        val sharedPreferences=this.getSharedPreferences("com.caneryildirim.odevinipaylas",Context.MODE_PRIVATE)
        val sharedInfo=sharedPreferences.getBoolean("info",false)
        if (sharedInfo==false){
            db.collection("Odev").whereEqualTo("userUid",auth.currentUser!!.uid).get().addOnSuccessListener {
                if (it!=null){
                    if (!it.isEmpty){
                        sharedPreferences.edit().putBoolean("info",true)
                        val documents=it.documents
                        for (document in documents){
                            val docRef=document.get("docRef") as String
                            val referenceNew=db.collection("Odev").document(docRef)
                            referenceNew.update("pId",pId).addOnSuccessListener {}
                        }
                    }
                }
            }
        }





        val toggle = ActionBarDrawerToggle(this, binding.drawerFeed, binding.toolbarFeed, 0, 0)
        toggle.drawerArrowDrawable.color=getColor(R.color.white)
        binding.drawerFeed.addDrawerListener(toggle)
        toggle.syncState()

        //getNotData()
        
        val bindingNav = NavigationBaslikFeedBinding.inflate(layoutInflater)
        bindingNav.textViewNavMenuFeed.text = "${auth.currentUser!!.displayName}"
        if (auth.currentUser!!.photoUrl != null) {
            Picasso.get().load(auth.currentUser!!.photoUrl).into(bindingNav.imageViewNavMenuFeed)
        } else {
            bindingNav.imageViewNavMenuFeed.setImageResource(R.drawable.usernullprofileimage)
        }
        binding.navigationViewFeed.addHeaderView(bindingNav.root)

        supportFragmentManager.beginTransaction().replace(R.id.fragmentFeed, FragmentFeedMain())
            .commit()
        navKontrol = "önde"

        binding.navigationViewFeed.setNavigationItemSelectedListener {
            if (it.itemId == R.id.menu_id_profile_feed) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentFeed, FragmentDersProfil()).commit()
                binding.drawerFeed.closeDrawer(GravityCompat.START)
                navKontrol = "yanda"
                binding.toolbarFeed.title = "Profil Ayarları"
                item.isVisible=false
                itemNotification.isVisible=false
                itemFilter.isVisible=false
            } else if (it.itemId == R.id.menu_id_puanla_feed) {
                val packageName="com.caneryildirim.odevinipaylas"
                val intentPuan=Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
                startActivity(intentPuan)
                navKontrol = "yanda"
            } else if (it.itemId == R.id.menu_id_paylas_feed) {

                val shareBody="Sorunu Paylaş uygulamasını Play Store'dan yükle : https://play.google.com/store/apps/details?id=com.caneryildirim.odevinipaylas"
                val shareIntent=Intent(Intent.ACTION_SEND)
                shareIntent.type="text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT,shareBody)
                startActivity(Intent.createChooser(shareIntent,"Paylaş"))

                navKontrol = "yanda"
            } else if (it.itemId == R.id.menu_id_cikis_feed) {
                    navKontrol = "önde"
                    if (auth.currentUser!=null){
                        auth.signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }

            } else if (it.itemId == R.id.menu_id_sorularim_feed) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentFeed, SorularimFragment()).commit()
                binding.drawerFeed.closeDrawer(GravityCompat.START)
                navKontrol = "yanda"
                binding.toolbarFeed.title = "Sorularım"
                item.isVisible=false
                itemNotification.isVisible=false
                itemFilter.isVisible=false
            } else if (it.itemId == R.id.menu_id_favori_feed) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentFeed, FavorilerFragment()).commit()
                binding.drawerFeed.closeDrawer(GravityCompat.START)
                navKontrol = "yanda"
                binding.toolbarFeed.title = "Favoriler"
                item.isVisible=false
                itemNotification.isVisible=false
                itemFilter.isVisible=false
            }
            true
        }

    }


    override fun onBackPressed() {
        if (binding.drawerFeed.isDrawerOpen(GravityCompat.START)) {
            binding.drawerFeed.closeDrawer(GravityCompat.START)
            navKontrol = "önde"
            binding.toolbarFeed.title = dersFeed
        } else {
            if (navKontrol == "yanda") {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentFeed, FragmentFeedMain()).commit()
                navKontrol = "önde"
                binding.toolbarFeed.title = dersFeed
                item.isVisible=true
                itemNotification.isVisible=true
                itemFilter.isVisible = dersFeed=="Tüm Dersler"
            } else if (navKontrol == "önde") {
                finish()
            }
        }

    }

    override fun onResume() {
        super.onResume()

        if (filterFeedString!=null){
            Singleton.adaptorFeed!!.filter.filter(filterFeedString)
        }

        Singleton.activityWhere="Feed"
        if (auth.currentUser == null) {
            finish()
        }else{
            getNotData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

            menuInflater.inflate(R.menu.menu_feed, menu)
            item = menu!!.findItem(R.id.menu_search)
            itemNotification=menu.findItem(R.id.menu_notification)
            itemFilter=menu.findItem(R.id.menu_filter)
            itemFilter.isVisible = dersFeed=="Tüm Dersler"

            val viewNotification=itemNotification.actionView
            cartBadgeTextview=viewNotification.findViewById<TextView>(R.id.cart_badge_textview)
            getNotData()


            viewNotification.setOnClickListener {
                onOptionsItemSelected(item)
                val intentNot=Intent(this,NotificationActivity::class.java)
                startActivity(intentNot)

            }

            val searchView = item.actionView as SearchView
            searchView.imeOptions=EditorInfo.IME_ACTION_DONE
            searchView.queryHint="Ara..."

            val textSearch=searchView.findViewById<EditText>(R.id.search_src_text)
            textSearch.setTextColor(getColor(R.color.white))
            textSearch.setHintTextColor(getColor(R.color.white))

            val searchButton=searchView.findViewById<ImageView>(R.id.search_close_btn)
            searchButton.setColorFilter(getColor(R.color.white))





            searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    adaptorFeed!!.filter.filter(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adaptorFeed!!.filter.filter(newText)
                    return false
                }
            })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.filter_gkgy){
            Singleton.adaptorFeed!!.filter.filter("(GKGY)")
            filterFeedString="(GKGY)"
        }else if (item.itemId==R.id.filter_eb){
            Singleton.adaptorFeed!!.filter.filter("(EB)")
            filterFeedString="(EB)"
        }else if (item.itemId==R.id.filter_oabt){
            Singleton.adaptorFeed!!.filter.filter("(ÖABT)")
            filterFeedString="(ÖABT)"
        }else if (item.itemId==R.id.filter_agrubu){
            Singleton.adaptorFeed!!.filter.filter("(A Grubu)")
            filterFeedString="(A Grubu)"
        }else if(item.itemId==R.id.filter_tum){
            Singleton.adaptorFeed!!.filter.filter("")
            filterFeedString=""
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        registirationFeed?.remove()
    }



}


