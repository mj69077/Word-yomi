package com.mohadev.word.data.model

data class Muezzin(
    val id: String,
    val name: String,
    val mosque: String,
    val audioUrl: String,
    val flag: String = "🕌"
)

object MuezzinData {
    val availableMuezzins = listOf(
        Muezzin(
            id = "makkah",
            name = "الشيخ علي ملا",
            mosque = "أذان المسجد الحرام (مكة المكرمة)",
            audioUrl = "https://media.blubrry.com/muslim_central_audio/podcasts.qurancentral.com/adhan/ali-ahmed-mulla.mp3",
            flag = "🕋"
        ),
        Muezzin(
            id = "madinah",
            name = "الشيخ عصام بخاري",
            mosque = "أذان المسجد النبوي الشريف (المدينة)",
            audioUrl = "https://media.blubrry.com/muslim_central_audio/podcasts.qurancentral.com/adhan/issam-bukhari.mp3",
            flag = "🕌"
        ),
        Muezzin(
            id = "alaqsa",
            name = "أذان المسجد الأقصى المبارك",
            mosque = "القدس الشريف",
            audioUrl = "https://media.blubrry.com/muslim_central_audio/podcasts.qurancentral.com/adhan/al-aqsa.mp3",
            flag = "🇵🇸"
        ),
        Muezzin(
            id = "cairo",
            name = "الشيخ عبدالباسط عبدالصمد",
            mosque = "الأذان المصري التاريخي",
            audioUrl = "https://media.blubrry.com/muslim_central_audio/podcasts.qurancentral.com/adhan/abdulbasit-abdulsamad.mp3",
            flag = "🇪🇬"
        ),
        Muezzin(
            id = "nasser",
            name = "الشيخ ناصر القطامي",
            mosque = "أذان شجي مؤثر",
            audioUrl = "https://media.blubrry.com/muslim_central_audio/podcasts.qurancentral.com/adhan/nasser-al-qatami.mp3",
            flag = "⭐"
        )
    )
}
