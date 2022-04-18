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
import androidx.recyclerview.widget.RecyclerView

import com.caneryildirim.odevinipaylas.Activity.NotificationActivity
import com.caneryildirim.odevinipaylas.Activity.UploadActivityNew
import com.caneryildirim.odevinipaylas.Adaptors.Odev
import com.caneryildirim.odevinipaylas.Adaptors.RecyclerOdevAdaptor
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.adaptorFeed

import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dersFeed
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapImageUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapStringUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.downloadUrlUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.position
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.positionDetail

import com.caneryildirim.odevinipaylas.Adaptors.Singleton.selectedAciklamaUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.selectedDersUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.selectedKonuUpload

import com.caneryildirim.odevinipaylas.Adaptors.Singleton.uuidUpload

import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.FragmentFeedMainBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

import com.google.firebase.storage.FirebaseStorage

import com.onesignal.OneSignal
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList


class FragmentFeedMain : Fragment(),RecyclerOdevAdaptor.Delete{
    private var _binding: FragmentFeedMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth:FirebaseAuth
    private lateinit var storage:FirebaseStorage
    private var odevArrayList=ArrayList<Odev>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedMainBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        //registiration?.remove()

    }



    override fun onStart() {
        super.onStart()
        db= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance()

        /*
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this.requireContext())
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent=Intent(this.requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }

         */


        binding.recyclerFeed.layoutManager=LinearLayoutManager(this.context)
        val userUid=auth.currentUser!!.uid
        adaptorFeed= RecyclerOdevAdaptor(this,odevArrayList,userUid)
        binding.recyclerFeed.adapter= adaptorFeed

        adaptorFeed!!.notifyDataSetChanged()

        if (auth.currentUser!=null){
            getData()
            uuidUpload="null"
        }


        binding.recyclerFeed.addOnScrollListener(object:RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy>0){
                    binding.fabFeed.hide()
                }else{
                    binding.fabFeed.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        if (position!=null){
            binding.recyclerFeed.scrollToPosition(position!!)
        }else{
            binding.recyclerFeed.scrollToPosition(0)
        }

        if (positionDetail!=null){
            binding.recyclerFeed.scrollToPosition(positionDetail!!)
            positionDetail=null
        }




        binding.swipeFragmentMain.setColorSchemeColors(resources.getColor(R.color.purple_500))
        binding.swipeFragmentMain.setOnRefreshListener {
            getData()
            binding.swipeFragmentMain.isRefreshing=false
        }


        binding.fabFeed.setOnClickListener {
            val intentUpload = Intent(this.context, UploadActivityNew::class.java)
            startActivity(intentUpload)
        }



    }


    private fun getData() {

        if (dersFeed=="Tüm Dersler"){
            val reference=db.collection("Odev").orderBy("date",Query.Direction.DESCENDING)
            reference.get().addOnSuccessListener {
                if (it!=null){
                        if (!it.isEmpty){
                            val documents= it.documents
                            odevArrayList.clear()
                            for (document in documents){
                                val downloadUrl=document.get("downloadUrl") as String
                                val email=document.get("email") as String
                                val userDisplayName=document.get("userDisplayName") as String
                                val selectedAciklama=document.get("selectedAciklama") as String
                                val selectedDers=document.get("selectedDers") as String
                                val selectedKonu=document.get("selectedKonu") as String
                                val userUid=document.get("userUid") as String
                                val time=document.get("time") as String
                                val userPhotoUrl=document.get("userPhotoUrl") as String
                                val docRef=document.get("docRef") as String
                                val pId=document.get("pId") as String?
                                val dogruCevap=document.get("dogruCevap") as Boolean?
                                val dogruCevapString=document.get("dogruCevapString") as String?
                                val dogruCevapImage=document.get("dogruCevapImage") as String?
                                val date=document.get("date") as Timestamp

                                val odev=Odev(downloadUrl,email,selectedAciklama,selectedDers,selectedKonu,userUid,time,userDisplayName,userPhotoUrl,docRef,pId,dogruCevap,dogruCevapString,dogruCevapImage,date)
                                odevArrayList.add(odev)
                            }
                            adaptorFeed!!.notifyDataSetChanged()
                        }
                    }

            }.addOnFailureListener {

            }

        }else{
            val referenceNew= db.collection("Odev")
                .whereEqualTo("selectedDers", dersFeed)
                .orderBy("date",Query.Direction.DESCENDING)
                referenceNew.get().addOnSuccessListener {
                if (it!=null){
                        if (!it.isEmpty){
                            val documents=it.documents
                            odevArrayList.clear()
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
                                val docRef=document.get("docRef") as String
                                val pId=document.get("pId") as String?
                                val dogruCevap=document.get("dogruCevap") as Boolean?
                                val dogruCevapString=document.get("dogruCevapString") as String?
                                val dogruCevapImage=document.get("dogruCevapImage") as String?
                                val date=document.get("date") as Timestamp

                                val odev=Odev(downloadUrl,email,selectedAciklama,selectedDers,selectedKonu,userUid,time,userDisplayName,userPhotoUrl,docRef,pId,dogruCevap,dogruCevapString,dogruCevapImage,date)
                                odevArrayList.add(odev)
                            }
                            adaptorFeed!!.notifyDataSetChanged()
                        }
                    }

            }.addOnFailureListener {
                println(it.localizedMessage)
            }

        }
    }

    override fun onItemClick(position: Int) {
        val reference=storage.reference.child("images").child("${odevArrayList[position].docRef}.jpg")
        reference.delete().addOnSuccessListener {
            db.collection("Odev").document(odevArrayList[position].docRef).delete().addOnSuccessListener {
                odevArrayList.clear()
                Toast.makeText(this.requireContext(),"Soru Silindi",Toast.LENGTH_SHORT).show()

                adaptorFeed!!.notifyDataSetChanged()
                getData()
            }.addOnFailureListener {
                Toast.makeText(this.requireContext(),"Soru silinirken hata oluştu",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this.requireContext(),"Soru silinirken hata oluştu",Toast.LENGTH_SHORT).show()
        }




    }

    override fun sikayetItem(position: Int) {
            val alert=AlertDialog.Builder(this.requireContext())
            alert.setTitle("Soruyu Şikayet Et")
            alert.setMessage("Emin misin?")
            alert.setNegativeButton("Hayır"){ dialogInterface: DialogInterface, i: Int ->
                Toast.makeText(this.requireContext(),"Vazgeçildi",Toast.LENGTH_SHORT).show()
            }
            alert.setPositiveButton("Evet"){ dialogInterface: DialogInterface, i: Int ->

                val soruUid=odevArrayList[position].docRef
                val userEmail=odevArrayList[position].userDisplayName
                val userUid=odevArrayList[position].userUid
                val imageUuid=odevArrayList[position].docRef
                val date=Timestamp.now()
                val uuidBildirim= UUID.randomUUID().toString()
                val calendar=Calendar.getInstance()
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
        if (odevArrayList[position].dogruCevap==true){
            Toast.makeText(this.requireContext(),"Çözülmüş bir soruyu düzenleyemezsin",Toast.LENGTH_SHORT).show()
        }else{
            val intent=Intent(this.requireContext(),UploadActivityNew::class.java)
            Singleton.position=position
            uuidUpload=odevArrayList[position].docRef
            selectedAciklamaUpload=odevArrayList[position].selectedAciklama
            selectedDersUpload=odevArrayList[position].selectedDers
            selectedKonuUpload=odevArrayList[position].selectedKonu
            downloadUrlUpload=odevArrayList[position].downloadUrl
            dogruCevapUpload=odevArrayList[position].dogruCevap
            dogruCevapStringUpload=odevArrayList[position].dogruCevapString
            dogruCevapImageUpload=odevArrayList[position].dogruCevapImage

            startActivity(intent)
        }


    }

    override fun usteCikar(position: Int) {


        if (odevArrayList[position].dogruCevap==true){
            Toast.makeText(this.requireContext(),"Çözülmüş bir soruyu üste çıkaramazsın",Toast.LENGTH_SHORT).show()
        }else{
            val calendar= Calendar.getInstance()
            val day=calendar.get(Calendar.DAY_OF_MONTH)
            val month=calendar.get(Calendar.MONTH)+1
            val year=calendar.get(Calendar.YEAR)
            val time="${day}.${month}.${year}"
            val postMap= hashMapOf<String,Any>()
            postMap.put("downloadUrl",odevArrayList[position].downloadUrl)
            postMap.put("selectedAciklama",odevArrayList[position].selectedAciklama)
            postMap.put("selectedDers",odevArrayList[position].selectedDers)
            postMap.put("selectedKonu",odevArrayList[position].selectedKonu)
            postMap.put("userUid",odevArrayList[position].userUid)
            postMap.put("email",odevArrayList[position].email)
            postMap.put("userDisplayName",odevArrayList[position].userDisplayName)
            postMap.put("userPhotoUrl",odevArrayList[position].userPhotoUrl)
            postMap.put("date",Timestamp.now())
            postMap.put("time",time)
            postMap.put("docRef",odevArrayList[position].docRef)
            odevArrayList[position].dogruCevap?.let { postMap.put("dogruCevap", it) }
            odevArrayList[position].pId?.let { postMap.put("pId", it) }
            odevArrayList[position].dogruCevapString?.let { postMap.put("dogruCevapString", it) }
            odevArrayList[position].dogruCevapImage?.let { postMap.put("dogruCevapImage", it) }

            db.collection("Odev").document(odevArrayList[position].docRef).set(postMap)
                .addOnSuccessListener {
                    odevArrayList.clear()
                    Toast.makeText(this.requireContext(),"Soru üste taşındı",Toast.LENGTH_SHORT).show()
                    adaptorFeed!!.notifyDataSetChanged()
                    getData()
                }.addOnFailureListener {
                    Toast.makeText(this.requireContext(),"Soru üste taşınamadı",Toast.LENGTH_SHORT).show()
                }

        }
    }


}