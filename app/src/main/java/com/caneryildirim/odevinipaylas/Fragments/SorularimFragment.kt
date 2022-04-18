package com.caneryildirim.odevinipaylas.Fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.caneryildirim.odevinipaylas.Activity.NotificationActivity
import com.caneryildirim.odevinipaylas.Activity.UploadActivityNew
import com.caneryildirim.odevinipaylas.Adaptors.Odev
import com.caneryildirim.odevinipaylas.Adaptors.RecyclerOdevAdaptor
import com.caneryildirim.odevinipaylas.Adaptors.Singleton

import com.caneryildirim.odevinipaylas.Adaptors.Singleton.adaptorSoru
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.position

import com.caneryildirim.odevinipaylas.databinding.FragmentSorularimBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

import com.google.firebase.storage.FirebaseStorage

import com.onesignal.OneSignal
import java.util.*
import kotlin.collections.ArrayList


class SorularimFragment : Fragment(),RecyclerOdevAdaptor.Delete {
    private var _binding: FragmentSorularimBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var odevArrayListSoru=ArrayList<Odev>()




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSorularimBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null

    }

    private fun getData() {
        val reference=db.collection("Odev").whereEqualTo("userUid",auth.currentUser!!.uid).orderBy("date",Query.Direction.DESCENDING)
       reference.get().addOnSuccessListener {
            if (it!=null){
                if (!it.isEmpty){
                    val documents= it.documents
                    odevArrayListSoru.clear()
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
                        odevArrayListSoru.add(odev)

                    }
                    adaptorSoru!!.notifyDataSetChanged()

                }
            }
        }.addOnFailureListener {
            //println(it.localizedMessage)
       }
    }



    override fun onStart() {
        super.onStart()
        db= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance()

        getData()
        Singleton.uuidUpload ="null"

        binding.recyclerViewSorularim.layoutManager= LinearLayoutManager(this.context)
        val userUid=auth.currentUser!!.uid
        adaptorSoru=RecyclerOdevAdaptor(this,odevArrayListSoru,userUid)
        binding.recyclerViewSorularim.adapter=adaptorSoru

        /*
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this.requireContext())
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent= Intent(this.requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }

         */

        if (position!=null){
            binding.recyclerViewSorularim.scrollToPosition(position!!)
        }else{
            binding.recyclerViewSorularim.scrollToPosition(0)
        }

        if (Singleton.positionDetail !=null){
            binding.recyclerViewSorularim.scrollToPosition(Singleton.positionDetail!!)
            Singleton.positionDetail =null
        }
    }


    override fun onItemClick(position: Int) {
        val reference=storage.reference.child("images").child("${odevArrayListSoru[position].docRef}.jpg")
        reference.delete().addOnSuccessListener {
            db.collection("Odev").document(odevArrayListSoru[position].docRef).delete().addOnSuccessListener {
                Toast.makeText(this.requireContext(),"Soru Silindi",Toast.LENGTH_SHORT).show()
                if (Singleton.activityWhere=="Ders"){
                    //parentFragmentManager.beginTransaction().replace(R.id.framelayoutDers,SorularimFragment()).commit()
                    odevArrayListSoru.clear()
                    adaptorSoru!!.notifyDataSetChanged()
                    getData()
                }else if (Singleton.activityWhere=="Feed"){
                    //parentFragmentManager.beginTransaction().replace(R.id.fragmentFeed,SorularimFragment()).commit()
                    odevArrayListSoru.clear()
                    adaptorSoru!!.notifyDataSetChanged()
                    getData()
                }
            }.addOnFailureListener {
                Toast.makeText(this.requireContext(),"Soru silinirken hata oluştu",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this.requireContext(),"Soru silinirken hata oluştu",Toast.LENGTH_SHORT).show()
        }


    }


    override fun sikayetItem(position: Int) {

    }

    override fun duzenleItem(position: Int) {
        if (odevArrayListSoru[position].dogruCevap==true){
            Toast.makeText(this.requireContext(),"Çözülmüş bir soruyu düzenleyemezsin",Toast.LENGTH_SHORT).show()
        }else{
            val intent=Intent(this.requireContext(), UploadActivityNew::class.java)
            Singleton.position=position
            Singleton.uuidUpload =odevArrayListSoru[position].docRef
            Singleton.selectedAciklamaUpload =odevArrayListSoru[position].selectedAciklama
            Singleton.selectedDersUpload =odevArrayListSoru[position].selectedDers
            Singleton.selectedKonuUpload =odevArrayListSoru[position].selectedKonu
            Singleton.downloadUrlUpload =odevArrayListSoru[position].downloadUrl
            Singleton.dogruCevapUpload =odevArrayListSoru[position].dogruCevap
            Singleton.dogruCevapStringUpload =odevArrayListSoru[position].dogruCevapString
            Singleton.dogruCevapImageUpload=odevArrayListSoru[position].dogruCevapImage
            startActivity(intent)
        }

    }

    override fun usteCikar(position: Int) {
        if (odevArrayListSoru[position].dogruCevap==true){
            Toast.makeText(this.requireContext(),"Çözülmüş bir soruyu üste çıkaramazsın",Toast.LENGTH_SHORT).show()
        }else{
            val calendar= Calendar.getInstance()
            val day=calendar.get(Calendar.DAY_OF_MONTH)
            val month=calendar.get(Calendar.MONTH)+1
            val year=calendar.get(Calendar.YEAR)
            val time="${day}.${month}.${year}"

            val postMap= hashMapOf<String,Any>()
            postMap.put("downloadUrl",odevArrayListSoru[position].downloadUrl)
            postMap.put("selectedAciklama",odevArrayListSoru[position].selectedAciklama)
            postMap.put("selectedDers",odevArrayListSoru[position].selectedDers)
            postMap.put("selectedKonu",odevArrayListSoru[position].selectedKonu)
            postMap.put("userUid",odevArrayListSoru[position].userUid)
            postMap.put("email",odevArrayListSoru[position].email)
            postMap.put("userDisplayName",odevArrayListSoru[position].userDisplayName)
            postMap.put("userPhotoUrl",odevArrayListSoru[position].userPhotoUrl)
            postMap.put("date",Timestamp.now())
            postMap.put("time",time)
            postMap.put("docRef",odevArrayListSoru[position].docRef)
            odevArrayListSoru[position].dogruCevap?.let { postMap.put("dogruCevap", it) }
            odevArrayListSoru[position].pId?.let { postMap.put("pId", it) }
            odevArrayListSoru[position].dogruCevapString?.let { postMap.put("dogruCevapString", it) }
            odevArrayListSoru[position].dogruCevapImage?.let { postMap.put("dogruCevapImage", it) }

            db.collection("Odev").document(odevArrayListSoru[position].docRef).set(postMap)
                .addOnSuccessListener {
                    odevArrayListSoru.clear()
                    Toast.makeText(this.requireContext(),"Soru üste taşındı",Toast.LENGTH_SHORT).show()
                    adaptorSoru!!.notifyDataSetChanged()
                    getData()
                }.addOnFailureListener {
                    Toast.makeText(this.requireContext(),"Soru üste taşınamadı",Toast.LENGTH_SHORT).show()
                }

        }
    }


}