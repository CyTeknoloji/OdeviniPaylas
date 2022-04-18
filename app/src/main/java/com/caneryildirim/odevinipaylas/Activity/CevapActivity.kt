package com.caneryildirim.odevinipaylas.Activity

import android.Manifest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.aciklamaCevap
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.cevapYorumFragment

import com.caneryildirim.odevinipaylas.Adaptors.Singleton.docRefFragment

import com.caneryildirim.odevinipaylas.Adaptors.Singleton.docUuidFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapDuzenle
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.downloadUrlCevap
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.pIdFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.takipciList

import com.caneryildirim.odevinipaylas.Adaptors.Singleton.userUidFragment
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.ActivityCevapBinding

import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.storage.FirebaseStorage

import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import org.json.JSONException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

import java.util.*
import kotlin.collections.ArrayList


class CevapActivity : AppCompatActivity() {

    private lateinit var binding:ActivityCevapBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var selectedImage:Uri?=null
    private lateinit var imageUuid:String

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
                .getIntent(this@CevapActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCevapBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        binding.progressBarCevap.visibility=View.INVISIBLE
        auth= FirebaseAuth.getInstance()
        firestore= FirebaseFirestore.getInstance()
        storage= FirebaseStorage.getInstance()



        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent=Intent(this,NotificationActivity::class.java)
            startActivity(intent)
        }

        registerLauncher()

        binding.toolbarCevap.title="Cevabını Yükle"
        binding.toolbarCevap.setTitleTextColor(getColor(R.color.white))
        setSupportActionBar(binding.toolbarCevap)
        binding.toolbarCevap.setNavigationOnClickListener {
            onBackPressed()
        }

