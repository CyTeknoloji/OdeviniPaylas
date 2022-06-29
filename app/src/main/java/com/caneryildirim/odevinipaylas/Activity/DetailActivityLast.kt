package com.caneryildirim.odevinipaylas.Activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.caneryildirim.odevinipaylas.Adaptors.Cevap
import com.caneryildirim.odevinipaylas.Adaptors.RecyclerCevapAdaptor
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.adaptorFeed
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dateFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.docRefFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapImageFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapStringFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.downloadUrlFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.pIdFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.position
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.positionDetail
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.selectedAciklamaFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.selectedDersFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.selectedKonuFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.takipciList
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.timeFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.userDisplayNameFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.userPhotoUrlFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.userUidFragment
import com.caneryildirim.odevinipaylas.Adaptors.Takipci
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.ActivityDetailLastBinding
import com.caneryildirim.odevinipaylas.databinding.CevapDetailAlertBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DetailActivityLast : AppCompatActivity(), RecyclerCevapAdaptor.Delete {
    private lateinit var binding:ActivityDetailLastBinding
    private lateinit var userUid:String
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private var cevapArrayList=ArrayList<Cevap>()
    private lateinit var adapterCevap:RecyclerCevapAdaptor
    private lateinit var docUuid:String

    private var selectedImage: Uri?=null
    private lateinit var imageUuid:String

    private lateinit var permiisonLauncher: ActivityResultLauncher<String>
    private lateinit var pickPhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickGalleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cropActivityGalleryResultLauncher: ActivityResultLauncher<Uri?>

    lateinit var currentPhotoPath: String
    private var requestFrom: String?=null

    private lateinit var referenceRegistration: ListenerRegistration

    private val cropActivityGalleryContract=object : ActivityResultContract<Uri?, Uri?>(){
        override fun createIntent(context: Context, input: Uri?): Intent {
            return CropImage.activity(input)
                .setCropMenuCropButtonTitle("Kırp").setRequestedSize(600,600)
                .getIntent(this@DetailActivityLast)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }

    private fun getTimeDate(date:Long):String{
        val netDate= Date(date)
        val sdf= SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault())
        return sdf.format(netDate)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDetailLastBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        auth= FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance()
        db= FirebaseFirestore.getInstance()
        binding.toolbarDetailLast.title="Soru Detayları"
        binding.toolbarDetailLast.setTitleTextColor(getColor(R.color.white))
        setSupportActionBar(binding.toolbarDetailLast)
        binding.toolbarDetailLast.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.progressBarDetailLast.visibility=View.INVISIBLE


        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent=Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        registerLauncher()

        val intent=intent
        dateFragment=intent.getLongExtra("date",0)
        downloadUrlFragment=intent.getStringExtra("downloadUrl")
        selectedAciklamaFragment=intent.getStringExtra("selectedAciklama")
        selectedDersFragment=intent.getStringExtra("selectedDers")
        selectedKonuFragment=intent.getStringExtra("selectedKonu")
        timeFragment=intent.getStringExtra("time")
        userDisplayNameFragment=intent.getStringExtra("userDisplayName")
        userPhotoUrlFragment=intent.getStringExtra("userPhotoUrl")
        userUidFragment=intent.getStringExtra("userUid")
        docRefFragment= intent.getStringExtra("docRef")
        position=intent.getIntExtra("position",0)
        if (position!=0){
            positionDetail= position
        }
        pIdFragment=intent.getStringExtra("pId")
        dogruCevapFragment=intent.getBooleanExtra("dogruCevap",false)
        dogruCevapStringFragment=intent.getStringExtra("dogruCevapString")
        dogruCevapImageFragment=intent.getStringExtra("dogruCevapImage")
        val info=intent.getStringExtra("info")

        userUid= userUidFragment.toString()

        getData()


        binding.recyclerDetailLast.layoutManager= LinearLayoutManager(this)
        adapterCevap= RecyclerCevapAdaptor(this,cevapArrayList,auth.currentUser!!.uid)
        binding.recyclerDetailLast.adapter=adapterCevap
        

        if (userPhotoUrlFragment=="null"){
            binding.imageUserProfileDetailLast.setImageResource(R.drawable.profileperson)
        }else{
            Picasso.get().load(userPhotoUrlFragment).into(binding.imageUserProfileDetailLast)
        }

        //Tarih ile ilgili işlemlerin başlangıcı

        val now= Timestamp.now().toDate().time
        val nowTime=getTimeDate(now)
        val firstTime=getTimeDate(Singleton.dateFragment!!)
        val format= SimpleDateFormat("dd/MM/yy HH:mm:ss")
        val firstDate=format.parse(firstTime)
        val nowDate= format.parse(nowTime)
        val diff=nowDate.time-firstDate.time

        val farkDakika=diff/(60*1000)
        val farkSaat=farkDakika/60
        val farkGun=farkSaat/24
        val farkHafta=farkGun/7
        val farkAy=farkHafta/4
        val farkYil=farkAy/12


        if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()==0 && farkGun.toInt()==0 && farkSaat.toInt()==0 && farkDakika.toInt()==0){
            binding.textTimeDetailLast.text="Şimdi"
        }else if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()==0 && farkGun.toInt()==0 && farkSaat.toInt()==0 && farkDakika.toInt()<61){
            binding.textTimeDetailLast.text="${farkDakika} Dk Önce"
        }else if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()==0 && farkGun.toInt()==0 && farkSaat.toInt()<25){
            binding.textTimeDetailLast.text="${farkSaat} Saat Önce"
        }else if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()==0 &&farkGun.toInt()<8){
            binding.textTimeDetailLast.text="${farkGun} Gün Önce"
        }else if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()<5){
            binding.textTimeDetailLast.text="${farkHafta} Hafta Önce"
        }else if (farkYil.toInt()==0 && farkAy.toInt()<13){
            binding.textTimeDetailLast.text="${farkAy} Ay Önce"
        }else if (farkYil.toInt()!=0){
            binding.textTimeDetailLast.text="${farkYil} Yıl Önce"
        }

        // Tarih ile ilgili işlemlerin sonu



        Picasso.get().load(downloadUrlFragment).into(binding.imageSoruDetailLast)

        binding.imageSoruDetailLast.setOnClickListener {
            val bindingAlert= CevapDetailAlertBinding.inflate(layoutInflater)
            val view=bindingAlert.root
            Picasso.get().load(downloadUrlFragment).into(bindingAlert.alertImage)
            val alert= AlertDialog.Builder(this)
            alert.setView(view)
            val builder=alert.create()
            builder.show()
            builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        binding.userNameTextDetailLast.text= userDisplayNameFragment
        binding.textDersDetailLast.text="Ders: "+ selectedDersFragment
        binding.textKonuDetailLast.text="Konu: "+ selectedKonuFragment
        binding.textAciklamaDetailLast.text="Açıklama: "+selectedAciklamaFragment

        db.collection("Users").document(auth.currentUser!!.uid).collection("Favoriler").get().addOnSuccessListener {

            if (!it.isEmpty){
                val documents=it.documents
                for (it in documents){
                    val favDoc=it.get("odevDoc") as String
                    if (favDoc== docRefFragment){
                        binding.checkBoxDetailLast.isChecked=true

                    }
                }
            }
        }

        binding.checkBoxDetailLast.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                val date= Timestamp.now()
                val odevDoc=docRefFragment
                val favoriteMap= hashMapOf<String,Any>()
                favoriteMap.put("date",date)
                favoriteMap.put("odevDoc",odevDoc!!)

                val reference=db.collection("Users").document(auth.currentUser!!.uid).collection("Favoriler").document(odevDoc)
                reference.set(favoriteMap).addOnSuccessListener {


                }.addOnFailureListener {
                    Toast.makeText(this,"Başarısız", Toast.LENGTH_SHORT).show()

                }


            }else{
                val odevDoc= docRefFragment
                val reference=db.collection("Users").document(auth.currentUser!!.uid).collection("Favoriler").document(odevDoc!!)
                reference.delete().addOnSuccessListener {


                }.addOnFailureListener {
                    Toast.makeText(this,"Başarısız", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.userNameTextDetailLast.setOnClickListener {
            val intent=Intent(this,ProfileWatchActivity::class.java)
            intent.putExtra("userUidFragment", userUidFragment)
            intent.putExtra("userPhotoFragment", userPhotoUrlFragment)
            intent.putExtra("userNameFragment", userDisplayNameFragment)
            startActivity(intent)
        }

        binding.imageUserProfileDetailLast.setOnClickListener {
            val intent=Intent(this,ProfileWatchActivity::class.java)
            intent.putExtra("userUidFragment", userUidFragment)
            intent.putExtra("userPhotoFragment", userPhotoUrlFragment)
            intent.putExtra("userNameFragment", userDisplayNameFragment)
            startActivity(intent)
        }

        binding.detailLastPopupMenu.setOnClickListener {
            if (auth.currentUser!!.uid==userUid || auth.currentUser!!.uid=="P2dukbTHNMcdyP3FjDOsd7fR6PT2"){
                val popup= PopupMenu(this,it)
                popup.menuInflater.inflate(R.menu.menu_delete,popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener {
                    if (it.itemId==R.id.menu_delete){
                        val reference=storage.reference.child("images").child("${docRefFragment}.jpg")
                        reference.delete().addOnSuccessListener {
                            db.collection("Odev").document(docRefFragment!!).delete().addOnSuccessListener {
                                Toast.makeText(this,"Soru Silindi", Toast.LENGTH_SHORT).show()
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this,it.localizedMessage, Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this,it.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                        true
                    }else if (it.itemId==R.id.menu_ustecikar){
                        if (farkGun.toInt()<2){
                            Toast.makeText(this,"En az 2 gün geçmesi gerekli", Toast.LENGTH_SHORT).show()
                        }else {

                            if (dogruCevapFragment == true) {
                                Toast.makeText(
                                    this,
                                    "Çözülmüş bir soruyu üste taşıyamazsın",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val calendar = Calendar.getInstance()
                                val day = calendar.get(Calendar.DAY_OF_MONTH)
                                val month = calendar.get(Calendar.MONTH) + 1
                                val year = calendar.get(Calendar.YEAR)
                                val time = "${day}.${month}.${year}"

                                val postMap = hashMapOf<String, Any>()
                                downloadUrlFragment?.let { it1 -> postMap.put("downloadUrl", it1) }
                                selectedAciklamaFragment?.let { it1 ->
                                    postMap.put(
                                        "selectedAciklama",
                                        it1
                                    )
                                }
                                selectedDersFragment?.let { it1 ->
                                    postMap.put(
                                        "selectedDers",
                                        it1
                                    )
                                }
                                selectedKonuFragment?.let { it1 ->
                                    postMap.put(
                                        "selectedKonu",
                                        it1
                                    )
                                }
                                userUidFragment?.let { it1 -> postMap.put("userUid", it1) }
                                postMap.put("email", auth.currentUser!!.email.toString())
                                userDisplayNameFragment?.let { it1 ->
                                    postMap.put(
                                        "userDisplayName",
                                        it1
                                    )
                                }
                                userPhotoUrlFragment?.let { it1 ->
                                    postMap.put(
                                        "userPhotoUrl",
                                        it1
                                    )
                                }
                                postMap.put("date", Timestamp.now())
                                postMap.put("time", time)
                                docRefFragment?.let { it1 -> postMap.put("docRef", it1) }
                                postMap.put("dogruCevap", dogruCevapFragment!!)
                                pIdFragment?.let { it1 -> postMap.put("pId", it1) }
                                dogruCevapStringFragment?.let { it1 ->
                                    postMap.put(
                                        "dogruCevapString",
                                        it1
                                    )
                                }
                                dogruCevapImageFragment?.let { it1 ->
                                    postMap.put(
                                        "dogruCevapImage",
                                        it1
                                    )
                                }

                                if (docRefFragment != null) {
                                    db.collection("Odev").document(docRefFragment!!).set(postMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this,
                                                "Soru üste taşındı",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                this,
                                                "Soru üste taşınamadı",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }


                            }
                        }
                        true
                    }else{
                        false
                    }
                }
            }else{
                val popup= PopupMenu(this,it)
                popup.menuInflater.inflate(R.menu.menu_block,popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener {
                    if (it.itemId==R.id.menu_block){
                        val alert= AlertDialog.Builder(this)
                        alert.setTitle("Soruyu Şikayet Et")
                        alert.setMessage("Emin misin?")
                        alert.setNegativeButton("Hayır"){ dialogInterface: DialogInterface, i: Int ->
                            Toast.makeText(this,"Vazgeçildi", Toast.LENGTH_SHORT).show()
                        }
                        alert.setPositiveButton("Evet"){ dialogInterface: DialogInterface, i: Int ->

                            val soruUid= docRefFragment
                            val userEmail= userDisplayNameFragment
                            val userUid= userUidFragment
                            val imageUuid= docRefFragment
                            val date= Timestamp.now()
                            val uuidBildirim= UUID.randomUUID().toString()
                            val calendar= Calendar.getInstance()
                            val day=calendar.get(Calendar.DAY_OF_MONTH)
                            val month=calendar.get(Calendar.MONTH)+1
                            val year=calendar.get(Calendar.YEAR)
                            val time="${day}.${month}.${year}"
                            val bildirimMap= hashMapOf<String,Any>()
                            bildirimMap.put("soruUid",soruUid!!)
                            bildirimMap.put("userName","${userEmail} sorusu şikayet edildi")
                            bildirimMap.put("userUid",userUid!!)
                            bildirimMap.put("docUid",imageUuid!!)
                            bildirimMap.put("date",date)
                            bildirimMap.put("docBildirim",uuidBildirim)
                            bildirimMap.put("time",time)
                            bildirimMap.put("okundu",false)
                            db.collection("Users").document("P2dukbTHNMcdyP3FjDOsd7fR6PT2").collection("Bildirimler").document(uuidBildirim).set(bildirimMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this,"Şikayet edildi", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this,it.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                        }
                        alert.show()
                        true
                    }else{
                        false
                    }
                }
            }
        }




    }

    private fun getData(){
        val reference=db.collection("Cevap").document(docRefFragment!!).collection("Cevaplar").orderBy("date",Query.Direction.ASCENDING)
        referenceRegistration= reference.addSnapshotListener { it, error ->
            if (it!=null){
                if (!it.isEmpty){
                    binding.textCevapYokLast.visibility=View.GONE
                    val documents=it.documents
                    takipciList.clear()
                    cevapArrayList.clear()
                    for (doc in documents){
                        val downloadUrl=doc.get("downloadUrl") as String
                        val selectedAciklama=doc.get("selectedAciklama") as String
                        val soruUid=doc.get("soruUid") as String
                        val userEmail=doc.get("userEmail") as String
                        val userPhotoUrl=doc.get("userPhotoUrl") as String?
                        val userUid=doc.get("userUid") as String
                        docUuid=doc.get("docUuid") as String
                        val dogruCevap=doc.get("dogruCevap") as Boolean
                        val yanlisCevap=doc.get("yanlisCevap") as Boolean?
                        val cevapYorum=doc.get("cevapYorum") as String
                        val pIdCevap=doc.get("pIdCevap") as String?
                        val date=doc.get("date") as Timestamp
                        val cevap=Cevap(downloadUrl,selectedAciklama,soruUid,userEmail,userUid,docUuid!!,dogruCevap,cevapYorum,pIdCevap,userPhotoUrl,date,yanlisCevap)
                        cevapArrayList.add(cevap)

                        adapterCevap.notifyDataSetChanged()

                        if (pIdCevap!=null && userUid!= Singleton.userUidFragment){
                            val takipci= Takipci(userUid,pIdCevap)
                            Singleton.takipciList.add(takipci)
                        }


                    }


                }else{
                    binding.textCevapYokLast.visibility=View.VISIBLE
                    takipciList.clear()
                }
            }else{
                takipciList.clear()
            }
        }
    }

    override fun onItemDelete(position: Int) {
        val reference=storage.reference.child("cevaplar").child(docRefFragment!!).child("${cevapArrayList[position].docUuid}.jpg")
        reference.delete().addOnSuccessListener {
            val refDb=db.collection("Cevap").document(docRefFragment!!).collection("Cevaplar").document(cevapArrayList[position].docUuid)
            refDb.delete().addOnSuccessListener {
                if (cevapArrayList[position].dogruCevap==true){
                    val referenceOdev=db.collection("Odev").document(cevapArrayList[position].soruUid)
                    referenceOdev.update("dogruCevap",false).addOnSuccessListener {}
                    referenceOdev.update("dogruCevapString","null").addOnSuccessListener {}
                    referenceOdev.update("dogruCevapImage","null").addOnSuccessListener {}
                }

                Toast.makeText(this,"Cevabın silindi",Toast.LENGTH_SHORT).show()
                cevapArrayList.clear()
                adapterCevap.notifyDataSetChanged()

                //getData()     
            // dikkat et yeni iptal ettin    07.06.2022 kontrolleri yapılmadı


            }.addOnFailureListener {
                Toast.makeText(this,"Cevap silinirken sorun oluştu", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            val refDb=db.collection("Cevap").document(docRefFragment!!).collection("Cevaplar").document(cevapArrayList[position].docUuid)
            refDb.delete().addOnSuccessListener {
                if (cevapArrayList[position].dogruCevap==true){
                    val referenceOdev=db.collection("Odev").document(cevapArrayList[position].soruUid)
                    referenceOdev.update("dogruCevap",false).addOnSuccessListener {

                    }
                }

                Toast.makeText(this,"Cevabın silindi",Toast.LENGTH_SHORT).show()
                cevapArrayList.clear()
                adapterCevap.notifyDataSetChanged()
                //getData()     //dikkat et yeni iptal ettin

            }.addOnFailureListener {
                Toast.makeText(this,"Cevap silinirken sorun oluştu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun sikayetItem(position: Int) {
        val alert=AlertDialog.Builder(this)
        alert.setTitle("Cevabı Şikayet Et")
        alert.setMessage("Emin misin?")
        alert.setNegativeButton("Hayır"){ dialogInterface: DialogInterface, i: Int ->
            Toast.makeText(this,"Vazgeçildi",Toast.LENGTH_SHORT).show()
        }
        alert.setPositiveButton("Evet"){ dialogInterface: DialogInterface, i: Int ->
            val soruUid= cevapArrayList[position].soruUid
            val userEmail=cevapArrayList[position].userEmail
            val userUid=cevapArrayList[position].userUid
            val imageUuid=cevapArrayList[position].docUuid
            val date=Timestamp.now()
            val uuidBildirim= UUID.randomUUID().toString()
            val calendar=Calendar.getInstance()
            val day=calendar.get(Calendar.DAY_OF_MONTH)
            val month=calendar.get(Calendar.MONTH)+1
            val year=calendar.get(Calendar.YEAR)
            val time="${day}.${month}.${year}"
            val bildirimMap= hashMapOf<String,Any>()
            bildirimMap.put("soruUid",soruUid)
            bildirimMap.put("userName","${userEmail} cevabı şikayet edildi")
            bildirimMap.put("userUid",userUid)
            bildirimMap.put("docUid",imageUuid)
            bildirimMap.put("date",date)
            bildirimMap.put("docBildirim",uuidBildirim)
            bildirimMap.put("time",time)
            bildirimMap.put("okundu",false)
            db.collection("Users").document("P2dukbTHNMcdyP3FjDOsd7fR6PT2").collection("Bildirimler").document(uuidBildirim).set(bildirimMap)
                .addOnSuccessListener {
                    Toast.makeText(this,"Şikayet edildi",Toast.LENGTH_SHORT).show()
                    db.collection("Admin").document("P2dukbTHNMcdyP3FjDOsd7fR6PT2")
                        .get().addOnSuccessListener {
                            if (it!=null){
                                val pidAdmin=it.get("pId") as String
                                try {
                                    OneSignal.postNotification("{'contents': {'en':'Cevaba Şikayet Geldi'}, 'include_player_ids': ['" + pidAdmin + "']}",null)

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
        //Singleton.docUuidFragment =cevapArrayList[position].docUuid
        //Singleton.dogruCevapDuzenle =cevapArrayList[position].dogruCevap
        //Singleton.cevapYorumFragment =cevapArrayList[position].cevapYorum
        //val intent= Intent(this, CevapActivity::class.java)
        //startActivity(intent)
        //Singleton.downloadUrlCevap =cevapArrayList[position].downloadUrl
        //Singleton.aciklamaCevap =cevapArrayList[position].selectedAciklama
    }

    override fun guncelleItem(position: Int) {
        if (cevapArrayList[position].dogruCevap!=true){
            val reference=db.collection("Cevap").document(cevapArrayList[position].soruUid).collection("Cevaplar").document(cevapArrayList[position].docUuid)
            reference.update("dogruCevap",true).addOnSuccessListener {

                val calendar=Calendar.getInstance()
                val day=calendar.get(Calendar.DAY_OF_MONTH)
                val month=calendar.get(Calendar.MONTH)+1
                val year=calendar.get(Calendar.YEAR)
                val time="${day}.${month}.${year}"

                val soruUid= docRefFragment!!
                val userEmail=auth.currentUser!!.displayName.toString()
                val userUid=auth.currentUser!!.uid
                val date= Timestamp.now()
                val pIdCevap=OneSignal.getDeviceState()?.userId
                val imageUuid=userUid

                val uuidBildirim= UUID.randomUUID().toString()
                val bildirimMap= hashMapOf<String,Any>()
                bildirimMap.put("soruUid",soruUid)
                bildirimMap.put("userName","${userEmail} cevabını doğru olarak işaretledi")
                bildirimMap.put("userUid",userUid)
                bildirimMap.put("docUid",imageUuid)
                bildirimMap.put("date",date)
                bildirimMap.put("docBildirim",uuidBildirim)
                bildirimMap.put("time",time)
                bildirimMap.put("okundu",false)
                db.collection("Users").document(cevapArrayList[position].userUid).collection("Bildirimler").document(uuidBildirim).set(bildirimMap).addOnSuccessListener {

                    try {
                        OneSignal.postNotification("{'contents': {'en':'Cevabın doğru cevap olarak işaretlendi'}, 'include_player_ids': ['" + cevapArrayList[position].pIdCevap + "']}",null)
                    }catch (e: JSONException){
                        e.printStackTrace()
                    }

                    val referenceOdev=db.collection("Odev").document(cevapArrayList[position].soruUid)
                    referenceOdev.update("dogruCevap",true).addOnSuccessListener {}
                    referenceOdev.update("dogruCevapString",cevapArrayList[position].selectedAciklama).addOnSuccessListener {}
                    referenceOdev.update("dogruCevapImage",cevapArrayList[position].downloadUrl).addOnSuccessListener {}

                    val uuidTakipBildirim=UUID.randomUUID().toString()
                    val takipBildirimMap= hashMapOf<String,Any>()
                    takipBildirimMap.put("soruUid",soruUid)
                    takipBildirimMap.put("userName","Takip ettiğin soru çözüldü")
                    takipBildirimMap.put("userUid",userUid)
                    takipBildirimMap.put("docUid",imageUuid)
                    takipBildirimMap.put("date",date)
                    takipBildirimMap.put("docBildirim",uuidTakipBildirim)
                    takipBildirimMap.put("time",time)
                    bildirimMap.put("okundu",false)

                    if (Singleton.takipciList.isNotEmpty()){
                        if (Singleton.takipciList.size>0){
                            for (takipci in Singleton.takipciList){
                                if (takipci.takipciUid!= userUidFragment && takipci.takipciUid!=cevapArrayList[position].userUid){
                                    db.collection("Users").document(takipci.takipciUid).collection("Bildirimler").document(uuidTakipBildirim).set(takipBildirimMap).addOnSuccessListener {
                                        try {
                                            OneSignal.postNotification("{'contents': {'en':'Takip ettiğin soru çözüldü'}, 'include_player_ids': ['" + takipci.takipciPid + "']}",null)
                                        }catch (e: JSONException){
                                            e.printStackTrace()
                                        }
                                    }.addOnFailureListener {
                                        println(it.localizedMessage)
                                    }
                                }
                            }
                        }

                    }


                }.addOnFailureListener {
                    Toast.makeText(this,"Başarısız",Toast.LENGTH_SHORT).show()
                }


            }.addOnFailureListener {
                Toast.makeText(this,"Başarısız",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this,"Cevap doğru olarak seçilmiş",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(position: Int) {
        val bindingAlert= CevapDetailAlertBinding.inflate(layoutInflater)
        val view=bindingAlert.root
        Picasso.get().load(cevapArrayList[position].downloadUrl).into(bindingAlert.alertImage)
        val alert=AlertDialog.Builder(this)
        alert.setView(view)
        val builder=alert.create()
        builder.show()
        builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun selectCamera(view: View){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                Snackbar.make(view,"Görsel yüklenmesi için izin gerekli", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver",View.OnClickListener {
                    requestFrom="Camera"
                    permiisonLauncher.launch(Manifest.permission.CAMERA)
                }).show()
            }else{
                requestFrom="Camera"
                permiisonLauncher.launch(Manifest.permission.CAMERA)
            }
        }else{
            dispatchTakePictureIntent()
        }
    }

    fun selectMedia(view: View){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Görsel yüklenmesi için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver",View.OnClickListener {
                    requestFrom="Gallery"
                    permiisonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                requestFrom="Gallery"
                permiisonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            val intentGallery=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickGalleryLauncher.launch(intentGallery)

        }
    }

    fun yukle(view: View){
        if (binding.cevapTextLast.text.toString().trim().isEmpty()&&selectedImage==null){
            Toast.makeText(this,"Görsel ya da yorumdan oluşan cevabınız olmalı",Toast.LENGTH_SHORT).show()
            //binding.progressBarCevap.visibility=View.INVISIBLE
            binding.progressBarDetailLast.visibility=View.INVISIBLE
        }else{
            imageUuid=UUID.randomUUID().toString()
            val calendar=Calendar.getInstance()
            val day=calendar.get(Calendar.DAY_OF_MONTH)
            val month=calendar.get(Calendar.MONTH)+1
            val year=calendar.get(Calendar.YEAR)
            val time="${day}.${month}.${year}"

            val soruUid= docRefFragment!!
            val imageName="${imageUuid}.jpg"
            val selectedAciklama=binding.cevapTextLast.text.toString().trim()
            val date=Timestamp.now()
            val userEmail=auth.currentUser!!.displayName.toString()     //email yazıyor ama userName aldın
            val userPhotoUrl=auth.currentUser!!.photoUrl.toString()
            val userUid=auth.currentUser!!.uid
            val pIdCevap=OneSignal.getDeviceState()?.userId
            val reference=storage.reference.child("cevaplar").child(soruUid).child(imageName)
            if (selectedImage!=null){
                binding.cevapGonderLast.isEnabled=false
                binding.selectCameraLast.isClickable=false
                binding.selectMediaLast.isClickable=false
                //binding.progressBarCevap.visibility=View.VISIBLE
                binding.progressBarDetailLast.visibility=View.VISIBLE
                reference.putFile(selectedImage!!).addOnSuccessListener {
                    val uploadImageReference=storage.reference.child("cevaplar").child(soruUid).child(imageName)
                    uploadImageReference.downloadUrl.addOnSuccessListener {
                        val downloadUrl=it.toString()
                        val cevapMap= hashMapOf<String,Any>()
                        cevapMap.put("downloadUrl",downloadUrl)
                        cevapMap.put("selectedAciklama",selectedAciklama)
                        cevapMap.put("date",date)
                        cevapMap.put("userEmail",userEmail)
                        cevapMap.put("userPhotoUrl",userPhotoUrl)
                        cevapMap.put("userUid",userUid)
                        cevapMap.put("soruUid",soruUid)
                        cevapMap.put("docUuid",imageUuid)
                        cevapMap.put("dogruCevap",false)
                        cevapMap.put("yanlisCevap",false)
                        cevapMap.put("cevapYorum","null")
                        if (pIdCevap != null) {
                            cevapMap.put("pIdCevap",pIdCevap)
                        }

                        db.collection("Cevap").document(soruUid).collection("Cevaplar").document(imageUuid)
                            .set(cevapMap).addOnSuccessListener {
                                binding.cevapGonderLast.isEnabled=true
                                binding.selectCameraLast.isClickable=true
                                binding.selectMediaLast.isClickable=true
                                binding.progressBarDetailLast.visibility=View.INVISIBLE
                                cevapArrayList.clear()
                                adapterCevap.notifyDataSetChanged()
                                getData()    //buradan sorun çıkıyor olabilir 07.06.2022 kontroller yapılmadı
                                binding.cevapTextLast.setText("")
                                binding.selectCameraLast.setImageResource(R.drawable.cameraselect)
                                binding.selectMediaLast.setImageResource(R.drawable.mediaselected)
                                Toast.makeText(this,"Cevabın Yüklendi",Toast.LENGTH_SHORT).show()
                                selectedImage=null

                                val uuidTakipBildirim=UUID.randomUUID().toString()
                                val takipBildirimMap= hashMapOf<String,Any>()
                                takipBildirimMap.put("soruUid",soruUid)
                                takipBildirimMap.put("userName","Takip ettiğin soruya cevap geldi")
                                takipBildirimMap.put("userUid",userUid)
                                takipBildirimMap.put("docUid",imageUuid)
                                takipBildirimMap.put("date",date)
                                takipBildirimMap.put("docBildirim",uuidTakipBildirim)
                                takipBildirimMap.put("time",time)
                                takipBildirimMap.put("okundu",false)

                                if (Singleton.takipciList.isNotEmpty()){
                                    if (Singleton.takipciList.size>0){
                                        for (takipci in Singleton.takipciList){
                                            if (takipci.takipciUid!=userUidFragment && takipci.takipciUid!=userUid){
                                                db.collection("Users").document(takipci.takipciUid).collection("Bildirimler").document(uuidTakipBildirim).set(takipBildirimMap).addOnSuccessListener {
                                                    try {
                                                        OneSignal.postNotification("{'contents': {'en':'Takip ettiğin soruya cevap geldi'}, 'include_player_ids': ['" + takipci.takipciPid + "']}",null)
                                                    }catch (e: JSONException){
                                                        e.printStackTrace()
                                                    }
                                                }.addOnFailureListener {
                                                    println(it.localizedMessage)
                                                }
                                            }
                                        }
                                    }

                                }


                                if (auth.currentUser!!.uid!= userUidFragment){
                                    val uuidBildirim=UUID.randomUUID().toString()
                                    val bildirimMap= hashMapOf<String,Any>()
                                    bildirimMap.put("soruUid",soruUid)
                                    bildirimMap.put("userName","${userEmail} sorunu yanıtladı")
                                    bildirimMap.put("userUid",userUid)
                                    bildirimMap.put("docUid",imageUuid)
                                    bildirimMap.put("date",date)
                                    bildirimMap.put("docBildirim",uuidBildirim)
                                    bildirimMap.put("time",time)
                                    bildirimMap.put("okundu",false)

                                    db.collection("Users").document(userUidFragment!!).collection("Bildirimler").document(uuidBildirim).set(bildirimMap).addOnSuccessListener {
                                        if (pIdCevap!= pIdFragment){     //bildirim gitmiyorsa ilk buraya bak
                                            try {
                                                OneSignal.postNotification("{'contents': {'en':'Soruna Cevap Geldi'}, 'include_player_ids': ['" + pIdFragment + "']}",null)

                                            }catch (e: JSONException){
                                                e.printStackTrace()
                                            }
                                        }
                                        //Singleton.docUuidFragment ="null"
                                        //finish()

                                    }.addOnFailureListener {

                                    }
                                }

                            }.addOnFailureListener {
                                Toast.makeText(this,"Cevap Yüklenemedi",Toast.LENGTH_LONG).show()
                                //binding.progressBarCevap.visibility=View.INVISIBLE
                                binding.progressBarDetailLast.visibility=View.INVISIBLE
                                binding.cevapGonderLast.isEnabled=true
                                binding.selectCameraLast.isClickable=true
                                binding.selectMediaLast.isClickable=true
                            }
                    }.addOnFailureListener {
                        Toast.makeText(this,"Cevap Yüklenemedi",Toast.LENGTH_LONG).show()
                        //binding.progressBarCevap.visibility=View.INVISIBLE
                        binding.progressBarDetailLast.visibility=View.INVISIBLE
                        binding.cevapGonderLast.isEnabled=true
                        binding.selectCameraLast.isClickable=true
                        binding.selectMediaLast.isClickable=true
                    }
                }.addOnFailureListener {
                    Toast.makeText(this,"Cevap Yüklenemedi",Toast.LENGTH_LONG).show()
                    //binding.progressBarCevap.visibility=View.INVISIBLE
                    binding.progressBarDetailLast.visibility=View.INVISIBLE
                    binding.cevapGonderLast.isEnabled=true
                    binding.selectCameraLast.isClickable=true
                    binding.selectMediaLast.isClickable=true
                }

            }else{
                //binding.progressBarCevap.visibility=View.VISIBLE
                binding.progressBarDetailLast.visibility=View.VISIBLE

                binding.cevapGonderLast.isEnabled=false
                binding.selectCameraLast.isClickable=false
                binding.selectMediaLast.isClickable=false
                val cevapMap= hashMapOf<String,Any>()
                cevapMap.put("downloadUrl","null")
                cevapMap.put("selectedAciklama",selectedAciklama)
                cevapMap.put("date",date)
                cevapMap.put("userEmail",userEmail)
                cevapMap.put("userPhotoUrl",userPhotoUrl)
                cevapMap.put("userUid",userUid)
                cevapMap.put("soruUid",soruUid!!)
                cevapMap.put("docUuid",imageUuid)
                cevapMap.put("dogruCevap",false)
                cevapMap.put("yanlisCevap",false)
                cevapMap.put("cevapYorum","null")
                if (pIdCevap != null) {
                    cevapMap.put("pIdCevap",pIdCevap)
                }

                val referenceNew=db.collection("Cevap").document(soruUid).collection("Cevaplar").document(imageUuid)
                referenceNew.set(cevapMap).addOnSuccessListener {
                    binding.cevapGonderLast.isEnabled=true
                    binding.selectCameraLast.isClickable=true
                    binding.selectMediaLast.isClickable=true
                    binding.progressBarDetailLast.visibility=View.INVISIBLE
                    cevapArrayList.clear()
                    adapterCevap.notifyDataSetChanged()
                    getData()     //buradan sorun çıkıyor olabilir
                    binding.cevapTextLast.setText("")
                    binding.selectCameraLast.setImageResource(R.drawable.cameraselect)
                    binding.selectMediaLast.setImageResource(R.drawable.mediaselected)
                    Toast.makeText(this,"Cevabın Yüklendi",Toast.LENGTH_SHORT).show()



                    val uuidTakipBildirim=UUID.randomUUID().toString()
                    val takipBildirimMap= hashMapOf<String,Any>()
                    takipBildirimMap.put("soruUid",soruUid)
                    takipBildirimMap.put("userName","Takip ettiğin soruya cevap geldi")
                    takipBildirimMap.put("userUid",userUid)
                    takipBildirimMap.put("docUid",imageUuid)
                    takipBildirimMap.put("date",date)
                    takipBildirimMap.put("docBildirim",uuidTakipBildirim)
                    takipBildirimMap.put("time",time)
                    takipBildirimMap.put("okundu",false)

                    if (Singleton.takipciList.isNotEmpty()){
                        if (Singleton.takipciList.size>0){
                            for (takipci in Singleton.takipciList){
                                if (takipci.takipciUid!=userUidFragment && takipci.takipciUid!=userUid){
                                    db.collection("Users").document(takipci.takipciUid).collection("Bildirimler").document(uuidTakipBildirim).set(takipBildirimMap).addOnSuccessListener {
                                        try {
                                            OneSignal.postNotification("{'contents': {'en':'Takip ettiğin soruya cevap geldi'}, 'include_player_ids': ['" + takipci.takipciPid + "']}",null)
                                        }catch (e: JSONException){
                                            e.printStackTrace()
                                        }
                                    }.addOnFailureListener {
                                        println(it.localizedMessage)
                                    }
                                }
                            }
                        }

                    }

                    if (auth.currentUser!!.uid!= userUidFragment){
                        val uuidBildirim=UUID.randomUUID().toString()
                        val bildirimMap= hashMapOf<String,Any>()
                        bildirimMap.put("soruUid",soruUid)
                        bildirimMap.put("userName","${userEmail} sorunu yanıtladı")
                        bildirimMap.put("userUid",userUid)
                        bildirimMap.put("docUid",imageUuid)
                        bildirimMap.put("date",date)
                        bildirimMap.put("docBildirim",uuidBildirim)
                        bildirimMap.put("time",time)
                        bildirimMap.put("okundu",false)

                        db.collection("Users").document(userUidFragment!!).collection("Bildirimler").document(uuidBildirim).set(bildirimMap).addOnSuccessListener {
                            if (pIdCevap!= pIdFragment){     //bildirim gitmiyorsa ilk buraya bak
                                try {
                                    OneSignal.postNotification("{'contents': {'en':'Soruna Cevap Geldi'}, 'include_player_ids': ['" + pIdFragment + "']}",null)

                                }catch (e: JSONException){
                                    e.printStackTrace()
                                }
                            }
                            //Singleton.docUuidFragment ="null"
                            //finish()


                        }.addOnFailureListener {

                        }
                    }


                }.addOnFailureListener {
                    Toast.makeText(this,"Cevap Yüklenemedi",Toast.LENGTH_LONG).show()
                    //binding.progressBarCevap.visibility=View.INVISIBLE
                    binding.progressBarDetailLast.visibility=View.INVISIBLE
                    binding.cevapGonderLast.isEnabled=true
                    binding.selectMediaLast.isClickable=true
                    binding.selectCameraLast.isClickable=true
                }
            }
        }
    }

    fun registerLauncher(){
        cropActivityGalleryResultLauncher=registerForActivityResult(cropActivityGalleryContract){uri->
            uri.let {
                //binding.cevapImage.setImageURI(it)
                selectedImage=it
                binding.selectMediaLast.setImageResource(R.drawable.mediaselectok)
                binding.selectCameraLast.setImageResource(R.drawable.cameraselectok)
            }
        }

        pickPhotoLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if (result.resultCode== RESULT_OK){
                //setPic()
                val file= File(currentPhotoPath)
                val uri=Uri.fromFile(file)
                cropActivityGalleryResultLauncher.launch(uri)
            }
        }

        pickGalleryLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if (result.resultCode== RESULT_OK){
                val intentData=result.data
                if (intentData!=null){
                    val uri= intentData.data
                    cropActivityGalleryResultLauncher.launch(uri)
                }
            }
        }

        permiisonLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                if (requestFrom!=null){
                    if (requestFrom=="Camera"){
                        dispatchTakePictureIntent()
                    }else if (requestFrom=="Gallery"){
                        val intentGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        pickGalleryLauncher.launch(intentGallery)
                    }
                }

            }else{
                Toast.makeText(this,"İzin verilmedi",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        referenceRegistration.remove()
    }

    override fun onPause() {
        super.onPause()
        referenceRegistration.remove()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        referenceRegistration.remove()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.caneryildirim.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    pickPhotoLauncher.launch(takePictureIntent)


                }
            }
        }
    }
}