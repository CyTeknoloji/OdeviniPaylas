package com.caneryildirim.odevinipaylas.Adaptors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.RecyclerNotificationRowBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerNotificationAdaptor(val notificationList:ArrayList<Notification>,val click:Click):RecyclerView.Adapter<RecyclerNotificationAdaptor.NotificationHolder>() {

    interface Click{
        fun delete(position: Int)
    }

    class NotificationHolder(val binding:RecyclerNotificationRowBinding):RecyclerView.ViewHolder(binding.root) {

    }

    fun getTimeDate(date:Long):String{
        val netDate= Date(date)
        val sdf= SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault())
        return sdf.format(netDate)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val binding=RecyclerNotificationRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NotificationHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        val dolu=holder.itemView.context.getColor(R.color.purple_700)
        val bos=holder.itemView.context.getColor(R.color.spinnercolor)

        if (notificationList[position].okundu==false || notificationList[position].okundu==null){
            holder.binding.cardNotification.setCardBackgroundColor(dolu)
        }else{
            holder.binding.cardNotification.setCardBackgroundColor(bos)
        }

        val dateFirst=notificationList[position].date.toDate().time
        val now= Timestamp.now().toDate().time
        val nowTime=getTimeDate(now)
        val firstTime=getTimeDate(dateFirst)
        val format=SimpleDateFormat("dd/MM/yy HH:mm:ss")
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
            holder.binding.textTimeNot.text="Şimdi"
        }else if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()==0 && farkGun.toInt()==0 && farkSaat.toInt()==0 && farkDakika.toInt()<61){
            holder.binding.textTimeNot.text="${farkDakika} Dk Önce"
        }else if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()==0 && farkGun.toInt()==0 && farkSaat.toInt()<25){
            holder.binding.textTimeNot.text="${farkSaat} Saat Önce"
        }else if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()==0 &&farkGun.toInt()<8){
            holder.binding.textTimeNot.text="${farkGun} Gün Önce"
        }else if (farkYil.toInt()==0 && farkAy.toInt()==0 && farkHafta.toInt()<5){
            holder.binding.textTimeNot.text="${farkHafta} Hafta Önce"
        }else if (farkYil.toInt()==0 && farkAy.toInt()<13){
            holder.binding.textTimeNot.text="${farkAy} Ay Önce"
        }else if (farkYil.toInt()!=0){
            holder.binding.textTimeNot.text="${farkYil} Yıl Önce"
        }

        holder.binding.textViewNotificationRow.text="${notificationList[position].senderUsername}"
        //holder.binding.textTimeNot.text="${notificationList[position].time}"
        holder.itemView.setOnClickListener {
            click.delete(position)
        }


        

    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}