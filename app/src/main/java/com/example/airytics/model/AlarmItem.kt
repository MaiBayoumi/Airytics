package com.example.airytics.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_item")
data class AlarmItem(
    @PrimaryKey val time: Long,
    val kind: String
) : Parcelable {

    // Write the object's data to the provided Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(time)
        parcel.writeString(kind)
    }

    // Describe the contents of the Parcelable
    override fun describeContents(): Int {
        return 0
    }

    // Companion object to facilitate the Parcelable creation
    companion object CREATOR : Parcelable.Creator<AlarmItem> {
        override fun createFromParcel(parcel: Parcel): AlarmItem {
            val time = parcel.readLong()
            val kind = parcel.readString() ?: ""
            return AlarmItem(time, kind)
        }

        override fun newArray(size: Int): Array<AlarmItem?> {
            return arrayOfNulls(size)
        }
    }
}
