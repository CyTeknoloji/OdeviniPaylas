package com.caneryildirim.odevinipaylas.Adaptors

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.caneryildirim.odevinipaylas.Activity.FeedActivity
import com.caneryildirim.odevinipaylas.Adaptors.Singleton.dersFeed
import com.caneryildirim.odevinipaylas.R
import com.caneryildirim.odevinipaylas.databinding.DersRowBinding
import java.util.*
import kotlin.collections.ArrayList

class RecyclerDersAdaptor(val dersList: ArrayList<Ders>): RecyclerView.Adapter<RecyclerDersAdaptor.DersHolder>(),Filterable {
    var filterDersList=ArrayList<Ders>()
    init {
        filterDersList=dersList
    }

    class DersHolder(val binding: DersRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DersHolder {
        val binding= DersRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DersHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: DersHolder, position: Int) {
        holder.binding.imageViewDersRow.setImageResource(filterDersList[position].dersImage)
        holder.binding.textViewDersRow.text="${filterDersList[position].dersName}"
        holder.itemView.setOnClickListener {
            val intent=Intent(holder.itemView.context, FeedActivity::class.java)
            intent.putExtra("ders",filterDersList[position].dersName)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return filterDersList.size
    }

    override fun getFilter(): Filter {
        return object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()){
                    filterDersList=dersList
                }else{
                    val resultList=ArrayList<Ders>()
                    for (row in dersList){
                        if (row.dersName.lowercase(Locale.getDefault()).contains(charSearch.lowercase(
                                Locale.getDefault()))){
                            resultList.add(row)
                        }
                    }
                    filterDersList=resultList
                }
                val filterResults=FilterResults()
                filterResults.values=filterDersList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterDersList=results?.values as ArrayList<Ders>
                notifyDataSetChanged()
            }

        }
    }
}