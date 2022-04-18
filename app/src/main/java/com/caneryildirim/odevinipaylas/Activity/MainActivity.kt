package com.caneryildirim.odevinipaylas.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast

import com.caneryildirim.odevinipaylas.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var userEmail:String
    private lateinit var userPassword:String
    private lateinit var db:FirebaseFirestore

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onBackPressed() {
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        binding.girisButton.isEnabled=true
        binding.progressBarMain.visibility=View.INVISIBLE
        auth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()


        val currentUser=auth.currentUser
        if (currentUser!=null){
            if (currentUser.isEmailVerified){
                val intent=Intent(this, DersActivity::class.java)
                startActivity(intent)
                finish()
            }


        }

    }

    fun giris(view:View){
        binding.progressBarMain.visibility=View.VISIBLE
        binding.girisButton.isEnabled=false
        userEmail=binding.loginEmailText.text.toString().trim()
        userPassword=binding.loginPasswordText.text.toString().trim()

        if (userEmail.isEmpty()){
            binding.loginEmailText.setError("Eposta adresinizi giriniz!")
            binding.loginEmailText.requestFocus()
            binding.girisButton.isEnabled=true
            binding.progressBarMain.visibility=View.INVISIBLE
        }else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            binding.loginEmailText.setError("Düzgün formatta Epostanızı giriniz!")
            binding.loginEmailText.requestFocus()
            binding.girisButton.isEnabled=true
            binding.progressBarMain.visibility=View.INVISIBLE
        }else if (userPassword.isEmpty()){
            binding.loginPasswordText.setError("Şifrenizi giriniz!")
            binding.loginPasswordText.requestFocus()
            binding.girisButton.isEnabled=true
            binding.progressBarMain.visibility=View.INVISIBLE
        }else if (userPassword.length<6){
            binding.loginPasswordText.setError("Şifreniz 6 karakterden az olamaz!")
            binding.loginPasswordText.requestFocus()
            binding.girisButton.isEnabled=true
            binding.progressBarMain.visibility=View.INVISIBLE
        }else{
            binding.girisButton.isEnabled=false
            binding.progressBarMain.visibility=View.VISIBLE
            auth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener {

            if (it.isSuccessful){
                val user=auth.currentUser
                if (user!!.isEmailVerified){
                    val intentDers=Intent(this, DersActivity::class.java)
                    startActivity(intentDers)
                    finish()
                    binding.girisButton.isEnabled=true
                    binding.progressBarMain.visibility=View.INVISIBLE
                }else{
                    Toast.makeText(this,"Aktivasyon kodunuzu onaylayınız",Toast.LENGTH_SHORT).show()
                    binding.girisButton.isEnabled=true
                    binding.progressBarMain.visibility=View.INVISIBLE
                }

            }


            }.addOnFailureListener {
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                binding.girisButton.isEnabled=true
                binding.progressBarMain.visibility=View.INVISIBLE
            }
        }


    }

    fun kayit(view:View){
        val intentKayit=Intent(this, KayitActivity::class.java)

        startActivity(intentKayit)
        //intentKayit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        finish()

    }

    fun lossPass(view: View){
        userEmail=binding.loginEmailText.text.toString().trim()
        if(userEmail.isEmpty()){
            binding.loginEmailText.setError("Eposta adresinizi giriniz!")
            binding.loginEmailText.requestFocus()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            binding.loginEmailText.setError("Geçersiz Eposta formatı!")
            binding.loginEmailText.requestFocus()
        }else{
            auth.sendPasswordResetEmail(userEmail).addOnSuccessListener {
                Toast.makeText(this,"Eposta adresinize şifre sıfırlama maili gönderildi.",Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }
}