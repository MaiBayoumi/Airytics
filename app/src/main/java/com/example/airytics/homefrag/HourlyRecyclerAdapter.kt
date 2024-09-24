package com.example.airytics.homefrag

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airytics.R
import com.example.airytics.databinding.ItemHoursBinding
import com.example.airytics.model.Hourly
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.example.airytics.utilities.Functions

class HourlyRecyclerAdapter :
    ListAdapter<Hourly, HourlyRecyclerAdapter.HourlyViewHolder>(RecyclerDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ItemHoursBinding.inflate(inflater, parent, false)
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.onBind(currentItem)
    }

    inner class HourlyViewHolder(private val binding: ItemHoursBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(currentItem: Hourly) {
            binding.apply {
                // Format and display the date and time based on the selected language
                val language = SettingSharedPref.getInstance(tvDateHours.context)
                    .readStringFromSettingSP(Constants.LANGUAGE)

                tvDateHours.text =
                    Functions.formatDateStamp(currentItem.dt, tvDateHours.context, language)
                tvTimeHours.text = Functions.formatTimestamp(currentItem.dt, language)

                // Safely parse and display the temperature
                val temp = currentItem.temp
                val tempUnit = SettingSharedPref.getInstance(tvDegreeHours.context)
                    .readStringFromSettingSP(Constants.TEMPERATURE)

                // Log for debugging
                Log.d("HASSAN", "Temp: $temp, Unit: $tempUnit")

                // Display temperature based on unit
                tvDegreeHours.text = when (tempUnit) {
                    Constants.KELVIN -> {
                        val kelvinTemp = temp + 273.15
                        String.format(
                            "%.0f°%s",
                            kelvinTemp,
                            tvDegreeHours.context.getString(R.string.k)
                        )
                    }

                    Constants.FAHRENHEIT -> {
                        val fahrenheitTemp = temp * 9 / 5 + 32
                        String.format(
                            "%.0f°%s",
                            fahrenheitTemp,
                            tvDegreeHours.context.getString(R.string.f)
                        )
                    }

                    else -> { // Default to Celsius
                        String.format("%.0f°%s", temp, tvDegreeHours.context.getString(R.string.c))
                    }
                }

                // Set the weather icon based on the API icon code
                Functions.setIcon(currentItem.icon, ivStatusIconHours)
            }
        }

    }

    class RecyclerDiffUtil : DiffUtil.ItemCallback<Hourly>() {
        override fun areItemsTheSame(oldItem: Hourly, newItem: Hourly): Boolean {
            return oldItem.dt == newItem.dt // Assuming dt is a unique identifier for the hourly data
        }

        override fun areContentsTheSame(oldItem: Hourly, newItem: Hourly): Boolean {
            return oldItem == newItem
        }
    }
}
