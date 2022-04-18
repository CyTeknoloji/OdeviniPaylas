package com.caneryildirim.odevinipaylas.Adaptors


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.caneryildirim.odevinipaylas.Activity.DetailActivityLast
import com.caneryildirim.odevinipaylas.Activity.ProfileWatchActivity
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.OdevFeedRowBinding
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerOdevAdaptor( val delete:Delete, val odevArrayList: ArrayList<Odev>, val userUid:String): RecyclerView.Adapter<RecyclerOdevAdaptor.OdevHolder>(),Filterable {
    var filterOdevArrayList=ArrayList<Odev>()
    init {
        filterOdevArrayList=odevArrayList
    }

    interface Delete{
        fun onItemClick(position: Int)
        fun sikayetItem(position: Int)
        fun duzenleItem(position: Int)
        fun usteCikar(position: Int)
    }

    class OdevHolder(val binding: OdevFeedRowBinding):RecyclerView.ViewHolder(binding.root) {

    }

    fun getTimeDate(date:Long):String{
        val netDate=Date(date)
        val sdf= SimpleDateFormat("dd/MM/yy HH:mm:ss",Locale.getDefault())
        return sdf.format(netDate)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OdevHolder {
        val binding=OdevFeedRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OdevHolder(binding)
    }

    override fun onBindViewHolder(holder: OdevHolder, position: Int) {
        if (filterOdevArrayList[position].dogruCevap!=null){
            if (filterOdevArrayList[position].dogruCevap!!){
                holder.binding.textViewOdevCozum.visibility=View.VISIBLE
            }else{
                holder.binding.textViewOdevCozum.visibility=View.INVISIBLE
            }
        }

        holder.binding.dersOdevText.text="Ders:${filterOdevArrayList[position].selectedDers}"
        holder.binding.konuOdevText.text="Konu:${filterOdevArrayList[position].selectedKonu}"
        holder.binding.userNameOdevText.text=filterOdevArrayList[position].userDisplayName

        val dateFirst=filterOdevArrayList[position].date.toDate().time
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
            holder.binding.tarihOdevText.text="Şimdi"
        }else if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt==0 && farkGunInt==0 && farkSaatInt==0 && farkDakikaInt!=0){
            holder.binding.tarihOdevText.text="${farkDakika} Dk Önce"
        }else if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt==0 && farkGunInt==0 && farkSaatInt!=0){
            holder.binding.tarihOdevText.text="${farkSaat} Saat Önce"
        }else if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt==0 &&farkGunInt!=0){
            holder.binding.tarihOdevText.text="${farkGun} Gün Önce"
        }else if (farkYilInt==0 && farkAyInt==0 && farkHaftaInt!=0){
            holder.binding.tarihOdevText.text="${farkHafta} Hafta Önce"
        }else if (farkYilInt==0 && farkAyInt!=0){
            holder.binding.tarihOdevText.text="${farkAy} Ay Önce"
        }else if (farkYilInt!=0){
            holder.binding.tarihOdevText.text="${farkYil} Yıl Önce"
        }

        holder.binding.odevFeedImageview.setImageResource(R.drawable.whiteimage)
        Picasso.get().load(filterOdevArrayList[position].downloadUrl).placeholder(R.drawable.whiteimage).into(holder.binding.odevFeedImageview)
        if (filterOdevArrayList[position].userPhotoUrl=="null"){
            holder.binding.userProfileOdevImageview.setImageResource(R.drawable.personfeed)
        }else{
            Picasso.get().load(filterOdevArrayList[position].userPhotoUrl).into(holder.binding.userProfileOdevImageview)
        }

        holder.itemView.setOnClickListener {
            val intent=Intent(holder.itemView.context,DetailActivityLast::class.java)
            intent.putExtra("downloadUrl",filterOdevArrayList[position].downloadUrl)
            intent.putExtra("selectedAciklama",filterOdevArrayList[position].selectedAciklama)
            intent.putExtra("selectedDers",filterOdevArrayList[position].selectedDers)
            intent.putExtra("selectedKonu",filterOdevArrayList[position].selectedKonu)
            intent.putExtra("time",filterOdevArrayList[position].time)
            intent.putExtra("userDisplayName",filterOdevArrayList[position].userDisplayName)
            intent.putExtra("userPhotoUrl",filterOdevArrayList[position].userPhotoUrl)
            intent.putExtra("userUid",filterOdevArrayList[position].userUid)
            intent.putExtra("docRef",filterOdevArrayList[position].docRef)
            intent.putExtra("position",position)
            intent.putExtra("pId",filterOdevArrayList[position].pId)
            intent.putExtra("info","feed")
            intent.putExtra("dogruCevap",filterOdevArrayList[position].dogruCevap)
            intent.putExtra("dogruCevapString",filterOdevArrayList[position].dogruCevapString)
            intent.putExtra("dogruCevapImage",filterOdevArrayList[position].dogruCevapImage)
            intent.putExtra("date",filterOdevArrayList[position].date.toDate().time)
            holder.itemView.context.startActivity(intent)
        }

            holder.binding.userNameOdevText.setOnClickListener {
                val intent=Intent(holder.itemView.context,ProfileWatchActivity::class.java)
                intent.putExtra("userUidFragment",filterOdevArrayList[position].userUid)
                intent.putExtra("userNameFragment",filterOdevArrayList[position].userDisplayName)
                intent.putExtra("userPhotoFragment",filterOdevArrayList[position].userPhotoUrl)
                intent.putExtra("position",position)
                holder.itemView.context.startActivity(intent)
            }

            holder.binding.userProfileOdevImageview.setOnClickListener {
                val intent=Intent(holder.itemView.context,ProfileWatchActivity::class.java)
                intent.putExtra("userUidFragment",filterOdevArrayList[position].userUid)
                intent.putExtra("userNameFragment",filterOdevArrayList[position].userDisplayName)
                intent.putExtra("userPhotoFragment",filterOdevArrayList[position].userPhotoUrl)
                intent.putExtra("position",position)
                holder.itemView.context.startActivity(intent)
            }



        holder.binding.menuRecycler.setOnClickListener {
            if (userUid==filterOdevArrayList[position].userUid || userUid=="P2dukbTHNMcdyP3FjDOsd7fR6PT2"){

                val popup=PopupMenu(holder.itemView.context,it)
                popup.menuInflater.inflate(R.menu.menu_delete,popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener {
                    if (it.itemId==R.id.menu_delete){
                        delete.onItemClick(position)
                        true
                    }else if (it.itemId==R.id.menu_ustecikar){
                        if (farkGunInt<2){
                            Toast.makeText(holder.itemView.context,"En az 2 gün geçmesi gerekli",Toast.LENGTH_SHORT).show()
                        }else{
                            delete.usteCikar(position)
                        }
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



    }

    override fun getItemCount(): Int {
        return filterOdevArrayList.size
    }

    override fun getFilter(): Filter {
        return object:Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()){
                    filterOdevArrayList=odevArrayList
                }else{
                    val resultList=ArrayList<Odev>()
                    for (row in odevArrayList){
                        if (row.selectedKonu.lowercase(Locale.getDefault()).contains(charSearch.lowercase(
                                Locale.getDefault()))
                            || row.selectedDers.lowercase(Locale.getDefault()).contains(charSearch.lowercase(
                                Locale.getDefault()))){
                            resultList.add(row)
                        }
                    }
                    filterOdevArrayList=resultList
                }
                val filterResults=FilterResults()
                filterResults.values=filterOdevArrayList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterOdevArrayList=results?.values as ArrayList<Odev>
                notifyDataSetChanged()
            }

        }
    }


}

