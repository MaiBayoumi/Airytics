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
import java.text.SimpleDateFormat
import java.util.Locale

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
            Log.d("HASSAN", "onBind called for position: ${adapterPosition}")
            Log.d("HASSAN", "Current Item: $currentItem")
            binding.apply {
                tvStatusDays.text = currentItem.weatherDescription


                val context = tvDayDays.context

                val language = SettingSharedPref.getInstance(context).readStringFromSettingSP(Constants.LANGUAGE)
                val dayTimestamp = convertDateStringToTimestamp(currentItem.day)
                tvDayDays.text = Functions.formatDayOfWeek(dayTimestamp, context, language)
                Log.d("HASSAN", "${tvDayDays.text}")

                // Convert temperature from String to Double
                val lowTemp = currentItem.LowTemp.replace("°C", "").toDoubleOrNull() ?: 0.0
                val highTemp = currentItem.highTemp.replace("°C", "").toDoubleOrNull() ?: 0.0
                Log.d("HASSAN", "LowTemp: $lowTemp, HighTemp: $highTemp")




                val temperatureUnit = SettingSharedPref.getInstance(context).readStringFromSettingSP(Constants.TEMPERATURE)
                when (temperatureUnit) {
                    Constants.KELVIN -> tvDegreeDays.text = String.format(
                        "%.0f/%.0f°%s",
                        highTemp + 273.15, lowTemp + 273.15, context.getString(R.string.k)
                    )
                    Constants.FAHRENHEIT -> tvDegreeDays.text = String.format(
                        "%.0f/%.0f°%s",
                        (highTemp - 273.15) * 9 / 5 + 32, (lowTemp - 273.15) * 9 / 5 + 32, context.getString(R.string.f)
                    )
                    else -> tvDegreeDays.text = String.format(
                        "%.0f/%.0f°%s",
                        highTemp, lowTemp, context.getString(R.string.c)
                    )
                }

                Functions.setIcon(
                    currentItem.icon,
                    ivIconDays
                )
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
