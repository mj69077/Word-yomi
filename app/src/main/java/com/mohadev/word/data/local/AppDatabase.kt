package com.mohadev.word.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mohadev.word.data.model.AthkarItem
import com.mohadev.word.data.model.AyahEntity
import com.mohadev.word.data.model.Bookmark
import com.mohadev.word.data.model.DailyTask
import com.mohadev.word.data.model.Dua
import com.mohadev.word.data.model.Fatwa
import com.mohadev.word.data.model.HadithFavorite
import com.mohadev.word.data.model.HijriCustomEvent
import com.mohadev.word.data.model.IslamicNote
import com.mohadev.word.data.model.QuizScoreRecord
import com.mohadev.word.data.model.QuranProgress
import com.mohadev.word.data.model.TasbihRecord

@Database(
    entities = [
        DailyTask::class,
        QuranProgress::class,
        Bookmark::class,
        Dua::class,
        AthkarItem::class,
        TasbihRecord::class,
        Fatwa::class,
        AyahEntity::class,
        IslamicNote::class,
        QuizScoreRecord::class,
        HadithFavorite::class,
        HijriCustomEvent::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun quranDao(): QuranDao
    abstract fun duaDao(): DuaDao
    abstract fun athkarDao(): AthkarDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun fatwaDao(): FatwaDao
    abstract fun islamicNoteDao(): IslamicNoteDao
    abstract fun quizScoreDao(): QuizScoreDao
    abstract fun hadithFavoriteDao(): HadithFavoriteDao
    abstract fun hijriEventDao(): HijriEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_wird_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
