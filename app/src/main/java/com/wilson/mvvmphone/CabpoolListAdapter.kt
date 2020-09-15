package com.wilson.mvvmphone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wilson.mvvmphone.models.Cabpools

class CabpoolListAdapter : ListAdapter<Cabpools?, CabpoolListAdapter.CabpoolHolder?>(DIFF_CALLBACK) {

    private var listener: onItemClickListener? = null

    fun getCabpoolAt(position: Int): Cabpools? {
        return getItem(position)
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): CabpoolHolder {
        val itemView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.cabpool_card_layout, viewGroup, false)
        return CabpoolHolder(itemView)
    }

    override fun onBindViewHolder(@NonNull CabpoolHolder: CabpoolHolder, i: Int) {
        val currentCabpool: Cabpools? = getItem(i)
        CabpoolHolder.cardFrom.setText(currentCabpool?.from)
        CabpoolHolder.cardTo.setText(currentCabpool?.to)
        CabpoolHolder.cardTime.setText(currentCabpool?.time?.substring(0,5))
        CabpoolHolder.cardDate.setText(currentCabpool?.date)
    }

    inner class CabpoolHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {


        val cardFrom: TextView = itemView.findViewById(R.id.cabpoolcard_from)
        val cardTo: TextView = itemView.findViewById(R.id.cabpoolcard_to)
        val cardTime: TextView = itemView.findViewById(R.id.cabpoolcard_time)
        val cardDate: TextView = itemView.findViewById(R.id.cabpoolcard_date)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener!!.onItemClick(getItem(position))
                }
            }
        }
    }

    interface onItemClickListener {
        fun onItemClick(cabpool: Cabpools?)
    }

    fun setOnItemClickListner(listener: onItemClickListener?) {
        this.listener = listener
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Cabpools?> = object : DiffUtil.ItemCallback<Cabpools?>() {
            override fun areItemsTheSame(@NonNull oldItem: Cabpools, @NonNull newItem: Cabpools): Boolean {
                return oldItem.startUserId == newItem.startUserId
            }

            override fun areContentsTheSame(@NonNull oldItem: Cabpools, @NonNull newItem: Cabpools): Boolean {
                return oldItem.from.equals(newItem.from) &&
                        oldItem.to.equals(newItem.to) &&
                        oldItem.time.equals(newItem.time) &&
                        oldItem.date.equals(newItem.date)
            }
        }
    }
}
