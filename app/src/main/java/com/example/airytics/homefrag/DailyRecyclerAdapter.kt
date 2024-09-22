package com.example.airytics.homefrag

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airytics.R
import com.example.airytics.databinding.ItemDaysBinding
import com.example.airytics.model.Daily
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.example.airytics.utilities.Functions
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DailyRecyclerAdapter :
    ListAdapter<Daily, DailyRecyclerAdapter.DailyViewHolder>(RecyclerDiffUtilDaily()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ItemDaysBinding.inflate(inflater, parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.onBind(currentItem)
    }

    inner class DailyViewHolder(private val binding: ItemDaysBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(currentItem: Daily) {

            binding.apply {
                val context = tvDayDays.context
                val sharedPref = SettingSharedPref.getInstance(context)
                val language = sharedPref.readStringFromSettingSP(Constants.LANGUAGE)

                // Handle day format based on language
                val dayTimestamp = convertDateStringToTimestamp(currentItem.day)
                tvDayDays.text = if (language == Constants.ARABIC) {
                    Functions.formatDayOfWeek(dayTimestamp, context, "ar")
                } else {
                    Functions.formatDayOfWeek(dayTimestamp, context, "en")
                }

                // Get localized weather description directly from API or based on a local list
                tvStatusDays.text = currentItem.weatherDescription

                // Parse temperature correctly based on locale
                val numberFormat = NumberFormat.getInstance(Locale.getDefault())
                val lowTemp = numberFormat.parse(currentItem.LowTemp.replace("°C", ""))?.toDouble() ?: 0.0
                val highTemp = numberFormat.parse(currentItem.highTemp.replace("°C", ""))?.toDouble() ?: 0.0

                // Handle temperature display based on user settings
                val temperatureUnit = sharedPref.readStringFromSettingSP(Constants.TEMPERATURE)
                tvDegreeDays.text = when (temperatureUnit) {
                    Constants.KELVIN -> String.format(
                        "%.0f/%.0f°%s",
                        highTemp + 273.15, lowTemp + 273.15, context.getString(R.string.k)
                    )
                    Constants.FAHRENHEIT -> String.format(
                        "%.0f/%.0f°%s",
                        (highTemp * 9 / 5) + 32, (lowTemp * 9 / 5) + 32, context.getString(R.string.f)
                    )
                    else -> String.format(
                        "%.0f/%.0f°%s",
                        highTemp, lowTemp, context.getString(R.string.c)
                    )
                }

                // Set weather icon
                Functions.setIcon(currentItem.icon, ivIconDays)
            }
        }
    }

    private fun convertDateStringToTimestamp(dateString: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString)
            date?.time?.div(1000) ?: 0L // Convert milliseconds to seconds
        } catch (e: Exception) {
            0L // Return 0 or handle error as needed
        }
    }
}

class RecyclerDiffUtilDaily : DiffUtil.ItemCallback<Daily>() {
    override fun areItemsTheSame(oldItem: Daily, newItem: Daily): Boolean {
        return oldItem.day == newItem.day
    }

    override fun areContentsTheSame(oldItem: Daily, newItem: Daily): Boolean {
        return oldItem == newItem
    }
}
