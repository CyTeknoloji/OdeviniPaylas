package com.caneryildirim.odevinipaylas.Activity

import android.Manifest

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment

import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts


import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.caneryildirim.odevinipaylas.Adaptors.Singleton


import com.caneryildirim.odevinipaylas.Adaptors.Singleton.ONESIGNAL_APP_ID
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapImageUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapStringUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.downloadUrlUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.position
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.selectedAciklamaUpload
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.uuidUpload


import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.ActivityUploadNewBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.storage.FirebaseStorage

import com.onesignal.OneSignal
import com.squareup.picasso.Picasso


import java.util.*
import kotlin.collections.ArrayList

import com.theartofdev.edmodo.cropper.CropImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat


class UploadActivityNew : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var firestore:FirebaseFirestore
    private lateinit var storage:FirebaseStorage



    private lateinit var binding: ActivityUploadNewBinding


    private val ders=ArrayList<String>()
    private val konuMatematikGKGY=ArrayList<String>()
    private val konuGeometriGKGY=ArrayList<String>()
    private val konuTurkceGKGY=ArrayList<String>()
    private val konuTarihGKGY=ArrayList<String>()
    private val konuCografyaGKGY=ArrayList<String>()
    private val konuVatandaslikGKGY=ArrayList<String>()
    private val konuOgretimIlkeYontem=ArrayList<String>()
    private val konuSinifYonetimi=ArrayList<String>()
    private val konuOgretimveMateryal=ArrayList<String>()
    private val konuProgramGelistirme=ArrayList<String>()
    private val konuOlcme=ArrayList<String>()
    private val konuOgrenmePsik=ArrayList<String>()
    private val konuGelisimPsik=ArrayList<String>()
    private val konuRehberlik=ArrayList<String>()
    private val konuTurkceOABT=ArrayList<String>()
    private val konuMatematikIlkogretimOABT=ArrayList<String>()
    private val konuFenOABT=ArrayList<String>()
    private val konuSosyalBilimlerOABT=ArrayList<String>()
    private val konuEdebiyatOABT=ArrayList<String>()
    private val konuTarihOABT=ArrayList<String>()
    private val konuCografyaOABT=ArrayList<String>()
    private val konuMatematikLiseOABT=ArrayList<String>()
    private val konuFizikOABT=ArrayList<String>()
    private val konuKimyaOABT=ArrayList<String>()
    private val konuBiyolojiOABT=ArrayList<String>()
    private val konuDinOABT=ArrayList<String>()
    private val konuIngilizceOabt=ArrayList<String>()
    private val konuRehberOgretmenOABT=ArrayList<String>()
    private val konuSinifOABT=ArrayList<String>()
    private val konuOkulOncesiOABT=ArrayList<String>()
    private val konuBedenOABT=ArrayList<String>()
    private val konuKamuYon=ArrayList<String>()
    private val konuUluslararasIliskiler=ArrayList<String>()
    private val konuCeko=ArrayList<String>()
    private val konuHukuk=ArrayList<String>()
    private val konuIktisat=ArrayList<String>()
    private val konuMaliye=ArrayList<String>()
    private val konuIsletme=ArrayList<String>()
    private val konuMuhasebe=ArrayList<String>()
    private val konuIstatistik=ArrayList<String>()

    private var uuid:String?=null
    private val konuBos=ArrayList<String>()
    private lateinit var dersAdaptor:ArrayAdapter<String>
    private lateinit var konuAdaptor:ArrayAdapter<String>
    private var dersPosition:Int?=null
    private lateinit var konuPosition:String
    private var selectedImage:Uri?=null
    private lateinit var permiisonLauncher:ActivityResultLauncher<String>
    private lateinit var pickPhotoLauncher:ActivityResultLauncher<Intent>
    private lateinit var pickGalleryLauncher:ActivityResultLauncher<Intent>
    private lateinit var cropActivityGalleryResultLauncher: ActivityResultLauncher<Uri?>

    lateinit var currentPhotoPath: String
    private var requestFrom: String?=null

    private val cropActivityGalleryContract=object :ActivityResultContract<Uri?,Uri?>(){
        override fun createIntent(context: Context, input: Uri?): Intent {
            return CropImage.activity(input)
                .setCropMenuCropButtonTitle("Kırp").setRequestedSize(600,600)
                .getIntent(this@UploadActivityNew)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadNewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.progressBarUpdate.visibility=View.INVISIBLE

        binding.toolbar.title="Sorunu Yükle"
        binding.toolbar.setTitleTextColor(getColor(R.color.white))
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()

        }
        dersler()
        konular()

        registerLauncher()


        auth= FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance()
        firestore= FirebaseFirestore.getInstance()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent=Intent(this,NotificationActivity::class.java)
            startActivity(intent)
        }




        if (auth.currentUser==null){
            finish()
        }

        if (downloadUrlUpload!=null){
            Picasso.get().load(downloadUrlUpload).into(binding.imageViewUpload)
        }else{
            binding.imageViewUpload.setImageResource(R.drawable.gorselsec)
        }

        if (selectedAciklamaUpload!=null){
            binding.editTextAciklama.setText(selectedAciklamaUpload)
        }



        binding.spinnerDers.onItemSelectedListener= object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                dersPosition=position
                if (position==0){
                            val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuBos)
                            binding.spinnerKonu.adapter=adaptor
                }else if (position==1){
                            val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuMatematikGKGY)
                            binding.spinnerKonu.adapter=adaptor
                }else if (position==2){
                            val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuGeometriGKGY)
                            binding.spinnerKonu.adapter=adaptor
                }else if (position==3){
                            val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuTurkceGKGY)
                            binding.spinnerKonu.adapter=adaptor
                }else if (position==4){
                            val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuTarihGKGY)
                            binding.spinnerKonu.adapter=adaptor
                }else if (position==5){
                            val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuCografyaGKGY)
                            binding.spinnerKonu.adapter=adaptor
                }else if (position==6){
                            val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuVatandaslikGKGY)
                            binding.spinnerKonu.adapter=adaptor
                }else if (position==7){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuOgretimIlkeYontem)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==8){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuSinifYonetimi)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==9){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuOgretimveMateryal)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==10){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuProgramGelistirme)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==11){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuOlcme)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==12){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuOgrenmePsik)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==13){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuGelisimPsik)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==14){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuRehberlik)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==15){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuTurkceOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==16){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuMatematikIlkogretimOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==17){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuFenOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==18){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuSosyalBilimlerOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==19){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuEdebiyatOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==20){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuTarihOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==21){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuCografyaOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==22){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuMatematikLiseOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==23){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuFizikOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==24){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuKimyaOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==25){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuBiyolojiOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==26){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuDinOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==27){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuIngilizceOabt)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==28){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuRehberOgretmenOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==29){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuSinifOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==30){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuOkulOncesiOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==31){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuBedenOABT)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==32){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuKamuYon)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==33){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuUluslararasIliskiler)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==34){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuCeko)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==35){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuHukuk)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==36){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuIktisat)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==37){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuMaliye)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==38){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuIsletme)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==39){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuMuhasebe)
                    binding.spinnerKonu.adapter=adaptor
                }else if (position==40){
                    val adaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,konuIstatistik)
                    binding.spinnerKonu.adapter=adaptor
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        binding.spinnerKonu.onItemSelectedListener= object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
               if (dersPosition!=null){
                   if (dersPosition==1){
                       konuPosition=konuMatematikGKGY[position]
                   }else if (dersPosition==2){
                       konuPosition=konuGeometriGKGY[position]
                   }else if (dersPosition==3){
                       konuPosition=konuTurkceGKGY[position]
                   } else if (dersPosition==4){
                       konuPosition=konuTarihGKGY[position]
                   }else if (dersPosition==5){
                       konuPosition=konuCografyaGKGY[position]
                   }else if (dersPosition==6){
                       konuPosition=konuVatandaslikGKGY[position]
                   }else if (dersPosition==7){
                       konuPosition=konuOgretimIlkeYontem[position]
                   }else if (dersPosition==8){
                       konuPosition=konuSinifYonetimi[position]
                   }else if (dersPosition==9){
                       konuPosition=konuOgretimveMateryal[position]
                   }else if (dersPosition==10){
                       konuPosition=konuProgramGelistirme[position]
                   }else if (dersPosition==11){
                       konuPosition=konuOlcme[position]
                   }else if(dersPosition==12){
                       konuPosition=konuOgrenmePsik[position]
                   }else if (dersPosition==13){
                       konuPosition=konuGelisimPsik[position]
                   }else if (dersPosition==14){
                       konuPosition=konuRehberlik[position]
                   }else if (dersPosition==15){
                       konuPosition=konuTurkceOABT[position]
                   }else if (dersPosition==16){
                       konuPosition=konuMatematikIlkogretimOABT[position]
                   }else if (dersPosition==17){
                       konuPosition=konuFenOABT[position]
                   }else if (dersPosition==18){
                       konuPosition=konuSosyalBilimlerOABT[position]
                   }else if (dersPosition==19){
                       konuPosition=konuEdebiyatOABT[position]
                   }else if (dersPosition==20){
                       konuPosition=konuTarihOABT[position]
                   }else if (dersPosition==21){
                       konuPosition=konuCografyaOABT[position]
                   }else if (dersPosition==22){
                       konuPosition=konuMatematikLiseOABT[position]
                   }else if (dersPosition==23){
                       konuPosition=konuFizikOABT[position]
                   }else if (dersPosition==24){
                       konuPosition=konuKimyaOABT[position]
                   }else if (dersPosition==25){
                       konuPosition=konuBiyolojiOABT[position]
                   }else if (dersPosition==26){
                       konuPosition=konuDinOABT[position]
                   }else if (dersPosition==27){
                       konuPosition=konuIngilizceOabt[position]
                   }else if (dersPosition==28){
                       konuPosition=konuRehberOgretmenOABT[position]
                   }else if (dersPosition==29){
                       konuPosition=konuSinifOABT[position]
                   }else if (dersPosition==30){
                       konuPosition=konuOkulOncesiOABT[position]
                   }else if (dersPosition==31){
                       konuPosition=konuBedenOABT[position]
                   }else if (dersPosition==32){
                       konuPosition=konuKamuYon[position]
                   }else if (dersPosition==33){
                       konuPosition=konuUluslararasIliskiler[position]
                   }else if (dersPosition==34){
                       konuPosition=konuCeko[position]
                   }else if (dersPosition==35){
                       konuPosition=konuHukuk[position]
                   }else if (dersPosition==36){
                       konuPosition=konuIktisat[position]
                   }else if (dersPosition==37){
                       konuPosition=konuMaliye[position]
                   }else if (dersPosition==38){
                       konuPosition=konuIsletme[position]
                   }else if (dersPosition==39){
                       konuPosition=konuMuhasebe[position]
                   }else if (dersPosition==40){
                       konuPosition=konuIstatistik[position]
                   }

               }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }



    }

    fun yukle(view: View){
        if(binding.spinnerDers.selectedItemPosition==0){
            Toast.makeText(this,"Ders seçmeniz gerekli",Toast.LENGTH_LONG).show()
        }else if(binding.spinnerKonu.selectedItemPosition==0){
            Toast.makeText(this,"Konu seçmeniz gerekli",Toast.LENGTH_LONG).show()
        }else if (selectedImage==null && downloadUrlUpload==null){
            Toast.makeText(this,"Soru görselinizi seçmeniz gerekli",Toast.LENGTH_LONG).show()
        }else if (binding.editTextAciklama.text.toString().isEmpty()){
            Toast.makeText(this,"Soru açıklaması yazmanız gerekli",Toast.LENGTH_LONG).show()
        }else{
            if (uuidUpload!=null){
                if (uuidUpload!="null"){
                    uuid= uuidUpload
                }else{
                    uuid=UUID.randomUUID().toString()
                }
            }else{
                uuid=UUID.randomUUID().toString()
            }

            var imageName="${uuid}.jpg"
            val selectedAciklama=binding.editTextAciklama.text.toString().trim()
            val selectedDers=ders[binding.spinnerDers.selectedItemPosition]
            val selectedKonu=konuPosition
            val userUid=auth.currentUser!!.uid
            val userEmail=auth.currentUser!!.email
            val userDisplayName=auth.currentUser!!.displayName
            val userPhotoUrl=auth.currentUser!!.photoUrl.toString()
            val reference=storage.reference.child("images").child(imageName)
            val calendar=Calendar.getInstance()
            val day=calendar.get(Calendar.DAY_OF_MONTH)
            val month=calendar.get(Calendar.MONTH)+1
            val year=calendar.get(Calendar.YEAR)
            val time="${day}.${month}.${year}"
            val docRef=uuid.toString()
            val pId=OneSignal.getDeviceState()?.userId


            if (selectedImage!=null){
                binding.buttonYukle.isEnabled=false
                binding.imageViewUpload.isClickable=false
                binding.progressBarUpdate.visibility=View.VISIBLE
                reference.putFile(selectedImage!!).addOnSuccessListener {

                    val uploadImageReference=storage.reference.child("images").child(imageName)
                    uploadImageReference.downloadUrl.addOnSuccessListener {
                        val downloadUrl=it.toString()
                        val postMap= hashMapOf<String,Any>()
                        postMap.put("downloadUrl",downloadUrl)
                        postMap.put("selectedAciklama",selectedAciklama)
                        postMap.put("selectedDers",selectedDers)
                        postMap.put("selectedKonu",selectedKonu)
                        postMap.put("userUid",userUid)
                        postMap.put("email",userEmail!!)
                        postMap.put("userDisplayName",userDisplayName!!)
                        postMap.put("userPhotoUrl",userPhotoUrl)
                        postMap.put("date",com.google.firebase.Timestamp.now())
                        postMap.put("time",time)
                        postMap.put("docRef",docRef)
                        postMap.put("dogruCevap",false)
                        postMap.put("dogruCevapString","null")
                        postMap.put("dogruCevapImage","null")
                        if (pId != null) {
                            postMap.put("pId",pId)
                        }


                        firestore.collection("Odev").document(docRef).set(postMap).addOnSuccessListener {
                            Singleton.uuidUpload ="null"
                            position=0
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                            binding.buttonYukle.isEnabled=true
                            binding.imageViewUpload.isClickable=true
                            binding.progressBarUpdate.visibility=View.INVISIBLE
                        }


                    }.addOnFailureListener {
                        Toast.makeText(this,"Ödev Yüklenemedi",Toast.LENGTH_LONG).show()
                        binding.buttonYukle.isEnabled=true
                        binding.imageViewUpload.isClickable=true
                        binding.progressBarUpdate.visibility=View.INVISIBLE
                    }

                }.addOnFailureListener {
                    Toast.makeText(this,"Ödev Yüklenemedi",Toast.LENGTH_LONG).show()
                    binding.buttonYukle.isEnabled=true
                    binding.imageViewUpload.isClickable=true
                    binding.progressBarUpdate.visibility=View.INVISIBLE
                }

            }else{
                binding.buttonYukle.isEnabled=false
                binding.imageViewUpload.isClickable=false
                binding.progressBarUpdate.visibility=View.VISIBLE
                val downloadUrl= downloadUrlUpload
                val postMap= hashMapOf<String,Any>()
                postMap.put("downloadUrl",downloadUrl!!)
                postMap.put("selectedAciklama",selectedAciklama)
                postMap.put("selectedDers",selectedDers)
                postMap.put("selectedKonu",selectedKonu)
                postMap.put("userUid",userUid)
                postMap.put("email",userEmail!!)
                postMap.put("userDisplayName",userDisplayName!!)
                postMap.put("userPhotoUrl",userPhotoUrl)
                postMap.put("date",com.google.firebase.Timestamp.now())
                postMap.put("time",time)
                postMap.put("docRef",docRef)
                if (dogruCevapUpload!=null){
                    postMap.put("dogruCevap", dogruCevapUpload!!)
                }

                if (pId != null) {
                    postMap.put("pId",pId)
                }

                if (dogruCevapStringUpload!=null){
                        postMap.put("dogruCevapString", dogruCevapStringUpload!!)
                }

                if (dogruCevapImageUpload!=null){
                        postMap.put("dogruCevapImage", dogruCevapImageUpload!!)
                }

                firestore.collection("Odev").document(docRef).set(postMap).addOnSuccessListener {
                    Singleton.uuidUpload ="null"
                    position=0
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this,"Ödev Yüklenemedi",Toast.LENGTH_LONG).show()
                    binding.buttonYukle.isEnabled=true
                    binding.imageViewUpload.isClickable=true
                    binding.progressBarUpdate.visibility=View.INVISIBLE
                }
            }
        }

    }

    fun cameraSelected(view: View){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
                Snackbar.make(view,"Görsel yüklenmesi için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver",View.OnClickListener {
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

    fun gallerySelected(view: View){
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
            val intentGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickGalleryLauncher.launch(intentGallery)

        }

    }



    fun registerLauncher(){
        cropActivityGalleryResultLauncher=registerForActivityResult(cropActivityGalleryContract){uri->
            uri.let {
                binding.imageViewUpload.setImageURI(it)
                selectedImage=it
            }
        }

        pickPhotoLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode== RESULT_OK){
                //setPic()
                    val file=File(currentPhotoPath)
                    val uri=Uri.fromFile(file)
                    cropActivityGalleryResultLauncher.launch(uri)
            }
        }

        pickGalleryLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
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

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = binding.imageViewUpload.width
        val targetH: Int = binding.imageViewUpload.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->


        }
    }



    override fun onDestroy() {
        super.onDestroy()
        downloadUrlUpload=null
        selectedAciklamaUpload=null
        uuidUpload="null"
    }

    private fun konular() {
        konuMatematikGKGY.add("Konu seçin")
        konuMatematikGKGY.add("Sayılar")
        konuMatematikGKGY.add("Faktöriyel")
        konuMatematikGKGY.add("Sayı Basamakları")
        konuMatematikGKGY.add("Bölme ve Bölünebilme")
        konuMatematikGKGY.add("Asal Çarpanlara Ayırma")
        konuMatematikGKGY.add("EBOB-EKOK")
        konuMatematikGKGY.add("Rasyonel Sayılar")
        konuMatematikGKGY.add("Üslü Sayılar")
        konuMatematikGKGY.add("Köklü Sayılar")
        konuMatematikGKGY.add("Basit Eşitsizlikler")
        konuMatematikGKGY.add("Mutlak Değer")
        konuMatematikGKGY.add("Çarpanlara Ayırma")
        konuMatematikGKGY.add("1. Dereceden Denklemler")
        konuMatematikGKGY.add("Oran – Orantı")
        konuMatematikGKGY.add("Sayı Problemleri")
        konuMatematikGKGY.add("Kesir Problemleri")
        konuMatematikGKGY.add("Yaş Problemleri")
        konuMatematikGKGY.add("İşçi-Havuz Problemleri")
        konuMatematikGKGY.add("Hareket Problemleri")
        konuMatematikGKGY.add("Yüzde Kar-Zarar Problemleri")
        konuMatematikGKGY.add("Karışım Problemleri")
        konuMatematikGKGY.add("Grafik Problemleri")
        konuMatematikGKGY.add("Kümeler")
        konuMatematikGKGY.add("İşlem")
        konuMatematikGKGY.add("Modüler Aritmetik")
        konuMatematikGKGY.add("Permütasyon – Kombinasyon – Olasılık")
        konuMatematikGKGY.add("Fonksiyonlar")
        konuMatematikGKGY.add("Sayısal Mantık")

        konuGeometriGKGY.add("Konu seçin")
        konuGeometriGKGY.add("Geometrik Kavramlar")
        konuGeometriGKGY.add("Doğruda Açılar")
        konuGeometriGKGY.add("Üçgende Açılar")
        konuGeometriGKGY.add("Dik Üçgen")
        konuGeometriGKGY.add("İkizkenar Üçgen")
        konuGeometriGKGY.add("Eşkenar Üçgen")
        konuGeometriGKGY.add("Açıortay")
        konuGeometriGKGY.add("Kenarortay")
        konuGeometriGKGY.add("Üçgende Alan")
        konuGeometriGKGY.add("Üçgende Benzerlik")
        konuGeometriGKGY.add("Açı Kenar Bağıntıları")
        konuGeometriGKGY.add("Çokgenler")
        konuGeometriGKGY.add("Dörtgenler")
        konuGeometriGKGY.add("Çember ve Daire")
        konuGeometriGKGY.add("Analitik Geometri")
        konuGeometriGKGY.add("Katı Cisimler")

        konuTurkceGKGY.add("Konu seçin")
        konuTurkceGKGY.add("Sözcükte Anlam")
        konuTurkceGKGY.add("Cümlede Anlam")
        konuTurkceGKGY.add("Paragrafta Anlam")
        konuTurkceGKGY.add("Sözcükte Yapı")
        konuTurkceGKGY.add("Sözcük Türleri")
        konuTurkceGKGY.add("Sözcük Grupları")
        konuTurkceGKGY.add("Cümlenin Ögeleri")
        konuTurkceGKGY.add("Cümle Türleri")
        konuTurkceGKGY.add("Ses Bilgisi")
        konuTurkceGKGY.add("Yazım Kuralları")
        konuTurkceGKGY.add("Noktalama İşaretleri")
        konuTurkceGKGY.add("Anlatım Bozuklukları")
        konuTurkceGKGY.add("Sözel Mantık")

        konuTarihGKGY.add("Konu seçin")
        konuTarihGKGY.add("İslamiyet Öncesi Türk Tarihi")
        konuTarihGKGY.add("İlk Türk İslam Devletleri")
        konuTarihGKGY.add("Anadolu Selçuklu Dönemi ve Beylikler Dönemi")
        konuTarihGKGY.add("Osmanlı Devleti Kültür ve Medeniyeti")
        konuTarihGKGY.add("Osmanlı Devleti Kuruluş Dönemi")
        konuTarihGKGY.add("Osmanlı Devleti Yükselme Dönemi")
        konuTarihGKGY.add("17. Yüzyılda Osmanlı Devleti Duraklama Dönemi")
        konuTarihGKGY.add("18. Yüzyılda Osmanlı Devleti Gerileme Dönemi ve Islahatları")
        konuTarihGKGY.add("19. Yüzyılda Osmanlı Devleti Dağılma Dönemi ve Islahatları")
        konuTarihGKGY.add("20. Yüzyılda Osmanlı Devleti")
        konuTarihGKGY.add("Kurtuluş Savaşı Hazırlık Dönemi")
        konuTarihGKGY.add("I. TBMM Dönemi ve Ayaklanmalar")
        konuTarihGKGY.add("Kurtuluş Savaşı Muharebeler ve Antlaşmalar Dönemi")
        konuTarihGKGY.add("Cumhuriyet Dönemi")
        konuTarihGKGY.add("Atatürk Dönemi Türk İç ve Dış Politikası")
        konuTarihGKGY.add("Atatürk İlkeleri")
        konuTarihGKGY.add("Çağdaş Türk ve Dünya Tarihi")

        konuCografyaGKGY.add("Konu seçin")
        konuCografyaGKGY.add("Türkiye’nin Coğrafi Konumu")
        konuCografyaGKGY.add("Türkiye’nin İklimi ve Bitki Örtüsü")
        konuCografyaGKGY.add("Türkiye’nin Fiziki Özellikleri")
        konuCografyaGKGY.add("Türkiye’de Nüfus ve Yerleşme")
        konuCografyaGKGY.add("Tarım")
        konuCografyaGKGY.add("Hayvancılık")
        konuCografyaGKGY.add("Madenler ve Enerji Kaynakları")
        konuCografyaGKGY.add("Sanayi ve Endüstri")
        konuCografyaGKGY.add("Ulaşım")
        konuCografyaGKGY.add("Ticaret")
        konuCografyaGKGY.add("Turizm")
        konuCografyaGKGY.add("Bölgeler Coğrafyası")

        konuVatandaslikGKGY.add("Konu seçin")
        konuVatandaslikGKGY.add("Temel Hukuk")
        konuVatandaslikGKGY.add("Anayasa ve Devlet Yapıları")
        konuVatandaslikGKGY.add("Hükümet Sistemleri ve Demokrasi")
        konuVatandaslikGKGY.add("Türk Anayasa Tarihi")
        konuVatandaslikGKGY.add("1982 Anayasası’nın Temel İlkeleri")
        konuVatandaslikGKGY.add("Temel Hak ve Hürriyetler")
        konuVatandaslikGKGY.add("Yasama")
        konuVatandaslikGKGY.add("Yürütme")
        konuVatandaslikGKGY.add("Yargı")
        konuVatandaslikGKGY.add("İdare Hukuku")

        konuOgretimIlkeYontem.add("Konu seçin")
        konuOgretimIlkeYontem.add("Eğitimle İlgili Temel Kavramlar")
        konuOgretimIlkeYontem.add("Öğretme-Öğrenme Yaklaşımları")
        konuOgretimIlkeYontem.add("Öğretim Yöntemleri")
        konuOgretimIlkeYontem.add("Öğretim Teknikleri")
        konuOgretimIlkeYontem.add("Kavram Öğretimi")
        konuOgretimIlkeYontem.add("Öğrenme Stratejileri")
        konuOgretimIlkeYontem.add("Öğrenme Stilleri ve Üst Düzey Zihinsel Beceriler")

        konuSinifYonetimi.add("Konu seçin")
        konuSinifYonetimi.add("Sınıf Yönetimini Etkileyen Faktörler")
        konuSinifYonetimi.add("Sınıf Yönetiminin Boyutları")
        konuSinifYonetimi.add("Öğretmen Özellikleri")
        konuSinifYonetimi.add("Sınıf Kuralları")
        konuSinifYonetimi.add("Güdüleme Kuramları")

        konuOgretimveMateryal.add("Konu seçin")
        konuOgretimveMateryal.add("Temel Kavramlar")
        konuOgretimveMateryal.add("Öğretim Araç Gereçleri")
        konuOgretimveMateryal.add("Öğretim Materyali")
        konuOgretimveMateryal.add("Materyal Tasarlama")
        konuOgretimveMateryal.add("Görsel Tasarım")

        konuProgramGelistirme.add("Konu seçin")
        konuProgramGelistirme.add("Eğitimle İlgili Temel Kavramlar")
        konuProgramGelistirme.add("Türk Eğitim Sistemi")
        konuProgramGelistirme.add("Eğitimde Program Kavramı ve Program Türleri")
        konuProgramGelistirme.add("Program Geliştirme ve Program Geliştirmenin Kuramsal Temelleri")
        konuProgramGelistirme.add("Program Geliştirme Yaklaşım,Model ve Tasarımları")
        konuProgramGelistirme.add("Program Tasarısının Hazırlanması")
        konuProgramGelistirme.add("Programın Denenmesi,Değerlendirilmesi ve Süreklilik Kazandırılması")
        konuProgramGelistirme.add("Program Geliştirmede Yeni Anlayışları ve Yönelimler")
        konuProgramGelistirme.add("Eğitim Öğretimde Planlama")

        konuOlcme.add("Konu seçin")
        konuOlcme.add("Temel Kavramlar")
        konuOlcme.add("Bir Ölçme Aracında Bulunması Gereken Nitelikler")
        konuOlcme.add("Test Türleri")
        konuOlcme.add("Çağdaş Değişim Yaklaşımları ve Ölçme Araçları")
        konuOlcme.add("İstatiksel İşlemler")

        konuOgrenmePsik.add("Konu seçin")
        konuOgrenmePsik.add("Öğrenme Psikolojisine Giriş")
        konuOgrenmePsik.add("Klasik Koşullanma")
        konuOgrenmePsik.add("Bağlaşımcılık Kuramı")
        konuOgrenmePsik.add("Bitişimlik Kuramı")
        konuOgrenmePsik.add("Edimsel Koşullanma Kuramı")
        konuOgrenmePsik.add("Gestalt Kuramı")
        konuOgrenmePsik.add("Bilgi İşleme Kuramı")
        konuOgrenmePsik.add("İşaret-Gestalt Kuramı")
        konuOgrenmePsik.add("Sosyal Öğrenme Kuramı")

        konuGelisimPsik.add("Konu seçin")
        konuGelisimPsik.add("Gelişim Psikolojisi")
        konuGelisimPsik.add("Biyolojik Gelişim")
        konuGelisimPsik.add("Bilişşel Gelişim")
        konuGelisimPsik.add("Ahlak Gelişimi")
        konuGelisimPsik.add("Kişilik Gelişimi")

        konuRehberlik.add("Konu seçin")
        konuRehberlik.add("Çağdaş Eğitim Anlayışı,Öğrenci Kişilik Hizmetleri ve Rehberlik Hizmetleri")
        konuRehberlik.add("Özel Eğitim Hizmetleri")
        konuRehberlik.add("Rehberlik Hizmet Alanları")
        konuRehberlik.add("Rehberlik Türleri ve Mesleki Danışma Kuramları")
        konuRehberlik.add("Bireyi Tanıma Teknikleri")
        konuRehberlik.add("Psikolojik Danışma Kuramları")
        konuRehberlik.add("Rebherlikte Öğrenci Öğretmen İlişkileri")
        konuRehberlik.add("Rehberlik Örgütlenme Sistemi")

        konuTurkceOABT.add("Konu seçin")
        konuTurkceOABT.add("Anlama ve Anlatma Teknikleri")
        konuTurkceOABT.add("Dil Bilgisi ve Dil Bilimi")
        konuTurkceOABT.add("Çocuk Edebiyatı")
        konuTurkceOABT.add("Türk Halk Edebiyatı")
        konuTurkceOABT.add("Yeni Türk Edebiyatı")
        konuTurkceOABT.add("Eski Türk Edebiyatı")
        konuTurkceOABT.add("Edebiyat Bilgi ve Kuramları")

        konuMatematikIlkogretimOABT.add("Konu seçin")
        konuMatematikIlkogretimOABT.add("Analiz")
        konuMatematikIlkogretimOABT.add("Cebir")
        konuMatematikIlkogretimOABT.add("Geometri")
        konuMatematikIlkogretimOABT.add("Uygulamalı Matematik")

        konuFenOABT.add("Konu seçin")
        konuFenOABT.add("Fizik")
        konuFenOABT.add("Kimya")
        konuFenOABT.add("Biyoloji")
        konuFenOABT.add("Yer Bilimi")
        konuFenOABT.add("Astronomi")
        konuFenOABT.add("Çevre Bilimi")

        konuSosyalBilimlerOABT.add("Konu seçin")
        konuSosyalBilimlerOABT.add("Tarih")
        konuSosyalBilimlerOABT.add("Coğrafya")
        konuSosyalBilimlerOABT.add("Siyaset Bilimi")
        konuSosyalBilimlerOABT.add("Diğer Sosyal Bilim Alanları")
        konuSosyalBilimlerOABT.add("Sosyal Bilgilerin Temelleri")

        konuEdebiyatOABT.add("Konu seçin")
        konuEdebiyatOABT.add("Eski Türk Dili ve Yeni Türk Dili")
        konuEdebiyatOABT.add("Yeni Türk Edebiyatı")
        konuEdebiyatOABT.add("Eski Türk Edebiyatı")
        konuEdebiyatOABT.add("Türk Halk edebiyatı")

        konuTarihOABT.add("Konu seçin")
        konuTarihOABT.add("Tarih Metodu")
        konuTarihOABT.add("Eski Çağ Tarihi")
        konuTarihOABT.add("İslamiyet Öncesi Türk Tarihi")
        konuTarihOABT.add("Orta Çağ İslam Tarihi")
        konuTarihOABT.add("Osmanlı Tarihi")
        konuTarihOABT.add("Türkiye Cumhuriyeti Tarihi")
        konuTarihOABT.add("Ortaçağ'dan 20.yy'a Dünya Tarihi")
        konuTarihOABT.add("20.yy Türk ve Dünya Tarihi")

        konuCografyaOABT.add("Konu seçin")
        konuCografyaOABT.add("Fiziki Coğrafya")
        konuCografyaOABT.add("Beşeri ve Ekonomik Coğrafya")
        konuCografyaOABT.add("Bölgeler ve Ülkeler")

        konuMatematikLiseOABT.add("Konu seçin")
        konuMatematikLiseOABT.add("Analiz")
        konuMatematikLiseOABT.add("Cebir")
        konuMatematikLiseOABT.add("Geometri")
        konuMatematikLiseOABT.add("Uygulamalı Matematik")

        konuFizikOABT.add("Konu seçin")
        konuFizikOABT.add("Mekanik")
        konuFizikOABT.add("Elektrik ve Manyetizma")
        konuFizikOABT.add("Madde ve Özellikleri")
        konuFizikOABT.add("Dalgalar ve Optik")
        konuFizikOABT.add("Modern Fizik")

        konuKimyaOABT.add("Konu seçin")
        konuKimyaOABT.add("Analitik Kimya")
        konuKimyaOABT.add("Anorganik Kimya")
        konuKimyaOABT.add("Organik Kimya")
        konuKimyaOABT.add("Fizikokimya")

        konuBiyolojiOABT.add("Konu seçin")
        konuBiyolojiOABT.add("Hücre ve Metabolizma")
        konuBiyolojiOABT.add("Bitki Biyolojisi")
        konuBiyolojiOABT.add("İnsan ve Hayvan Biyolojisi")
        konuBiyolojiOABT.add("Ekoloji")
        konuBiyolojiOABT.add("Canlıların Sınıflandırılması")
        konuBiyolojiOABT.add("Genetik ve Evrim")

        konuDinOABT.add("Konu seçin")
        konuDinOABT.add("Hz. Muhammed'in Hayatı ve Sünneti")
        konuDinOABT.add("Kur'an-ı Kerimin Muhtevasını Anlama")
        konuDinOABT.add("Temel Dini Bilgiler")
        konuDinOABT.add("İslam Ahlakı,Estetiği ve Felsefesi")
        konuDinOABT.add("Günümüz Türkiye'sinde İslam Mezhep ve Yorumları")
        konuDinOABT.add("Din Bilimleri")

        konuIngilizceOabt.add("Konu seçin")
        konuIngilizceOabt.add("Dil Yeterliliği")
        konuIngilizceOabt.add("Dil Bilim")
        konuIngilizceOabt.add("Edebiyat")

        konuRehberOgretmenOABT.add("Konu seçin")
        konuRehberOgretmenOABT.add("Temel Psikolojik Kavramlar")
        konuRehberOgretmenOABT.add("Psikolojik Danışma Kuram İlke ve Teknikleri")
        konuRehberOgretmenOABT.add("Davranış ve Uyum Problemleri")
        konuRehberOgretmenOABT.add("Bireyi Tanıma Teknikleri")
        konuRehberOgretmenOABT.add("Bireyle ve Grupla Psikolojik Danışma")
        konuRehberOgretmenOABT.add("Mesleki Rehberlik ve Kariyer Danışmanlığı")
        konuRehberOgretmenOABT.add("Psikolojik Danışma ve Rehberlikte Araştırma ve Program Geliştirme")
        konuRehberOgretmenOABT.add("Meslek Etiği ve Yasal Konular")

        konuSinifOABT.add("Konu seçin")
        konuSinifOABT.add("Temel Matematik")
        konuSinifOABT.add("Genel Biyoloji")
        konuSinifOABT.add("Genel Fizik")
        konuSinifOABT.add("Genel Kimya")
        konuSinifOABT.add("Türk Dili")
        konuSinifOABT.add("Cumhuriyet Dönemi Türk Edebiyatı")
        konuSinifOABT.add("Çocuk Edebiyatı")
        konuSinifOABT.add("Uygarlık Tarihi")
        konuSinifOABT.add("Türk Tarihi ve Kültürü")
        konuSinifOABT.add("Genel Coğrafya")
        konuSinifOABT.add("Türkiye Coğrafyası ve Jeopolitiği")

        konuOkulOncesiOABT.add("Konu seçin")
        konuOkulOncesiOABT.add("Okul Öncesi Eğitime Giriş")
        konuOkulOncesiOABT.add("İnsan Anatomisi ve Fizyolojisi")
        konuOkulOncesiOABT.add("Psikoloji")
        konuOkulOncesiOABT.add("Anne-Çocuk Sağlığı ve İlkyardım")
        konuOkulOncesiOABT.add("Anne-Çocuk Beslenmesi")
        konuOkulOncesiOABT.add("Erken Çocukluk Döneminde Gelişim")
        konuOkulOncesiOABT.add("Yaratıcılık ve Geliltirilmesi")
        konuOkulOncesiOABT.add("Çocukta Oyun Gelişimi")
        konuOkulOncesiOABT.add("Çocuk Edebiyatı")
        konuOkulOncesiOABT.add("Çocuk Ruh Sağlığı")
        konuOkulOncesiOABT.add("Drama")
        konuOkulOncesiOABT.add("Özel Öğretim Yöntemleri")
        konuOkulOncesiOABT.add("Anne-Baba Eğitimi")
        konuOkulOncesiOABT.add("İlköğretime Hazırlık ve İlköğretim Programları")

        konuBedenOABT.add("Konu seçin")
        konuBedenOABT.add("Beden Eğitimi ve Sporun Temelleri")
        konuBedenOABT.add("İnsan Anatomisi ve Kinesiyoloji")
        konuBedenOABT.add("Sağlık Bilgisi ve İlk Yardım")
        konuBedenOABT.add("Egzersiz Fizyolojisi")
        konuBedenOABT.add("Antrenman Bilgisi")
        konuBedenOABT.add("Fiziksel Uygunluk")
        konuBedenOABT.add("Egzersiz ve Beslenme")
        konuBedenOABT.add("Engelliler İçin Beden Eğitimi ve Spor")
        konuBedenOABT.add("Psikomotor Gelişim")
        konuBedenOABT.add("Beden Eğitimi ve Spor Yönetimi")
        konuBedenOABT.add("Beceri Öğrenimi")
        konuBedenOABT.add("Atletizm")
        konuBedenOABT.add("Takım Sporları")
        konuBedenOABT.add("Ritim Eğitimi ve Dans, Halk Oyunları")
        konuBedenOABT.add("Jimnastik")
        konuBedenOABT.add("Eğitsel Oyunlar")

        konuKamuYon.add("Konu seçin")
        konuKamuYon.add("Yönetim Bilimi Ve Kamu Yönetimi")
        konuKamuYon.add("Personel Yönetimi")
        konuKamuYon.add("Siyaset Bilimi")
        konuKamuYon.add("Kentleşme Ve Çevre")
        konuKamuYon.add("Türk Siyasal Hayatı")
        konuKamuYon.add("Sosyoloji")
        konuKamuYon.add("Siyasal Sistemler Ve Yapılan Reformlar")

        konuUluslararasIliskiler.add("Konu seçin")
        konuUluslararasIliskiler.add("Uluslararası Hukuk")
        konuUluslararasIliskiler.add("Uluslararası İlişkiler Teorisi")
        konuUluslararasIliskiler.add("Siyasi Tarih")
        konuUluslararasIliskiler.add("Türk Dış Politikası")
        konuUluslararasIliskiler.add("Uluslararası Örgütler")
        konuUluslararasIliskiler.add("Uluslararası Güncel Sorunlar")

        konuCeko.add("Konu seçin")
        konuCeko.add("İş ve Sosyal Güvenlik Hukuku")
        konuCeko.add("Sosyal Güvenlik Hukuku ve Teorisi")
        konuCeko.add("Çalışma Psikolojisi ve Sosyolojisi")

        konuHukuk.add("Konu seçin")
        konuHukuk.add("Anayasa Hukuku")
        konuHukuk.add("İdare Hukuku ve İdari Yargı")
        konuHukuk.add("Ceza Hukuku")
        konuHukuk.add("Medeni Hukuku")
        konuHukuk.add("Borçlar Hukuku")
        konuHukuk.add("Ticaret Hukuku")
        konuHukuk.add("İcra ve İflas Hukuku")

        konuIktisat.add("Konu seçin")
        konuIktisat.add("İktisadi Doktrinler Tarihi")
        konuIktisat.add("Mikro İktisat")
        konuIktisat.add("Makro İktisat")
        konuIktisat.add("Para-Banka-Kredi")
        konuIktisat.add("Uluslararası İktisat")
        konuIktisat.add("Büyüme ve Kalkınma")
        konuIktisat.add("Türkiye Ekonomisi")

        konuMaliye.add("Konu seçin")
        konuMaliye.add("Maliye Teorisi")
        konuMaliye.add("Kamu Gelirleri")
        konuMaliye.add("Kamu Giderleri")
        konuMaliye.add("Kamu Borçları")
        konuMaliye.add("Bütçe")
        konuMaliye.add("Vergi Hukuk")
        konuMaliye.add("Maliye Politikası")

        konuIsletme.add("Konu seçin")
        konuIsletme.add("İşletmenin Temel Kavramları")
        konuIsletme.add("Üretim Yönetimi")
        konuIsletme.add("İşletme Yönetimi")
        konuIsletme.add("Pazarlama Yönetimi")
        konuIsletme.add("Finansal Yönetim")

        konuMuhasebe.add("Konu seçin")
        konuMuhasebe.add("Genel Muhasebe")
        konuMuhasebe.add("Mali Tablo Analizi")
        konuMuhasebe.add("İhtisas Muhasebesi")

        konuIstatistik.add("Konu seçin")
        konuIstatistik.add("Olasılık ve Stokastik Süreçler")
        konuIstatistik.add("Matematiksel istatistik")
        konuIstatistik.add("Yöneylem Araştırması")
        konuIstatistik.add("Çok Değişkenli Analizler")
        konuIstatistik.add("Parametrik Olmayan Testler")
        konuIstatistik.add("Uygulamalı İstatistik")
        konuIstatistik.add("Zaman Serileri")
        konuIstatistik.add("Deney Tasarımı ve Varyans Analizi")
        konuIstatistik.add("Örnekleme")
        konuIstatistik.add("Regresyon Analizi")

        konuBos.add("Ders Seçmeniz Gerekiyor")
    }

    private fun dersler() {
        ders.add("Dersi Seçin")
        ders.add("Matematik(GKGY)")
        ders.add("Geometri(GKGY)")
        ders.add("Türkçe(GKGY)")
        ders.add("Tarih(GKGY)")
        ders.add("Coğrafya(GKGY)")
        ders.add("Vatandaşlık(GKGY)")
        ders.add("Öğretim İlke ve Yöntemleri(EB)")
        ders.add("Sınıf Yönetimi(EB)")
        ders.add("Öğretim Tek. ve Materyal Tas.(EB)")
        ders.add("Program Geliştirme(EB)")
        ders.add("Ölçme ve Değerlendirme(EB)")
        ders.add("Öğrenme Psikolojisi(EB)")
        ders.add("Gelişim Psikolojisi(EB)")
        ders.add("Rehberlik ve Özel Eğitim(EB)")
        ders.add("Türkçe(ÖABT)")
        ders.add("İlköğretim Matematik(ÖABT)")
        ders.add("Fen Bilimleri(ÖABT)")
        ders.add("Sosyal Bilgiler(ÖABT)")
        ders.add("Türk Dili ve Edebiyatı(ÖABT)")
        ders.add("Tarih(ÖABT)")
        ders.add("Coğrafya(ÖABT)")
        ders.add("Matematik Lise(ÖABT)")
        ders.add("Fizik(ÖABT)")
        ders.add("Kimya(ÖABT)")
        ders.add("Biyoloji(ÖABT)")
        ders.add("Din Kültürü ve Ahlak Bilgisi(ÖABT)")
        ders.add("Yabancı Dil İngilizce(ÖABT)")
        ders.add("Rehber Öğretmen(ÖABT)")
        ders.add("Sınıf Öğretmenliği(ÖABT)")
        ders.add("Okul Öncesi(ÖABT)")
        ders.add("Beden Eğitimi(ÖABT)")
        ders.add("Kamu Yönetimi(A Grubu)")
        ders.add("Uluslararası İlişkiler(A Grubu)")
        ders.add("ÇEKO(A Grubu)")
        ders.add("Hukuk(A Grubu)")
        ders.add("İktisat(A Grubu)")
        ders.add("Maliye(A Grubu)")
        ders.add("İşletme(A Grubu)")
        ders.add("Muhasebe(A Grubu)")
        ders.add("İstatistik(A Grubu)")


        dersAdaptor= ArrayAdapter(this@UploadActivityNew,android.R.layout.simple_list_item_1,android.R.id.text1,ders)
        binding.spinnerDers.adapter=dersAdaptor

    }

}