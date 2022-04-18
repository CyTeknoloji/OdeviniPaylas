package com.caneryildirim.odevinipaylas.Fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.caneryildirim.odevinipaylas.Activity.NotificationActivity

import com.caneryildirim.odevinipaylas.Adaptors.Odev
import com.caneryildirim.odevinipaylas.Adaptors.RecyclerOdevAdaptor
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.adaptorFav

import com.caneryildirim.odevinipaylas.databinding.FragmentFavorilerBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.Query

import com.google.firebase.storage.FirebaseStorage

import com.onesignal.OneSignal
import org.json.JSONException

import java.util.*
import kotlin.collections.ArrayList


class FavorilerFragment : Fragment(),RecyclerOdevAdaptor.Delete{
    private var _binding: FragmentFavorilerBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var odevArrayListFav=ArrayList<Odev>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFavorilerBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onStart() {
        super.onStart()
        auth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()
        storage= FirebaseStorage.getInstance()


        binding.recyclerViewFavoriler.layoutManager=LinearLayoutManager(this.context)
        val userUid=auth.currentUser!!.uid
        adaptorFav= RecyclerOdevAdaptor(this,odevArrayListFav,userUid)
        binding.recyclerViewFavoriler.adapter=adaptorFav

        odevArrayListFav.clear()
        adaptorFav!!.notifyDataSetChanged()
        getData()

        /*
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this.requireContext())
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent= Intent(this.requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }

         */



    }

    private fun getData(){
        if (auth.currentUser!=null){
            val reference=db.collection("Users").document(auth.currentUser!!.uid).collection("Favoriler").orderBy("date",Query.Direction.DESCENDING)
            reference.get().addOnSuccessListener {
                if (it!=null){
                    val documents=it.documents
                    odevArrayListFav.clear()

                    for (document in documents){
                        val odevDoc=document.get("odevDoc") as String
                        db.collection("Odev").whereEqualTo("docRef",odevDoc).get().addOnSuccessListener { its->

                            val docs=its!!.documents
                            for (doc in docs){
                                val downloadUrl=doc!!.get("downloadUrl") as String
                                val email=doc.get("email") as String
                                val selectedAciklama=doc.get("selectedAciklama") as String
                                val selectedDers=doc.get("selectedDers") as String
                                val selectedKonu=doc.get("selectedKonu") as String
                                val userUid=doc.get("userUid") as String
                                val time=doc.get("time") as String
                                val userDisplayName=doc.get("userDisplayName") as String
                                val userPhotoUrl=doc.get("userPhotoUrl") as String
                                val docRef=doc.get("docRef") as String
                                val pId=document.get("pId") as String?
                                val dogruCevap=doc.get("dogruCevap") as Boolean?
                                val dogruCevapString=doc.get("dogruCevapString") as String?
                                val dogruCevapImage=doc.get("dogruCevapImage") as String?
                                val date=doc.get("date") as Timestamp
                                val odevFav=Odev(downloadUrl,email,selectedAciklama,selectedDers,selectedKonu,userUid,time,userDisplayName,userPhotoUrl,docRef,pId,dogruCevap,dogruCevapString,dogruCevapImage,date)
                                odevArrayListFav.add(odevFav)

                                adaptorFav!!.notifyDataSetChanged()

                            }


                        }
                    }

                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this.requireContext(),"Sorunu sorularım menüsünden silebilirsin",Toast.LENGTH_SHORT).show()
    }

    override fun sikayetItem(position: Int) {
        val alert= AlertDialog.Builder(this.requireContext())
        alert.setTitle("Soruyu Şikayet Et")
        alert.setMessage("Emin misin?")
        alert.setNegativeButton("Hayır"){ dialogInterface: DialogInterface, i: Int ->
            Toast.makeText(this.requireContext(),"Vazgeçildi",Toast.LENGTH_SHORT).show()
        }
        alert.setPositiveButton("Evet"){ dialogInterface: DialogInterface, i: Int ->

            val soruUid=odevArrayListFav[position].docRef
            val userEmail=odevArrayListFav[position].userDisplayName
            val userUid=odevArrayListFav[position].userUid
            val imageUuid=odevArrayListFav[position].docRef
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
                    Toast.makeText(this.requireContext(),"Şikayet edildi",Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this.requireContext(),it.localizedMessage,Toast.LENGTH_SHORT).show()
                }
        }
        alert.show()
    }

    override fun duzenleItem(position: Int) {
        Toast.makeText(this.requireContext(),"Sorunu, sorularım bölümünden düzenleyebilirsin",Toast.LENGTH_SHORT).show()

    }

    override fun usteCikar(position: Int) {
        Toast.makeText(this.requireContext(),"Sorunu, sorularım bölümünden üste taşıyabilirsin",Toast.LENGTH_SHORT).show()
    }


}
