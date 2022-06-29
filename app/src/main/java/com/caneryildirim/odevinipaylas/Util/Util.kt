package com.caneryildirim.odevinipaylas.Util

import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.caneryildirim.odevinipaylas.R

fun ImageView.downloadFromUrl(url: String?,context: Context){

    val options = RequestOptions()
        .placeholder(placeholderProgressBar(context))


    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(url)
        .into(this)

}

fun placeholderProgressBar(context: Context) : CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 8f
        centerRadius = 40f
        start()
    }
}