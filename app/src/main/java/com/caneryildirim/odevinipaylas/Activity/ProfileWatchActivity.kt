package com.caneryildirim.odevinipaylas.Activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.caneryildirim.odevinipaylas.Adaptors.Odev
import com.caneryildirim.odevinipaylas.Adaptors.RecyclerOdevAdaptor
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.ActivityProfileWatchActivityBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

class ProfileWatchActivity : AppCompatActivity(),RecyclerOdevAdaptor.Delete {
    private lateinit var binding:ActivityProfileWatchActivityBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var odevArrayListProfileWatch=ArrayList<Odev>()
    private lateinit var userUidFragment:String
    private lateinit var adapter:RecyclerOdevAdaptor
    private var userPhotoFromData:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityProfileWatchActivityBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        binding.toolbarProfileWatch.title="Profil Detayları"
        binding.toolbarProfileWatch.setTitleTextColor(getColor(R.color.white))
        setSupportActionBar(binding.toolbarProfileWatch)
        binding.toolbarProfileWatch.setNavigationOnClickListener {
            onBackPressed()
        }

        db= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent= Intent(this,NotificationActivity::class.java)
            startActivity(intent)
        }

        val intent=intent
        userUidFragment= intent.getStringExtra("userUidFragment") as String
        val userPhoto=intent.getStringExtra("userPhotoFragment")
        val userName=intent.getStringExtra("userNameFragment")
        Singleton.position=intent.getIntExtra("position",0)

        getData()

        if (userPhotoFromData=="null"){
            binding.userPhotoProfileWatch.setImageResource(R.drawable.personfeed)
        }else if (userPhotoFromData==null){
            binding.userPhotoProfileWatch.setImageResource(R.drawable.personfeed)
        }



        binding.userNameProfileWatch.text=userName

        binding.recyclerProfileWatch.layoutManager= LinearLayoutManager(this)
        val currentUserUid=auth.currentUser!!.uid
        adapter=RecyclerOdevAdaptor(this,odevArrayListProfileWatch,currentUserUid)
        binding.recyclerProfileWatch.adapter=adapter


    }

    private fun getData() {
        val reference=db.collection("Odev").whereEqualTo("userUid",userUidFragment).orderBy("date", Query.Direction.DESCENDING)
        reference.get().addOnSuccessListener {
            if (it!=null){
                if (!it.isEmpty){
                    val documents= it.documents
                    odevArrayListProfileWatch.clear()
                    for (document in documents){
                        val downloadUrl=document.get("downloadUrl") as String
                        val email=document.get("email") as String
                        val selectedAciklama=document.get("selectedAciklama") as String
                        val selectedDers=document.get("selectedDers") as String
                        val selectedKonu=document.get("selectedKonu") as String
                        val userUid=document.get("userUid") as String
                        val time=document.get("time") as String
                        val userDisplayName=document.get("userDisplayName") as String
                        val userPhotoUrl=document.get("userPhotoUrl") as String
                        val pId=document.get("pId") as String
                        val docRef=document.get("docRef") as String
                        val dogruCevap=document.get("dogruCevap") as Boolean?
                        val dogruCevapString=document.get("dogruCevapString") as String?
                        val dogruCevapImage=document.get("dogruCevapImage") as String?
                        val date=document.get("date") as Timestamp
                        userPhotoFromData=userPhotoUrl

                        if (userPhotoFromData=="null"){
                            binding.userPhotoProfileWatch.setImageResource(R.drawable.personfeed)
                        }else if (userPhotoFromData==null){
                            binding.userPhotoProfileWatch.setImageResource(R.drawable.personfeed)
                        }else{
                            Picasso.get().load(userPhotoFromData).into(binding.userPhotoProfileWatch)
                        }

                        val odev=Odev(downloadUrl,email,selectedAciklama,selectedDers,selectedKonu,userUid,time,userDisplayName,userPhotoUrl,docRef,pId,dogruCevap,dogruCevapString,dogruCevapImage,date)
                        odevArrayListProfileWatch.add(odev)
                        adapter.notifyDataSetChanged()

                        if (odevArrayListProfileWatch.isEmpty()){
                            binding.textProfileWatch.visibility= View.VISIBLE
                        }else{
                            binding.textProfileWatch.visibility= View.INVISIBLE
                        }
                    }

                }
            }
        }.addOnFailureListener {
            println(it.localizedMessage)
        }
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this,"Sorunu sorularım menüsünden silebilirsin",Toast.LENGTH_SHORT).show()
    }

    override fun sikayetItem(position: Int) {
        val alert= AlertDialog.Builder(this)
        alert.setTitle("Soruyu Şikayet Et")
        alert.setMessage("Emin misin?")
        alert.setNegativeButton("Hayır"){ dialogInterface: DialogInterface, i: Int ->
            Toast.makeText(this,"Vazgeçildi",Toast.LENGTH_SHORT).show()
        }
        alert.setPositiveButton("Evet"){ dialogInterface: DialogInterface, i: Int ->

            val soruUid=odevArrayListProfileWatch[position].docRef
            val userEmail=odevArrayListProfileWatch[position].userDisplayName
            val userUid=odevArrayListProfileWatch[position].userUid
            val imageUuid=odevArrayListProfileWatch[position].docRef
            val date= Timestamp.now()
            val uuidBildirim= UUID.randomUUID().toString()
            val calendar= Calendar.getInstance()
            val day=calendar.get(Calendar.DAY_OF_MONTH)
            val month=calendar.get(Calendar.MONTH)+1
            val year=calendar.get(Calendar.YEAR)
            val time="${day}.${month}.${year}"
            val bildirimMap= hashMapOf<String,Any>()
            bildirimMap.put("soruUid",soruUid)
            bildirimMap.put("userName","${userEmail} sorusu şikayet edildi")
            bildirimMap.put("userUid",userUid)
            bildirimMap.put("docUid",imageUuid)
            bildirimMap.put("date",date)
            bildirimMap.put("docBildirim",uuidBildirim)
            bildirimMap.put("time",time)
            db.collection("Users").document("P2dukbTHNMcdyP3FjDOsd7fR6PT2").collection("Bildirimler").document(uuidBildirim).set(bildirimMap)
                .addOnSuccessListener {
                    Toast.makeText(this,"Şikayet edildi",Toast.LENGTH_SHORT).show()
                    db.collection("Admin").document("P2dukbTHNMcdyP3FjDOsd7fR6PT2")
                        .get().addOnSuccessListener {
                            if (it!=null){
                                val pidAdmin=it.get("pId") as String
                                try {
                                    OneSignal.postNotification("{'contents': {'en':'Soruya Şikayet Geldi'}, 'include_player_ids': ['" + pidAdmin + "']}",null)

                                }catch (e: JSONException){
                                    e.printStackTrace()
                                }
                            }
                        }
                }.addOnFailureListener {
                    Toast.makeText(this,it.localizedMessage,Toast.LENGTH_SHORT).show()
                }
        }
        alert.show()
    }

    override fun duzenleItem(position: Int) {
        Toast.makeText(this,"Sorunu, sorularım bölümünden düzenleyebilirsin",Toast.LENGTH_SHORT).show()
    }

    override fun usteCikar(position: Int) {
        Toast.makeText(this,"Sorunu, sorularım bölümünden üste taşıyabilirsin",Toast.LENGTH_SHORT).show()
    }
}