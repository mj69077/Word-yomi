package com.mohadev.word.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hijri_custom_events")
data class HijriCustomEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val hijriYear: Int,
    val hijriMonth: Int, // 1 to 12
    val hijriDay: Int,   // 1 to 30
    val category: String = "موعد شخصي", // مناسبة إسلامية, موعد شخصي, صيام وتطوع, ورد وعبادة, حلقة علم
    val description: String = "",
    val linkedPrayer: String = "", // e.g. "بعد صلاة الفجر", "مع أذان الظهر", "بين العصر والمغرب", "بعد صلاة العشاء"
    val isFastingDay: Boolean = false,
    val isCompleted: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
