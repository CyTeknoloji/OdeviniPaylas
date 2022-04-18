package com.caneryildirim.odevinipaylas.Activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager
import com.caneryildirim.odevinipaylas.Adaptors.Notification
import com.caneryildirim.odevinipaylas.Adaptors.RecyclerNotificationAdaptor
import com.caneryildirim.odevinipaylas.Adaptors.Singleton


import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.ActivityNotificationBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationActivity : AppCompatActivity(),RecyclerNotificationAdaptor.Click {
    private lateinit var binding:ActivityNotificationBinding
    private var notificationList=ArrayList<Notification>()
    private lateinit var adapter:RecyclerNotificationAdaptor
    private lateinit var db:FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    override fun onResume() {
        super.onResume()
        notificationList.clear()
        adapter.notifyDataSetChanged()
        getData()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityNotificationBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        db= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()


        if (Singleton.mInterstitialAd!=null){
            Singleton.mInterstitialAd?.show(this)
            Singleton.mInterstitialAd =null
        }



        binding.toolbarNotification.title="Bildirimler"
        binding.toolbarNotification.setTitleTextColor(getColor(R.color.white))
        setSupportActionBar(binding.toolbarNotification)
        binding.toolbarNotification.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.recyclerNotification.layoutManager=LinearLayoutManager(this)
        adapter= RecyclerNotificationAdaptor(notificationList,this)
        binding.recyclerNotification.adapter=adapter

        getData()

        binding.deleteNot.setOnClickListener {
            if (!notificationList.isNullOrEmpty()){
                val popup= PopupMenu(this,it)
                popup.menuInflater.inflate(R.menu.bildirim_sil,popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener {
                    if (it.itemId==R.id.okunanlari_sil){
                        val alert= AlertDialog.Builder(this)
                        alert.setTitle("Okunan Bildirimleri Sil")
                        alert.setMessage("Emin misin?")
                        alert.setNegativeButton("Hayır"){ dialogInterface: DialogInterface, i: Int ->
                            Toast.makeText(this,"Vazgeçildi", Toast.LENGTH_SHORT).show()
                        }

                        alert.setPositiveButton("Evet"){ dialogInterface: DialogInterface, i: Int ->
                            db.collection("Users").document(auth.currentUser!!.uid).collection("Bildirimler")
                                .whereEqualTo("okundu",true).get().addOnSuccessListener {
                                    if (it!=null){
                                        if (!it.isEmpty){
                                            val documents=it.documents
                                            for (document in documents){
                                                val docBildirim=document.get("docBildirim") as String
                                                db.collection("Users").document(auth.currentUser!!.uid).collection("Bildirimler")
                                                    .document(docBildirim).delete().addOnSuccessListener {  }
                                            }
                                            notificationList.clear()
                                            adapter.notifyDataSetChanged()
                                            getData()
                                        }else{
                                            Toast.makeText(this,"Okunmuş bildirim yok!",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                        }
                        alert.show()

                        true
                    }else if (it.itemId==R.id.tumunu_sil){
                        val alert= AlertDialog.Builder(this)
                        alert.setTitle("Tüm Bildirimleri Sil")
                        alert.setMessage("Emin misin?")
                        alert.setNegativeButton("Hayır"){ dialogInterface: DialogInterface, i: Int ->
                            Toast.makeText(this,"Vazgeçildi", Toast.LENGTH_SHORT).show()
                        }

                        alert.setPositiveButton("Evet"){ dialogInterface: DialogInterface, i: Int ->
                            db.collection("Users").document(auth.currentUser!!.uid).collection("Bildirimler")
                                .get().addOnSuccessListener {
                                    if (it!=null){
                                        if (!it.isEmpty){
                                            val documents=it.documents
                                            for (document in documents){
                                                val docBildirim=document.get("docBildirim") as String
                                                db.collection("Users").document(auth.currentUser!!.uid).collection("Bildirimler")
                                                    .document(docBildirim).delete().addOnSuccessListener {  }
                                            }
                                            notificationList.clear()
                                            adapter.notifyDataSetChanged()
                                            getData()

                                        }else{
                                            Toast.makeText(this,"Bildirim yok!",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                        }
                        alert.show()


                        true
                    }else{
                        false
                    }
                }
            }else{
                Toast.makeText(this,"Bildirim yok!",Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun getData() {
        db.collection("Users").document(auth.currentUser!!.uid).collection("Bildirimler")
            .orderBy("date",
            Query.Direction.DESCENDING).get().addOnSuccessListener {
            if (it != null) {
                if (!it.isEmpty){
                    notificationList.clear()
                    var documents=it.documents
                    for (document in documents){
                        val senderUsername=document.get("userName") as String
                        val senderUid=document.get("userUid") as String
                        val docBildirim=document.get("docBildirim") as String
                        val soruUid=document.get("soruUid") as String
                        val docUid=document.get("docUid") as String
                        val time=document.get("time") as String
                        val date=document.get("date") as Timestamp
                        val okundu=document.get("okundu") as Boolean?
                        val notification=Notification(senderUsername,senderUid,docBildirim,soruUid,docUid,time,date,okundu)
                        notificationList.add(notification)
                        adapter.notifyDataSetChanged()

                    }
                }
            }
        }


    }

    override fun delete(position: Int) {
        val refNot=db.collection("Users").document(auth.currentUser!!.uid).collection("Bildirimler").document(notificationList[position].docBildirim)
            refNot.update("okundu",true).addOnSuccessListener {

                db.collection("Odev").get().addOnSuccessListener {
                    if (it!=null){
                        val documents=it.documents
                        var info=false
                        for (doc in documents){
                            val soruUid=doc.get("docRef") as String
                            if (soruUid==notificationList[position].soruUid){
                                info=true
                                db.collection("Odev").document(notificationList[position].soruUid).get().addOnSuccessListener {
                                    if (it!=null){
                                        val downloadUrl=it.get("downloadUrl") as String
                                        val selectedAciklama=it.get("selectedAciklama") as String
                                        val selectedDers=it.get("selectedDers") as String
                                        val selectedKonu=it.get("selectedKonu") as String
                                        val time=it.get("time") as String
                                        val userDisplayName=it.get("userDisplayName") as String
                                        val userPhotoUrl=it.get("userPhotoUrl") as String
                                        val userUid=it.get("userUid") as String
                                        val docRef=it.get("docRef") as String
                                        //val position=position
                                        val pId=it.get("pId") as String
                                        val dogruCevap=it.get("dogruCevap") as Boolean?
                                        val dogruCevapString=it.get("dogruCevapString") as String?
                                        val dogruCevapImage=it.get("dogruCevapImage") as String?
                                        val date=it.get("date") as Timestamp
                                        val info="notification"

                                        val intent=Intent(this,DetailActivityLast::class.java)
                                        intent.putExtra("downloadUrl",downloadUrl)
                                        intent.putExtra("selectedAciklama",selectedAciklama)
                                        intent.putExtra("selectedDers",selectedDers)
                                        intent.putExtra("selectedKonu",selectedKonu)
                                        intent.putExtra("time",time)
                                        intent.putExtra("userDisplayName",userDisplayName)
                                        intent.putExtra("userPhotoUrl",userPhotoUrl)
                                        intent.putExtra("userUid",userUid)
                                        intent.putExtra("docRef",docRef)
                                        intent.putExtra("position",position)
                                        intent.putExtra("pId",pId)
                                        intent.putExtra("info",info)
                                        intent.putExtra("dogruCevap",dogruCevap)
                                        intent.putExtra("dogruCevapString",dogruCevapString)
                                        intent.putExtra("dogruCevapImage",dogruCevapImage)
                                        intent.putExtra("date",date)
                                        startActivity(intent)

                                    }
                                }.addOnFailureListener {
                                    println(it.localizedMessage)
                                }
                            }
                        }
                        if (info==false){
                            refNot.delete().addOnSuccessListener {
                                Toast.makeText(this,"Soru Silinmiş",Toast.LENGTH_SHORT).show()
                                notificationList.clear()
                                adapter.notifyDataSetChanged()
                                getData()
                            }
                        }
                    }
                }



            }.addOnFailureListener {
                println(it.localizedMessage)
            }
    }
}