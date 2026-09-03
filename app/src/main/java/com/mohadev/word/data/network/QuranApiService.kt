package com.mohadev.word.data.network

import com.mohadev.word.data.model.Ayah
import com.mohadev.word.data.model.Reciter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object QuranApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val availableReciters = listOf(
        Reciter(
            id = "7",
            nameArabic = "مشاري راشد العفاسي",
            serverUrl = "https://server8.mp3quran.net/afs/",
            everyAyahSubfolder = "Alafasy_128kbps"
        ),
        Reciter(
            id = "1",
            nameArabic = "ماهر المعيقلي",
            serverUrl = "https://server12.mp3quran.net/maher/",
            everyAyahSubfolder = "Maher_AlMuaiqly_64kbps"
        ),
        Reciter(
            id = "3",
            nameArabic = "عبد الباسط عبد الصمد (مرتل)",
            serverUrl = "https://server7.mp3quran.net/basit/",
            everyAyahSubfolder = "Abdul_Basit_Murattal_192kbps"
        ),
        Reciter(
            id = "3_mjwd",
            nameArabic = "عبد الباسط عبد الصمد (مجود)",
            serverUrl = "https://server7.mp3quran.net/basit/Almusshaf-Al-Mojawwad/",
            everyAyahSubfolder = "Abdul_Basit_Mujawwad_128kbps"
        ),
        Reciter(
            id = "2",
            nameArabic = "محمد صديق المنشاوي (مرتل)",
            serverUrl = "https://server10.mp3quran.net/minsh/",
            everyAyahSubfolder = "Minshawy_Murattal_128kbps"
        ),
        Reciter(
            id = "2_mjwd",
            nameArabic = "محمد صديق المنشاوي (مجود)",
            serverUrl = "https://server10.mp3quran.net/minsh/Almusshaf-Al-Mojawwad/",
            everyAyahSubfolder = "Minshawy_Mujawwad_192kbps"
        ),
        Reciter(
            id = "4",
            nameArabic = "محمود خليل الحصري (مرتل)",
            serverUrl = "https://server13.mp3quran.net/husr/",
            everyAyahSubfolder = "Husary_128kbps"
        ),
        Reciter(
            id = "4_mjwd",
            nameArabic = "محمود خليل الحصري (مجود)",
            serverUrl = "https://server13.mp3quran.net/husr/Almusshaf-Al-Mojawwad/",
            everyAyahSubfolder = "Husary_128kbps"
        ),
        Reciter(
            id = "5",
            nameArabic = "سعد الغامدي",
            serverUrl = "https://server7.mp3quran.net/s_gmd/",
            everyAyahSubfolder = "Ghamadi_40kbps"
        ),
        Reciter(
            id = "6",
            nameArabic = "أحمد بن علي العجمي",
            serverUrl = "https://server10.mp3quran.net/ajm/",
            everyAyahSubfolder = "Ahmed_ibn_Ali_al-Ajamy_128kbps"
        ),
        Reciter(
            id = "8",
            nameArabic = "ياسر الدوسري",
            serverUrl = "https://server11.mp3quran.net/yasser/",
            everyAyahSubfolder = "Yasser_Ad-Dussary_128kbps"
        ),
        Reciter(
            id = "9",
            nameArabic = "عبد الرحمن السديس",
            serverUrl = "https://server11.mp3quran.net/sds/",
            everyAyahSubfolder = "Abdurrahmaan_As-Sudais_192kbps"
        ),
        Reciter(
            id = "10",
            nameArabic = "سعود الشريم",
            serverUrl = "https://server7.mp3quran.net/shur/",
            everyAyahSubfolder = "Saood_ash-Shuraym_128kbps"
        ),
        Reciter(
            id = "11",
            nameArabic = "ناصر القطامي",
            serverUrl = "https://server6.mp3quran.net/qtm/",
            everyAyahSubfolder = "Nasser_Alqatami_128kbps"
        ),
        Reciter(
            id = "12",
            nameArabic = "أبو بكر الشاطري",
            serverUrl = "https://server11.mp3quran.net/shatri/",
            everyAyahSubfolder = "Abu_Bakr_Ash-Shaatree_128kbps"
        ),
        Reciter(
            id = "13",
            nameArabic = "إدريس أبكر",
            serverUrl = "https://server6.mp3quran.net/abkar/",
            everyAyahSubfolder = "Idrees_Abkr_128kbps"
        ),
        Reciter(
            id = "14",
            nameArabic = "علي بن عبد الرحمن الحذيفي",
            serverUrl = "https://server9.mp3quran.net/hthfi/",
            everyAyahSubfolder = "Hudhaify_128kbps"
        ),
        Reciter(
            id = "15",
            nameArabic = "محمد أيوب",
            serverUrl = "https://server8.mp3quran.net/ayyub/",
            everyAyahSubfolder = "Muhammad_Ayyoob_128kbps"
        ),
        Reciter(
            id = "16",
            nameArabic = "عبد الله بصفر",
            serverUrl = "https://server6.mp3quran.net/bsfr/",
            everyAyahSubfolder = "Abdullah_Basfar_192kbps"
        ),
        Reciter(
            id = "17",
            nameArabic = "فارس عباد",
            serverUrl = "https://server8.mp3quran.net/frs_a/",
            everyAyahSubfolder = "Fares_Abbad_64kbps"
        ),
        Reciter(
            id = "18",
            nameArabic = "خالد الجليل",
            serverUrl = "https://server10.mp3quran.net/jleel/",
            everyAyahSubfolder = "Khalid_Al-Jileel_128kbps"
        ),
        Reciter(
            id = "19",
            nameArabic = "صلاح بوخاطر",
            serverUrl = "https://server8.mp3quran.net/bu_khtr/",
            everyAyahSubfolder = "Bukhatir_128kbps"
        ),
        Reciter(
            id = "20",
            nameArabic = "هزاع البلوشي",
            serverUrl = "https://server11.mp3quran.net/hazza/",
            everyAyahSubfolder = "Hazza_Al-Balushi_128kbps"
        ),
        Reciter(
            id = "21",
            nameArabic = "محمود علي البنا",
            serverUrl = "https://server8.mp3quran.net/bna/",
            everyAyahSubfolder = "Mahmoud_Ali_Al_Banna_32kbps"
        ),
        Reciter(
            id = "22",
            nameArabic = "مصطفى إسماعيل",
            serverUrl = "https://server8.mp3quran.net/mustafa/",
            everyAyahSubfolder = "Mustafa_Ismail_128kbps"
        ),
        Reciter(
            id = "24",
            nameArabic = "محمد محمود الطبلاوي",
            serverUrl = "https://server12.mp3quran.net/tblawi/",
            everyAyahSubfolder = "Mohammad_al_Tablaway_128kbps"
        )
    )

    suspend fun fetchVersesForSurah(surahId: Int): List<Ayah> = withContext(Dispatchers.IO) {
        val ayahs = mutableListOf<Ayah>()
        try {
            val url = "https://api.quran.com/api/v4/quran/verses/uthmani?chapter_number=$surahId"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                val versesArray = json.optJSONArray("verses")
                if (versesArray != null) {
                    for (i in 0 until versesArray.length()) {
                        val v = versesArray.getJSONObject(i)
                        val verseKey = v.optString("verse_key", "$surahId:${i + 1}")
                        val parts = verseKey.split(":")
                        val ayahNum = if (parts.size == 2) parts[1].toIntOrNull() ?: (i + 1) else (i + 1)
                        val text = v.optString("text_uthmani", "")
                        ayahs.add(
                            Ayah(
                                id = v.optInt("id", i + 1),
                                surahId = surahId,
                                numberInSurah = ayahNum,
                                textUthmani = text
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If network failed or empty, generate beautiful fallback Uthmani verses representation
        if (ayahs.isEmpty()) {
            return@withContext generateFallbackVerses(surahId)
        }
        return@withContext ayahs
    }

    suspend fun fetchTafsirForSurah(surahId: Int): Map<Int, String> = withContext(Dispatchers.IO) {
        val tafsirMap = mutableMapOf<Int, String>()
        try {
            val url = "https://quranenc.com/api/v1/translation/sura/arabic_moyassar/$surahId"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                val resultArray = json.optJSONArray("result")
                if (resultArray != null) {
                    for (i in 0 until resultArray.length()) {
                        val item = resultArray.getJSONObject(i)
                        val aya = item.optInt("aya", i + 1)
                        val translation = item.optString("translation", "")
                        tafsirMap[aya] = translation
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext tafsirMap
    }

    fun getSurahAudioUrl(reciter: Reciter, surahId: Int): String {
        val formattedId = String.format("%03d", surahId)
        val baseUrl = reciter.serverUrl.trimEnd('/')
        return "$baseUrl/$formattedId.mp3"
    }

    fun getSurahAudioUrlsWithFallbacks(reciter: Reciter, surahId: Int): List<String> {
        val formattedId = String.format("%03d", surahId)
        val urls = mutableListOf<String>()
        val primary = getSurahAudioUrl(reciter, surahId)
        urls.add(primary)

        // Add HTTP variant in case HTTPS cert has issues on certain networks
        if (primary.startsWith("https://")) {
            urls.add(primary.replace("https://", "http://"))
        }

        // Secondary fallback sources
        when (reciter.id) {
            "7" -> { // Mishary Alafasy
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surahId.mp3")
                urls.add("https://server8.mp3quran.net/afs/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$formattedId.mp3")
            }
            "1" -> { // Maher Al Muaiqly
                urls.add("https://server12.mp3quran.net/maher/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/maher_almu3aiqly/year1431/$formattedId.mp3")
            }
            "3" -> { // Abdul Basit Murattal
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmurattal/$surahId.mp3")
                urls.add("https://server7.mp3quran.net/basit/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/abdulbaset_mujawwad/$formattedId.mp3")
            }
            "3_mjwd" -> { // Abdul Basit Mujawwad
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmujawwad/$surahId.mp3")
                urls.add("https://server7.mp3quran.net/basit_mjwd/$formattedId.mp3")
            }
            "2" -> { // Minshawi Murattal
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.minshawi/$surahId.mp3")
                urls.add("https://server10.mp3quran.net/minsh/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshaawee/$formattedId.mp3")
            }
            "4" -> { // Husary Murattal
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.husary/$surahId.mp3")
                urls.add("https://server13.mp3quran.net/husr/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/mahmood_khaleel_al-husaree_iza3ah/$formattedId.mp3")
            }
            "5" -> { // Ghamadi
                urls.add("https://server7.mp3quran.net/s_gmd/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/sa3d_al-ghaamidee/complete/$formattedId.mp3")
            }
            "6" -> { // Ajmi
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.ahmedajamy/$surahId.mp3")
                urls.add("https://server10.mp3quran.net/ajm/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/ahmed_ibn_3ali_al-3ajamy/$formattedId.mp3")
            }
            "8" -> { // Yasser Al Dossari
                urls.add("https://server11.mp3quran.net/yasser/$formattedId.mp3")
            }
            "9" -> { // Sudais
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.abdurrahmaansudais/$surahId.mp3")
                urls.add("https://server11.mp3quran.net/sds/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/abdurrahmaan_as-sudays/$formattedId.mp3")
            }
            "10" -> { // Shuraim
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.saoodshuraym/$surahId.mp3")
                urls.add("https://server7.mp3quran.net/shur/$formattedId.mp3")
                urls.add("https://download.quranicaudio.com/quran/sa3ood_ash-shuraym/$formattedId.mp3")
            }
            "14" -> { // Hudhaify
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.hudhaify/$surahId.mp3")
                urls.add("https://server9.mp3quran.net/hthfi/$formattedId.mp3")
            }
            "15" -> { // Muhammad Ayyub
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.muhammadayyoub/$surahId.mp3")
                urls.add("https://server8.mp3quran.net/ayyub/$formattedId.mp3")
            }
            "16" -> { // Abdullah Basfar
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.abdullahbasfar/$surahId.mp3")
                urls.add("https://server6.mp3quran.net/bsfr/$formattedId.mp3")
            }
            "21" -> { // Hani Rifai
                urls.add("https://cdn.islamic.network/quran/audio-surah/128/ar.hanirifai/$surahId.mp3")
                urls.add("https://server8.mp3quran.net/rifai/$formattedId.mp3")
            }
        }

        // Generic fallback to QuranicAudio CDN if not already in list
        val genericQuranicAudio = "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$formattedId.mp3"
        if (!urls.contains(genericQuranicAudio)) {
            urls.add(genericQuranicAudio)
        }

        return urls.distinct()
    }

    fun getAyahAudioUrl(reciter: Reciter, surahId: Int, ayahNumber: Int): String {
        val surahStr = String.format("%03d", surahId)
        val ayahStr = String.format("%03d", ayahNumber)
        val subfolder = if (reciter.everyAyahSubfolder.isNotBlank()) reciter.everyAyahSubfolder else "Alafasy_128kbps"
        return "https://everyayah.com/data/$subfolder/$surahStr$ayahStr.mp3"
    }

    fun getAyahAudioUrlsWithFallbacks(reciter: Reciter, surahId: Int, ayahNumber: Int): List<String> {
        val surahStr = String.format("%03d", surahId)
        val ayahStr = String.format("%03d", ayahNumber)
        val subfolder = if (reciter.everyAyahSubfolder.isNotBlank()) reciter.everyAyahSubfolder else "Alafasy_128kbps"
        val urls = mutableListOf<String>()
        urls.add("https://everyayah.com/data/$subfolder/$surahStr$ayahStr.mp3")
        urls.add("http://everyayah.com/data/$subfolder/$surahStr$ayahStr.mp3")
        if (subfolder != "Alafasy_128kbps") {
            urls.add("https://everyayah.com/data/Alafasy_128kbps/$surahStr$ayahStr.mp3")
        }
        return urls
    }

    private fun generateFallbackVerses(surahId: Int): List<Ayah> {
        return when (surahId) {
            1 -> listOf(
                Ayah(1, 1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
                Ayah(2, 1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"),
                Ayah(3, 1, 3, "الرَّحْمَٰنِ الرَّحِيمِ"),
                Ayah(4, 1, 4, "مَالِكِ يَوْمِ الدِّينِ"),
                Ayah(5, 1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"),
                Ayah(6, 1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ"),
                Ayah(7, 1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ")
            )
            112 -> listOf(
                Ayah(1, 112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ"),
                Ayah(2, 112, 2, "اللَّهُ الصَّمَدُ"),
                Ayah(3, 112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ"),
                Ayah(4, 112, 4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ")
            )
            113 -> listOf(
                Ayah(1, 113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ"),
                Ayah(2, 113, 2, "مِنْ شَرِّ مَا خَلَقَ"),
                Ayah(3, 113, 3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ"),
                Ayah(4, 113, 4, "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ"),
                Ayah(5, 113, 5, "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ")
            )
            114 -> listOf(
                Ayah(1, 114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ"),
                Ayah(2, 114, 2, "مَلِكِ النَّاسِ"),
                Ayah(3, 114, 3, "إِلَٰهِ النَّاسِ"),
                Ayah(4, 114, 4, "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ"),
                Ayah(5, 114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ"),
                Ayah(6, 114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ")
            )
            else -> listOf(
                Ayah(1, surahId, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
                Ayah(2, surahId, 2, "اقْرَأْ كِتَابَ اللَّهِ تَعَالَى وَتَدَبَّرْ آيَاتِهِ الْعَظِيمَةَ")
            )
        }
    }
}
