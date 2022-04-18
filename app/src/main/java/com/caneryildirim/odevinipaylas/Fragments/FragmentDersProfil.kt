package com.caneryildirim.odevinipaylas.Fragments

import android.Manifest
import android.app.AlertDialog

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.caneryildirim.odevinipaylas.Activity.MainActivity

import com.caneryildirim.odevinipaylas.Activity.NotificationActivity
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.registiration
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.registirationFeed
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.FragmentDersProfilBinding

import com.google.android.material.snackbar.Snackbar

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FragmentDersProfil : Fragment() {
    private var _binding: FragmentDersProfilBinding? = null
    private val binding get() = _binding!!

    private var selectedImage:Uri?=null
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database:FirebaseFirestore

    private lateinit var permiisonLauncher:ActivityResultLauncher<String>
    private lateinit var pickPhotoLauncher:ActivityResultLauncher<Intent>
    private lateinit var pickGalleryLauncher:ActivityResultLauncher<Intent>
    private lateinit var cropActivityGalleryResultLauncher: ActivityResultLauncher<Uri?>

    lateinit var currentPhotoPath: String
    private var requestFrom: String?=null

    private var photoUrl:Uri?=null

    private val cropActivityGalleryContract=object :ActivityResultContract<Uri?,Uri?>(){
        override fun createIntent(context: Context, input: Uri?): Intent {
            return CropImage.activity(input)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1)
                .setCropMenuCropButtonTitle("Kırp").setRequestedSize(600,600)
                .getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDersProfilBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance()
        database= FirebaseFirestore.getInstance()
        registerLauncher()


        getData()

        if (photoUrl==null){
            binding.userProfileImageview.setImageResource(R.drawable.profileperson)
        }

        binding.editTextUserName.setOnClickListener {
            binding.editTextUserName.isCursorVisible=true
        }

        /*
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this.requireContext())
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent=Intent(this.requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }

         */

        binding.emailProfileText.text=auth.currentUser!!.email

        binding.cameraSelectProfile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this.requireContext(),Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this.requireActivity(),Manifest.permission.CAMERA)){
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

        binding.gallerySelectProfile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this.requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this.requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
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


        binding.buttonProfile.setOnClickListener {
            binding.buttonProfile.isEnabled=false
            val reference=storage.reference.child("Users").child(auth.currentUser!!.uid)
            if (selectedImage!=null){
                reference.putFile(selectedImage!!).addOnSuccessListener {
                    val uploadProfilePicture=storage.reference.child("Users").child(auth.currentUser!!.uid)
                    uploadProfilePicture.downloadUrl.addOnSuccessListener {
                        val downloadUrl=it.toString()

                        val user = Firebase.auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = binding.editTextUserName.text.toString().trim()
                            photoUri = Uri.parse(downloadUrl)
                        }
                        user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this.requireContext(),"Profil güncellendi",Toast.LENGTH_SHORT).show()
                                    binding.buttonProfile.isEnabled=true

                                    database.collection("Odev").whereEqualTo("userUid",auth.currentUser!!.uid).get().addOnSuccessListener {
                                        if (it!=null){
                                            if (!it.isEmpty){
                                                val documents=it.documents
                                                for (document in documents){
                                                    val docRef=document.get("docRef") as String
                                                    val referenceNew=database.collection("Odev").document(docRef)
                                                    referenceNew.update("userPhotoUrl",downloadUrl).addOnSuccessListener {}
                                                    referenceNew.update("userDisplayName",binding.editTextUserName.text.toString().trim()).addOnSuccessListener {}

                                                }
                                            }
                                        }
                                    }
                                }
                            }



                    }.addOnFailureListener {
                        Toast.makeText(this.requireContext(),"Resim yüklenirken hata oluştu",Toast.LENGTH_SHORT).show()
                        binding.buttonProfile.isEnabled=true
                    }

                }.addOnFailureListener {
                    Toast.makeText(this.requireContext(),"Resim yüklenirken hata oluştu",Toast.LENGTH_SHORT).show()
                    binding.buttonProfile.isEnabled=true
                }
            }else{
                val user = Firebase.auth.currentUser
                val profileUpdates = userProfileChangeRequest {
                    displayName = binding.editTextUserName.text.toString().trim()
                }
                user!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this.requireContext(),"Profil güncellendi",Toast.LENGTH_SHORT).show()
                            binding.buttonProfile.isEnabled=true
                            database.collection("Odev").whereEqualTo("userUid",auth.currentUser!!.uid).get().addOnSuccessListener {
                                if (it!=null){
                                    if (!it.isEmpty){
                                        val documents=it.documents
                                        for (document in documents){ val docRef=document.get("docRef") as String
                                            val referenceNew=database.collection("Odev").document(docRef)
                                            referenceNew.update("userDisplayName",binding.editTextUserName.text.toString().trim()).addOnSuccessListener {}

                                        }
                                    }
                                }
                            }

                        }
                    }
            }

        }

        binding.buttonPassReset.setOnClickListener {
            val email=auth.currentUser!!.email.toString()
            auth.sendPasswordResetEmail(email).addOnSuccessListener {
                Toast.makeText(this.requireContext(),"Eposta adresinize şifre sıfırlama maili gönderildi.",Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this.requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

        binding.deleteAuth.setOnClickListener {
            val alert= AlertDialog.Builder(this.requireContext())
            alert.setTitle("Hesabı Sil")
            alert.setMessage("Emin misin?")
            alert.setNegativeButton("Hayır"){ dialogInterface: DialogInterface, i: Int ->
                Toast.makeText(this.requireContext(),"Vazgeçildi",Toast.LENGTH_SHORT).show()
            }
            alert.setPositiveButton("Evet"){ dialogInterface: DialogInterface, i: Int ->
                auth.currentUser!!.delete().addOnSuccessListener {
                    Toast.makeText(this.requireContext(),"Hesabın Silindi",Toast.LENGTH_SHORT).show()
                    val intentFinish=Intent(this.requireContext(),MainActivity::class.java)
                    startActivity(intentFinish)
                    registiration?.remove()
                    registirationFeed?.remove()

                }.addOnFailureListener {
                    Toast.makeText(this.requireContext(),"Hesabı silmek için çıkış yapıp tekrar deneyin!",Toast.LENGTH_SHORT).show()
                }
            }
            alert.show()
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun registerLauncher(){
        cropActivityGalleryResultLauncher=registerForActivityResult(cropActivityGalleryContract){uri->
            uri.let {
                binding.userProfileImageview.setImageURI(it)
                selectedImage=it
            }
        }

        pickPhotoLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode== AppCompatActivity.RESULT_OK){
                //setPic()
                val file= File(currentPhotoPath)
                val uri=Uri.fromFile(file)
                cropActivityGalleryResultLauncher.launch(uri)
            }
        }

        pickGalleryLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode== AppCompatActivity.RESULT_OK){
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
                Toast.makeText(this.requireContext(),"İzin verilmedi",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getData(){
        val user = Firebase.auth.currentUser
        user?.let {

            val name = user.displayName
            photoUrl = user.photoUrl
            Picasso.get().load(photoUrl).into(binding.userProfileImageview)
            binding.editTextUserName.setText(name)


        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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
            takePictureIntent.resolveActivity(this.requireActivity().packageManager)?.also {
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
                        this.requireContext(),
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
        val targetW: Int = binding.userProfileImageview.width
        val targetH: Int = binding.userProfileImageview.height

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