package com.wilson.mvvmphone

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.facebook.shimmer.ShimmerFrameLayout
import com.wilson.mvvmphone.models.ShopItems
import java.util.logging.Handler

class ShopItemsListAdapter() : ListAdapter<ShopItems?, ShopItemsListAdapter.ShopItemHolder?>(DIFF_CALLBACK) {

    private var listener: OnItemClickListener? = null
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
//    private val shimmer = Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
//        .setDuration(1800) // how long the shimmering animation takes to do one full sweep
//        .setBaseAlpha(0.7f) //the alpha of the underlying children
//        .setHighlightAlpha(0.6f) // the shimmer alpha amount
//        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
//        .setAutoStart(true)
//        .build()

    fun getShopItemAt(position: Int): ShopItems? {
        return getItem(position)
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): ShopItemHolder {
        val itemView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.shopitem_card_layout, viewGroup, false)
        return ShopItemHolder(itemView)
    }

    override fun onBindViewHolder(@NonNull itemHolder: ShopItemHolder, i: Int) {
        val currentItem: ShopItems? = getItem(i)
        itemHolder.cardTitle.text = currentItem?.itemname
        itemHolder.cardPrice.visibility = View.INVISIBLE
        itemHolder.cardPrice.text = currentItem?.price.toString().plus(" ₹")
        itemHolder.cardNego.visibility = View.INVISIBLE
        itemHolder.cardShimmer.startShimmer()

        if(currentItem?.imageUrl!=null){
            Glide.with(itemHolder.itemView.context)
                .load(currentItem.imageUrl)
                .apply(requestOptions)
                .fallback(null)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("ImageLoad Error :",currentItem.imageUrl.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        itemHolder.cardShimmer.stopShimmer()
                        itemHolder.cardShimmer.visibility = View.GONE
                        itemHolder.cardPrice.visibility = View.VISIBLE
                        itemHolder.cardNego.visibility = when(currentItem.negotiable){
                            true -> View.VISIBLE
                            else -> View.INVISIBLE
                        }
                        Log.d("ImageLoad Success :",""+currentItem.itemname)
                        return false
                    }
                })
                .into(itemHolder.cardImage)
        }

    }

    inner class ShopItemHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var cardImage: ImageView = itemView.findViewById(R.id.shopitem_card_image)
        val cardTitle: TextView = itemView.findViewById(R.id.shopitem_card_title)
        val cardPrice: TextView = itemView.findViewById(R.id.shopitem_card_price)
        val cardNego: TextView = itemView.findViewById(R.id.shopitem_card_negotiable)
        val cardShimmer: ShimmerFrameLayout = itemView.findViewById(R.id.shopitem_card_shimmer)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener!!.onItemClick(getItem(position),cardImage.drawable)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(shopitem: ShopItems?,itemDrawable:Drawable?)
    }

    fun setOnItemClickListner(listener: OnItemClickListener) {
        this.listener = listener
    }


    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<ShopItems?> = object : DiffUtil.ItemCallback<ShopItems?>() {
            override fun areItemsTheSame(@NonNull oldItem: ShopItems, @NonNull newItem: ShopItems): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(@NonNull oldItem: ShopItems, @NonNull newItem: ShopItems): Boolean {
                return oldItem == newItem
            }
        }
    }
}

