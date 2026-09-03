package com.mohadev.word.data.local

import androidx.room.*
import com.mohadev.word.data.model.HijriCustomEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface HijriEventDao {
    @Query("SELECT * FROM hijri_custom_events ORDER BY hijriYear ASC, hijriMonth ASC, hijriDay ASC, id DESC")
    fun getAllEvents(): Flow<List<HijriCustomEvent>>

    @Query("SELECT * FROM hijri_custom_events WHERE hijriMonth = :month AND hijriDay = :day ORDER BY id DESC")
    fun getEventsForDay(month: Int, day: Int): Flow<List<HijriCustomEvent>>

    @Query("SELECT * FROM hijri_custom_events WHERE hijriMonth = :month ORDER BY hijriDay ASC, id DESC")
    fun getEventsForMonth(month: Int): Flow<List<HijriCustomEvent>>

    @Query("SELECT COUNT(*) FROM hijri_custom_events")
    fun getEventsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: HijriCustomEvent): Long

    @Update
    suspend fun updateEvent(event: HijriCustomEvent)

    @Delete
    suspend fun deleteEvent(event: HijriCustomEvent)

    @Query("DELETE FROM hijri_custom_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM hijri_custom_events")
    suspend fun clearAllEvents()
}