        if (downloadUrlCevap!=null){
            if (downloadUrlCevap=="null"){
                binding.cevapImage.setImageResource(R.drawable.gorselsec)
            }else{
                Picasso.get().load(downloadUrlCevap).into(binding.cevapImage)
            }

        }
        if (aciklamaCevap!=null){
            binding.cevapText.setText(aciklamaCevap)
        }




    }

    fun cameraSelectCevap(view: View){
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
    fun gallerySelectCevap(view: View){
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



    fun yukle(view: View){
        if (binding.cevapText.text.toString().trim().isEmpty()&&selectedImage==null&& downloadUrlCevap==null){
            Toast.makeText(this,"Görsel ya da yorumdan oluşan cevabınız olmalı",Toast.LENGTH_LONG).show()
            binding.progressBarCevap.visibility=View.INVISIBLE
        }else{
            if(docUuidFragment!=null){
                if (docUuidFragment!="null"){
                    imageUuid= docUuidFragment as String
                }else{
                    imageUuid=UUID.randomUUID().toString()
                }
            }else{
                imageUuid=UUID.randomUUID().toString()
            }

            val calendar=Calendar.getInstance()
            val day=calendar.get(Calendar.DAY_OF_MONTH)
            val month=calendar.get(Calendar.MONTH)+1
            val year=calendar.get(Calendar.YEAR)
            val time="${day}.${month}.${year}"

            val soruUid= docRefFragment!!
            val imageName="${imageUuid}.jpg"
            val selectedAciklama=binding.cevapText.text.toString().trim()
            val date=Timestamp.now()
            val userEmail=auth.currentUser!!.displayName.toString()     //email yazıyor ama userName aldın
            val userPhotoUrl=auth.currentUser!!.photoUrl.toString()
            val userUid=auth.currentUser!!.uid
            val pIdCevap=OneSignal.getDeviceState()?.userId
            val reference=storage.reference.child("cevaplar").child(soruUid).child(imageName)
            if (selectedImage!=null){
                binding.buttonYukleCevap.isEnabled=false
                binding.cevapImage.isClickable=false
                binding.progressBarCevap.visibility=View.VISIBLE
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
                        if (dogruCevapDuzenle!=null){
                            cevapMap.put("dogruCevap", dogruCevapDuzenle!!)
                        }else{
                            cevapMap.put("dogruCevap",false)
                        }
                        if (cevapYorumFragment!=null){
                            cevapMap.put("cevapYorum", cevapYorumFragment!!)
                        }else{
                            cevapMap.put("cevapYorum","null")
                        }

                        if (pIdCevap != null) {
                            cevapMap.put("pIdCevap",pIdCevap)
                        }

                        firestore.collection("Cevap").document(soruUid).collection("Cevaplar").document(imageUuid)
                        .set(cevapMap).addOnSuccessListener {
                                val uuidTakipBildirim=UUID.randomUUID().toString()
                                val takipBildirimMap= hashMapOf<String,Any>()
                                takipBildirimMap.put("soruUid",soruUid)
                                takipBildirimMap.put("userName","Takip ettiğin soruya cevap geldi")
                                takipBildirimMap.put("userUid",userUid)
                                takipBildirimMap.put("docUid",imageUuid)
                                takipBildirimMap.put("date",date)
                                takipBildirimMap.put("docBildirim",uuidTakipBildirim)
                                takipBildirimMap.put("time",time)

                                if (takipciList.isNotEmpty()){
                                    if (takipciList.size>0){
                                        for (takipci in takipciList){
                                            if (takipci.takipciUid!=userUidFragment && takipci.takipciUid!=userUid){
                                                firestore.collection("Users").document(takipci.takipciUid).collection("Bildirimler").document(uuidTakipBildirim).set(takipBildirimMap).addOnSuccessListener {
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

                                    firestore.collection("Users").document(userUidFragment!!).collection("Bildirimler").document(uuidBildirim).set(bildirimMap).addOnSuccessListener {
                                        if (pIdCevap!= pIdFragment){     //bildirim gitmiyorsa ilk buraya bak
                                            try {
                                                OneSignal.postNotification("{'contents': {'en':'Soruna Cevap Geldi'}, 'include_player_ids': ['" + pIdFragment + "']}",null)

                                            }catch (e: JSONException){
                                                e.printStackTrace()
                                            }
                                        }
                                        docUuidFragment="null"
                                        finish()
                                    }.addOnFailureListener {

                                    }
                                }else{
                                    finish()
                                }

                        }.addOnFailureListener {
                                Toast.makeText(this,"Cevap Yüklenemedi",Toast.LENGTH_LONG).show()
                                binding.progressBarCevap.visibility=View.INVISIBLE
                            binding.buttonYukleCevap.isEnabled=true
                                binding.cevapImage.isClickable=true
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this,"Cevap Yüklenemedi",Toast.LENGTH_LONG).show()
                        binding.progressBarCevap.visibility=View.INVISIBLE
                        binding.buttonYukleCevap.isEnabled=true
                        binding.cevapImage.isClickable=true
                    }
                }.addOnFailureListener {
                    Toast.makeText(this,"Cevap Yüklenemedi",Toast.LENGTH_LONG).show()
                    binding.progressBarCevap.visibility=View.INVISIBLE
                    binding.buttonYukleCevap.isEnabled=true
                    binding.cevapImage.isClickable=true
                }

            }else{
                binding.progressBarCevap.visibility=View.VISIBLE
                binding.buttonYukleCevap.isEnabled=false
                binding.cevapImage.isClickable=false
                val cevapMap= hashMapOf<String,Any>()
                if (downloadUrlCevap!=null){
                    if (downloadUrlCevap=="null"){
                        cevapMap.put("downloadUrl","null")
                    }else{
                        cevapMap.put("downloadUrl", downloadUrlCevap!!)
                    }
                }else{
                    cevapMap.put("downloadUrl","null")
                }


                cevapMap.put("selectedAciklama",selectedAciklama)
                cevapMap.put("date",date)
                cevapMap.put("userEmail",userEmail)
                cevapMap.put("userPhotoUrl",userPhotoUrl)
                cevapMap.put("userUid",userUid)
                cevapMap.put("soruUid",soruUid!!)
                cevapMap.put("docUuid",imageUuid)
                if (dogruCevapDuzenle!=null){
                    cevapMap.put("dogruCevap", dogruCevapDuzenle!!)
                }else{
                    cevapMap.put("dogruCevap",false)
                }
                if (cevapYorumFragment!=null){
                    cevapMap.put("cevapYorum", cevapYorumFragment!!)
                }else{
                    cevapMap.put("cevapYorum","null")
                }
                if (pIdCevap != null) {
                    cevapMap.put("pIdCevap",pIdCevap)
                }

                val referenceNew=firestore.collection("Cevap").document(soruUid).collection("Cevaplar").document(imageUuid)
                referenceNew.set(cevapMap).addOnSuccessListener {
                    val uuidTakipBildirim=UUID.randomUUID().toString()
                    val takipBildirimMap= hashMapOf<String,Any>()
                    takipBildirimMap.put("soruUid",soruUid)
                    takipBildirimMap.put("userName","Takip ettiğin soruya cevap geldi")
                    takipBildirimMap.put("userUid",userUid)
                    takipBildirimMap.put("docUid",imageUuid)
                    takipBildirimMap.put("date",date)
                    takipBildirimMap.put("docBildirim",uuidTakipBildirim)
                    takipBildirimMap.put("time",time)

                    if (takipciList.isNotEmpty()){
                        if (takipciList.size>0){
                            for (takipci in takipciList){
                                if (takipci.takipciUid!=userUidFragment && takipci.takipciUid!=userUid){
                                    firestore.collection("Users").document(takipci.takipciUid).collection("Bildirimler").document(uuidTakipBildirim).set(takipBildirimMap).addOnSuccessListener {
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

                        firestore.collection("Users").document(userUidFragment!!).collection("Bildirimler").document(uuidBildirim).set(bildirimMap).addOnSuccessListener {
                            if (pIdCevap!= pIdFragment){     //bildirim gitmiyorsa ilk buraya bak
                                try {
                                    OneSignal.postNotification("{'contents': {'en':'Soruna Cevap Geldi'}, 'include_player_ids': ['" + pIdFragment + "']}",null)

                                }catch (e: JSONException){
                                    e.printStackTrace()
                                }
                            }
                            docUuidFragment="null"
                            finish()
                        }.addOnFailureListener {

                        }
                    }else{
                        docUuidFragment="null"
                        finish()
                    }


                }.addOnFailureListener {
                    Toast.makeText(this,"Cevap Yüklenemedi",Toast.LENGTH_LONG).show()
                    binding.progressBarCevap.visibility=View.INVISIBLE
                    binding.buttonYukleCevap.isEnabled=true
                    binding.cevapImage.isClickable=true
                }
            }
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        downloadUrlCevap=null
        aciklamaCevap=null
    }

    fun registerLauncher(){
        cropActivityGalleryResultLauncher=registerForActivityResult(cropActivityGalleryContract){uri->
            uri.let {
                binding.cevapImage.setImageURI(it)
                selectedImage=it
            }
        }

        pickPhotoLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode== RESULT_OK){
                //setPic()
                val file= File(currentPhotoPath)
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
        val targetW: Int = binding.cevapImage.width
        val targetH: Int = binding.cevapImage.height

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






}