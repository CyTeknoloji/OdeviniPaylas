package com.caneryildirim.odevinipaylas.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caneryildirim.odevinipaylas.Activity.NotificationActivity
import com.caneryildirim.odevinipaylas.Activity.UploadActivityNew
import com.caneryildirim.odevinipaylas.Adaptors.RecyclerDersAdaptor
import com.caneryildirim.odevinipaylas.Adaptors.Ders
import com.caneryildirim.odevinipaylas.Adaptors.Singleton
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.adaptorDers

import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.FragmentDersMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.onesignal.OneSignal


class FragmentDersMain : Fragment() {
    private var _binding: FragmentDersMainBinding? = null
    private val binding get() = _binding!!
    private var dersList = ArrayList<Ders>()
    private lateinit var auth:FirebaseAuth
    private lateinit var db:FirebaseFirestore



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDersMainBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()

        val dersTum = Ders(R.drawable.imagederstum, "Tüm Dersler")
        val dersMatematikGKGY = Ders(R.drawable.imageders2, "Matematik(GKGY)")
        val dersGeometriGKDY= Ders(R.drawable.geometriicon,"Geometri(GKGY)")
        val dersTurkceGKGY = Ders(R.drawable.imagedersedebiyat, "Türkçe(GKGY)")
        val dersTarihGKGY = Ders(R.drawable.imagederstarih, "Tarih(GKGY)")
        val dersCografyaGKGY = Ders(R.drawable.imagederscografya, "Coğrafya(GKGY)")
        val dersVatandaslikGKGY =Ders(R.drawable.imagedersvatandaslik,"Vatandaşlık(GKGY)")
        val dersOgretimIlkeYontem=Ders(R.drawable.imageogretimilkeyontem,"Öğretim İlke ve Yöntemleri(EB)")
        val dersSinifYonetimi=Ders(R.drawable.imagesinifyonetimi,"Sınıf Yönetimi(EB)")
        val dersOgretimveMateryal=Ders(R.drawable.imageogretimmateryal,"Öğretim Tek. ve Materyal Tas.(EB)")
        val programGelistirme=Ders(R.drawable.imageprogramgel,"Program Geliştirme(EB)")
        val olcme=Ders(R.drawable.imageolcme,"Ölçme ve Değerlendirme(EB)")
        val ogrenmePsik=Ders(R.drawable.imageogrenmepsik,"Öğrenme Psikolojisi(EB)")
        val gelisimPsik=Ders(R.drawable.imagegelisimpsik,"Gelişim Psikolojisi(EB)")
        val rehberlik=Ders(R.drawable.imagerehberlik,"Rehberlik ve Özel Eğitim(EB)")
        val turkceOABT=Ders(R.drawable.imagedersedebiyat,"Türkçe(ÖABT)")
        val matematikIlkogretimOABT=Ders(R.drawable.imageders2,"İlköğretim Matematik(ÖABT)")
        val fenOABT=Ders(R.drawable.imagedersfizik,"Fen Bilimleri(ÖABT)")
        val sosyalBilimlerOABT=Ders(R.drawable.imageogrenmepsik,"Sosyal Bilgiler(ÖABT)")
        val edebiyatOABT=Ders(R.drawable.turkceicon,"Türk Dili ve Edebiyatı(ÖABT)")
        val tarihOABT=Ders(R.drawable.imagederstarih,"Tarih(ÖABT)")
        val cografyaOABT=Ders(R.drawable.imagederscografya,"Coğrafya(ÖABT)")
        val matematikLiseOABT=Ders(R.drawable.imageders2,"Matematik Lise(ÖABT)")
        val fizikOABT=Ders(R.drawable.imagedersfizik,"Fizik(ÖABT)")
        val kimyaOABT=Ders(R.drawable.imagederskimya,"Kimya(ÖABT)")
        val biyolojiOABT=Ders(R.drawable.imagedersbiyoloji,"Biyoloji(ÖABT)")
        val dinOABT=Ders(R.drawable.imagedersdin,"Din Kültürü ve Ahlak Bilgisi(ÖABT)")
        val ingilizceOabt=Ders(R.drawable.imagedersingilizce,"Yabancı Dil İngilizce(ÖABT)")
        val rehberOgretmenOABT=Ders(R.drawable.imagerehberlik,"Rehber Öğretmen(ÖABT)")
        val sinifOABT=Ders(R.drawable.imageogrenmepsik,"Sınıf Öğretmenliği(ÖABT)")
        val okulOncesiOABT=Ders(R.drawable.imagegelisimpsik,"Okul Öncesi(ÖABT)")
        val bedenOABT=Ders(R.drawable.imagebeden,"Beden Eğitimi(ÖABT)")
        val kamuYonetimi=Ders(R.drawable.imageogretimilkeyontem,"Kamu Yönetimi(A Grubu)")
        val uluslararasIliskiler=Ders(R.drawable.imageuluslararasi,"Uluslararası İlişkiler(A Grubu)")
        val ceko=Ders(R.drawable.imageceko,"ÇEKO(A Grubu)")
        val hukuk=Ders(R.drawable.imagedersfizik,"Hukuk(A Grubu)")
        val iktisat=Ders(R.drawable.turkceicon,"İktisat(A Grubu)")
        val maliye=Ders(R.drawable.imagemaliye,"Maliye(A Grubu)")
        val isletme=Ders(R.drawable.imageisletme,"İşletme(A Grubu)")
        val muhasebe=Ders(R.drawable.imagemuhasebe,"Muhasebe(A Grubu)")
        val istatistik=Ders(R.drawable.imageistatistik,"İstatistik(A Grubu)")


