package com.caneryildirim.odevinipaylas.Adaptors

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.caneryildirim.odevinipaylas.Activity.ProfileWatchActivity
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dogruCevapFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.userDisplayNameFragment
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.userUidFragment
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.CevapDetailRowBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerCevapAdaptor(val delete: Delete, val cevapArrayList: ArrayList<Cevap>, val userUid:String):RecyclerView.Adapter<RecyclerCevapAdaptor.CevapHolder>() {

    interface Delete{
        fun onItemDelete(position: Int)
        fun sikayetItem(position: Int)
        fun duzenleItem(position: Int)
        fun guncelleItem(position: Int)
        fun onItemClick(position: Int)
    }

    class CevapHolder(val binding: CevapDetailRowBinding):RecyclerView.ViewHolder(binding.root) {

    }

    fun getTimeDate(date:Long):String{
        val netDate=Date(date)
        val sdf= SimpleDateFormat("dd/MM/yy HH:mm:ss",Locale.getDefault())
        return sdf.format(netDate)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CevapHolder {
        val binding=CevapDetailRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CevapHolder(binding)
    }

    override fun onBindViewHolder(holder: CevapHolder, position: Int) {
        if (cevapArrayList[position].cevapYorum=="null"){
            holder.binding.cevapYorumText.visibility=View.GONE
            holder.binding.cevapYorumText.isEnabled=false
            holder.binding.cevapYorumText.isClickable=false
            holder.binding.yorumYukle.visibility=View.GONE
            holder.binding.yorumYukle.isEnabled=false
            holder.binding.yorumYukle.isClickable=false
        }else{
            holder.binding.cevapYorumText.visibility=View.VISIBLE
            holder.binding.cevapYorumText.isEnabled=false
            holder.binding.cevapYorumText.isClickable=false
            holder.binding.cevapYorumText.setText("${userDisplayNameFragment}:${cevapArrayList[position].cevapYorum}")
            holder.binding.yorumYukle.visibility=View.GONE
            holder.binding.yorumYukle.isEnabled=false
            holder.binding.yorumYukle.isClickable=false
        }



        if (userUid== userUidFragment &&cevapArrayList[position].dogruCevap==false && cevapArrayList[position].userUid!=userUid&& dogruCevapFragment==false){
            if (cevapArrayList[position].yanlisCevap==false || cevapArrayList[position].yanlisCevap==null){
                holder.binding.dogruCevapmiSorText.visibility=View.VISIBLE
                holder.binding.evetDogruText.visibility=View.VISIBLE
                holder.binding.hayirYanlisText.visibility=View.VISIBLE
            }else{
                holder.binding.dogruCevapmiSorText.visibility=View.GONE
                holder.binding.evetDogruText.visibility=View.GONE
                holder.binding.hayirYanlisText.visibility=View.GONE
            }
        }else{
            holder.binding.dogruCevapmiSorText.visibility=View.GONE
            holder.binding.evetDogruText.visibility=View.GONE
            holder.binding.hayirYanlisText.visibility=View.GONE
        }

        holder.binding.evetDogruText.setOnClickListener {
            holder.binding.dogruCevap.visibility=View.VISIBLE
            holder.binding.dogruCevapText.visibility=View.VISIBLE
            holder.binding.dogruCevapmiSorText.visibility=View.GONE
            holder.binding.evetDogruText.visibility=View.GONE
            holder.binding.hayirYanlisText.visibility=View.GONE
            delete.guncelleItem(position)
        }
        holder.binding.hayirYanlisText.setOnClickListener {
            holder.binding.dogruCevapmiSorText.visibility=View.GONE
            holder.binding.evetDogruText.visibility=View.GONE
            holder.binding.hayirYanlisText.visibility=View.GONE
            val db=FirebaseFirestore.getInstance()
            val referenceCevap=db.collection("Cevap").document(cevapArrayList[position].soruUid).collection("Cevaplar").document(cevapArrayList[position].docUuid)
            referenceCevap.update("yanlisCevap",true).addOnSuccessListener {}
        }



        if (cevapArrayList[position].dogruCevap){
            holder.binding.dogruCevap.visibility=View.VISIBLE
            holder.binding.dogruCevapText.visibility=View.VISIBLE
        }else{
            holder.binding.dogruCevap.visibility=View.GONE
            holder.binding.dogruCevapText.visibility=View.GONE
        }

        val dateFirst=cevapArrayList[position].date.toDate().time
        val now=Timestamp.now().toDate().time
        val nowTime=getTimeDate(now)
        val firstTime=getTimeDate(dateFirst)
        val format=SimpleDateFormat("dd/MM/yy HH:mm:ss")
        val firstDate=format.parse(firstTime)
        val nowDate= format.parse(nowTime)
        val diff=nowDate.time-firstDate.time

        val farkDakika=diff/(60*1000) %60
        val farkDakikaInt=farkDakika.toInt()
        val farkSaat=diff/(60*60*1000) %60
        val farkSaatInt=farkSaat.toInt()
        val farkGun=diff/(24*60*60*1000) %24
        val farkGunInt=farkGun.toInt()
        val farkHafta=diff/(7*24*60*60*1000) %7
        val farkHaftaInt=farkHafta.toInt()
        val farkAy=diff/(30*24*60*60) %0.003
        val farkAyInt=farkAy.toInt()
        val farkYil=diff/(12*30*24*60*60) %0.012
        val farkYilInt=farkYil.toInt()

        if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt==0 && farkGunInt==0 && farkSaatInt==0 &&farkDakikaInt==0){
            holder.binding.tarihCevapText.text="Şimdi"
        }else if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt==0 && farkGunInt==0 && farkSaatInt==0 && farkDakikaInt!=0){
            holder.binding.tarihCevapText.text="${farkDakika} Dk Önce"
        }else if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt==0 && farkGunInt==0 && farkSaatInt!=0){
            holder.binding.tarihCevapText.text="${farkSaat} Saat Önce"
        }else if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt==0 &&farkGunInt!=0){
            holder.binding.tarihCevapText.text="${farkGun} Gün Önce"
        }else if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt!=0){
            holder.binding.tarihCevapText.text="${farkHafta} Hafta Önce"
        }else if (farkYilInt==0 && farkAyInt!=0){
            holder.binding.tarihCevapText.text="${farkAy} Ay Önce"
        }else if (farkYilInt!=0){
            holder.binding.tarihCevapText.text="${farkYil} Yıl Önce"
        }

        holder.binding.textAciklamaDetailCevap.text=cevapArrayList[position].selectedAciklama
        holder.binding.textUserNameDetailCevap.text=cevapArrayList[position].userEmail
        holder.itemView.setOnClickListener {
            val intent=Intent(holder.itemView.context,ProfileWatchActivity::class.java)
            intent.putExtra("userUidFragment",cevapArrayList[position].userUid)
            intent.putExtra("userNameFragment",cevapArrayList[position].userEmail)
            //intent.putExtra("userPhotoFragment",cevapArrayList[position].userPhotoUrl)
            holder.itemView.context.startActivity(intent)
        }

        if (cevapArrayList[position].downloadUrl=="null"){
            holder.binding.imageCevapDetailCevap.visibility=View.GONE
            holder.binding.imageCevapDetailCevap.isClickable=false
        }else{
            holder.binding.imageCevapDetailCevap.visibility=View.VISIBLE
            holder.binding.imageCevapDetailCevap.isClickable=true
            holder.binding.imageCevapDetailCevap.setImageResource(R.drawable.cevapmedia)
        }

        holder.binding.menuRecyclerCevap.setOnClickListener {
            if (userUid==cevapArrayList[position].userUid || userUid=="P2dukbTHNMcdyP3FjDOsd7fR6PT2"){
                val popup= PopupMenu(holder.itemView.context,it)
                popup.menuInflater.inflate(R.menu.menu_cevap_delete,popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener {
                    if (it.itemId==R.id.menu_cevap_delete){
                        delete.onItemDelete(position)
                        true
                    }else{
                        false
                    }
                }
            }else if(userUid==userUidFragment){
                val popup=PopupMenu(holder.itemView.context,it)
                popup.menuInflater.inflate(R.menu.menu_sorusahibi,popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener {
                    if (it.itemId==R.id.menu_blocksahip){
                        delete.sikayetItem(position)
                        true
                    }else if(it.itemId==R.id.menu_cevapyorum){
                        holder.binding.cevapYorumText.visibility=View.VISIBLE
                        holder.binding.cevapYorumText.isEnabled=true
                        holder.binding.cevapYorumText.isClickable=true
                        holder.binding.yorumYukle.visibility=View.VISIBLE
                        holder.binding.yorumYukle.isEnabled=true
                        holder.binding.yorumYukle.isClickable=true
                        holder.binding.yorumYukle.setOnClickListener {
                            holder.binding.yorumYukle.isClickable=false
                            val db=FirebaseFirestore.getInstance()
                            val reference=db.collection("Cevap").document(cevapArrayList[position].soruUid).collection("Cevaplar").document(cevapArrayList[position].docUuid)
                            reference.update("cevapYorum",holder.binding.cevapYorumText.text.toString()).addOnSuccessListener {

                                OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
                                OneSignal.initWithContext(holder.itemView.context)
                                OneSignal.setAppId(Singleton.ONESIGNAL_APP_ID)
                                val auth=FirebaseAuth.getInstance()


                                val calendar=Calendar.getInstance()
                                val day=calendar.get(Calendar.DAY_OF_MONTH)
                                val month=calendar.get(Calendar.MONTH)+1
                                val year=calendar.get(Calendar.YEAR)
                                val time="${day}.${month}.${year}"

                                val soruUid= Singleton.docRefFragment!!
                                val userEmail=auth.currentUser!!.displayName.toString()
                                val userUid=auth.currentUser!!.uid
                                val date= Timestamp.now()
                                val pIdCevap=OneSignal.getDeviceState()?.userId
                                val imageUuid=userUid

                                val uuidBildirim= UUID.randomUUID().toString()
                                val bildirimMap= hashMapOf<String,Any>()
                                bildirimMap.put("soruUid",soruUid)
                                bildirimMap.put("userName","${userEmail} cevabını yanıtladı")
                                bildirimMap.put("userUid",userUid)
                                bildirimMap.put("docUid",imageUuid)
                                bildirimMap.put("date",date)
                                bildirimMap.put("docBildirim",uuidBildirim)
                                bildirimMap.put("time",time)
                                bildirimMap.put("okundu",false)
                                db.collection("Users").document(cevapArrayList[position].userUid).collection("Bildirimler").document(uuidBildirim).set(bildirimMap).addOnSuccessListener {
                                    if (soruUid!=userUid){
                                        try {
                                            OneSignal.postNotification("{'contents': {'en':'Cevabına yorum geldi'}, 'include_player_ids': ['" + cevapArrayList[position].pIdCevap + "']}",null)
                                        }catch (e: JSONException){
                                            e.printStackTrace()
                                        }
                                    }


                                }.addOnFailureListener {

                                }


                                Toast.makeText(holder.itemView.context,"Yorumun Yüklendi",Toast.LENGTH_SHORT).show()
                                holder.binding.cevapYorumText.visibility=View.VISIBLE
                                holder.binding.cevapYorumText.isEnabled=false
                                holder.binding.cevapYorumText.isClickable=false
                                holder.binding.yorumYukle.visibility=View.INVISIBLE
                                holder.binding.yorumYukle.isEnabled=false
                                holder.binding.yorumYukle.isClickable=false
                                holder.binding.cevapYorumText.setText("")
                            }.addOnFailureListener {
                                Toast.makeText(holder.itemView.context,"Başarısız",Toast.LENGTH_SHORT).show()
                                holder.binding.yorumYukle.isClickable=true
                            }
                        }

                        true
                    }else if (it.itemId==R.id.menu_dogrucevapisaret){
                        holder.binding.dogruCevap.visibility=View.VISIBLE
                        holder.binding.dogruCevapText.visibility=View.VISIBLE
                        holder.binding.dogruCevapmiSorText.visibility=View.GONE
                        holder.binding.evetDogruText.visibility=View.GONE
                        holder.binding.hayirYanlisText.visibility=View.GONE
                        delete.guncelleItem(position)
                        true
                    }else{
                        false
                }
                }
            }else{
                val popup=PopupMenu(holder.itemView.context,it)
                popup.menuInflater.inflate(R.menu.menu_block,popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener {
                    if (it.itemId==R.id.menu_block){
                        delete.sikayetItem(position)
                        true
                    }else{
                        false
                    }

                }
        }
        }
        holder.binding.imageCevapDetailCevap.setOnClickListener {
            delete.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return cevapArrayList.size
    }
}