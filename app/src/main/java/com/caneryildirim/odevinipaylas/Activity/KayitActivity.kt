package com.caneryildirim.odevinipaylas.Activity

import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.caneryildirim.odevinipaylas.databinding.ActivityKayitBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore


class KayitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKayitBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userDisplayName:String
    private lateinit var userEmail:String
    private lateinit var userPassword:String
    private lateinit var userRepeatPassword:String
    private lateinit var googleSignInClient:GoogleSignInClient
    private lateinit var idGoogle:String


    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityKayitBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
        binding.kayitOlButton.isEnabled=true
        binding.progressBarKayit.visibility=View.INVISIBLE

        auth= FirebaseAuth.getInstance()
        firestore= FirebaseFirestore.getInstance()
        idGoogle ="1014690464588-msrnf5vok7o98ss59t57hecl469ihocp.apps.googleusercontent.com"

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(idGoogle)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }



    fun kayitGoogle(view:View){
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val user = auth.currentUser
                updateUI(user)
                val intent=Intent(this, DersActivity::class.java)
                ///intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()

            }.addOnFailureListener {
                updateUI(null)
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
            }
    }

    override fun onStart() {

        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    private fun updateUI(user: FirebaseUser?) {

    }

    fun kayitOl(view:View){
        binding.kayitOlButton.isEnabled=false
        binding.progressBarKayit.visibility=View.VISIBLE

        userDisplayName=binding.userNameText.text.toString().trim()
        userEmail=binding.kayitEmailText.text.toString().trim()
        userPassword=binding.kayitPasswordText.text.toString().trim()
        userRepeatPassword=binding.kayitRepeatPasswordText.text.toString().trim()

        if (userDisplayName.isEmpty()){
            binding.userNameText.setError("Kullanıcı adınızı giriniz!")
            binding.userNameText.requestFocus()
            binding.kayitOlButton.isEnabled=true
            binding.progressBarKayit.visibility=View.INVISIBLE
        }else if (userEmail.isEmpty()){
            binding.kayitEmailText.setError("Eposta adresi giriniz!")
            binding.kayitEmailText.requestFocus()
            binding.kayitOlButton.isEnabled=true
            binding.progressBarKayit.visibility=View.INVISIBLE
        }else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            binding.kayitEmailText.setError("Geçersiz Eposta formatı!")
            binding.kayitEmailText.requestFocus()
            binding.kayitOlButton.isEnabled=true
            binding.progressBarKayit.visibility=View.INVISIBLE
        }else if (userPassword.isEmpty()){
            binding.kayitPasswordText.setError("Şifrenizi giriniz!")
            binding.kayitPasswordText.requestFocus()
            binding.kayitOlButton.isEnabled=true
            binding.progressBarKayit.visibility=View.INVISIBLE
        }else if (userPassword.length<6){
            binding.kayitPasswordText.setError("Şifreniz 6 karakterden küçük olamaz")
            binding.kayitPasswordText.requestFocus()
            binding.kayitOlButton.isEnabled=true
            binding.progressBarKayit.visibility=View.INVISIBLE
        }else if (userPassword!=userRepeatPassword){
            binding.kayitRepeatPasswordText.setError("Şifreleriniz uyuşmuyor")
            binding.kayitRepeatPasswordText.requestFocus()
            binding.kayitOlButton.isEnabled=true
            binding.progressBarKayit.visibility=View.INVISIBLE
        }else{
            binding.kayitOlButton.isEnabled=false
            binding.progressBarKayit.visibility=View.VISIBLE
            auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnSuccessListener {

                val user=auth.currentUser
                val profileUpdates = userProfileChangeRequest {
                    displayName = userDisplayName
                }
                user!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateUI(user)
                            user.sendEmailVerification().addOnSuccessListener {
                             Toast.makeText(this,"Epostanıza aktivasyon kodu gönderildi.",Toast.LENGTH_SHORT).show()
                                auth.signOut()
                                val intent=Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                                binding.kayitOlButton.isEnabled=true
                                binding.progressBarKayit.visibility=View.INVISIBLE
                            }

                        }
                    }

            }.addOnFailureListener {
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                binding.kayitOlButton.isEnabled=true
                binding.progressBarKayit.visibility=View.INVISIBLE
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent=Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}