        dersList.clear()
        dersList.add(dersTum)
        dersList.add(dersMatematikGKGY)
        dersList.add(dersGeometriGKDY)
        dersList.add(dersTurkceGKGY)
        dersList.add(dersTarihGKGY)
        dersList.add(dersCografyaGKGY)
        dersList.add(dersVatandaslikGKGY)
        dersList.add(dersOgretimIlkeYontem)
        dersList.add(dersSinifYonetimi)
        dersList.add(dersOgretimveMateryal)
        dersList.add(programGelistirme)
        dersList.add(olcme)
        dersList.add(ogrenmePsik)
        dersList.add(gelisimPsik)
        dersList.add(rehberlik)
        dersList.add(turkceOABT)
        dersList.add(matematikIlkogretimOABT)
        dersList.add(fenOABT)
        dersList.add(sosyalBilimlerOABT)
        dersList.add(edebiyatOABT)
        dersList.add(tarihOABT)
        dersList.add(cografyaOABT)
        dersList.add(matematikLiseOABT)
        dersList.add(fizikOABT)
        dersList.add(kimyaOABT)
        dersList.add(biyolojiOABT)
        dersList.add(dinOABT)
        dersList.add(ingilizceOabt)
        dersList.add(rehberOgretmenOABT)
        dersList.add(sinifOABT)
        dersList.add(okulOncesiOABT)
        dersList.add(bedenOABT)
        dersList.add(kamuYonetimi)
        dersList.add(uluslararasIliskiler)
        dersList.add(ceko)
        dersList.add(hukuk)
        dersList.add(iktisat)
        dersList.add(maliye)
        dersList.add(isletme)
        dersList.add(muhasebe)
        dersList.add(istatistik)

        Singleton.uuidUpload ="null"



        binding.recyclerViewDers.layoutManager = LinearLayoutManager(this.context)
        adaptorDers= RecyclerDersAdaptor(dersList)
        binding.recyclerViewDers.adapter=adaptorDers

        /*
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this.requireContext())
        OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler {
            val intent=Intent(this.requireContext(),NotificationActivity::class.java)
            startActivity(intent)
        }

         */


        if (auth.currentUser!!.uid=="P2dukbTHNMcdyP3FjDOsd7fR6PT2"){
            val pId=OneSignal.getDeviceState()?.userId.toString()
            val pIdMap= hashMapOf<String,Any>()
            pIdMap.put("pId",pId)
            db.collection("Admin").document(auth.currentUser!!.uid).set(pIdMap).addOnSuccessListener {

            }
        }



        binding.recyclerViewDers.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy>0){
                    binding.fabDers.hide()
                }else{
                    binding.fabDers.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        binding.fabDers.setOnClickListener {
            val intentUpload = Intent(this.context, UploadActivityNew::class.java)
            startActivity(intentUpload)
        }


    }



    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}