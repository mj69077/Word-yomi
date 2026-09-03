package com.mohadev.word.data.model

import java.util.Calendar
import java.util.Date

data class HijriEvent(
    val title: String,
    val hijriMonth: Int, // 1 to 12
    val hijriDay: Int,
    val description: String,
    val isFastingRecommended: Boolean = false,
    val category: String = "مناسبة إسلامية"
)

object HijriCalendarData {
    val hijriMonths = listOf(
        "محرّم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان المبارك",
        "شوّال",
        "ذو القعدة",
        "ذو الحجة"
    )

    val hijriMonthsDescriptions = listOf(
        "أول شهور السنة الهجرية وأحد الأشهر الحرم وفيه يوم عاشوراء",
        "الشهر الثاني في التقويم الهجري",
        "الشهر الثالث وفيه ولد النبي محمد ﷺ",
        "الشهر الرابع من التقويم الهجري",
        "الشهر الخامس من شهور السنة الهجرية",
        "الشهر السادس من شهور السنة الهجرية",
        "أحد الأشهر الحرم وفيه معجزة الإسراء والمعراج",
        "شهر ترفع فيه الأعمال وفيه ليلة النصف من شعبان",
        "شهر الصيام والقرآن والقيام وفيه ليلة القدر المباركة",
        "شهر عيد الفطر وفيه صيام الست من شوال",
        "أحد الأشهر الحرم الثلاثة المتوالية والاستعداد لموسم الحج",
        "شهر الحج وفيه العشر الأوائل ويوم عرفة وعيد الأضحى"
    )

    val importantEvents = listOf(
        HijriEvent("رأس السنة الهجرية", 1, 1, "بداية العام الهجري الجديد وتجديد النية وهجرة الحبيب ﷺ", true, "عام هجري"),
        HijriEvent("يوم تاسوعاء", 1, 9, "يستحب صيامه مع عاشوراء مخالفة لأهل الكتاب", true, "صيام مستحب"),
        HijriEvent("يوم عاشوراء", 1, 10, "صيام يوم عاشوراء يكفر ذنوب السنة الماضية بنص الحديث الصحيح", true, "صيام مستحب"),
        HijriEvent("المولد النبوي الشريف", 3, 12, "ذكرى مولد سيد ولد آدم وخير الأنام نبينا محمد ﷺ", false, "سيرة نبوية"),
        HijriEvent("ليلة الإسراء والمعراج", 7, 27, "ذكرى معجزة الإسراء والمعراج وفرض الصلوات الخمس", false, "مناسبة عظيمة"),
        HijriEvent("ليلة النصف من شعبان", 8, 15, "فضل ليلة النصف من شعبان والاستعداد لرمضان", true, "شعبان"),
        HijriEvent("بداية شهر رمضان المبارك", 9, 1, "شهر الصيام والقيام ونزول القرآن الكريم وتفتح فيه أبواب الجنان", true, "رمضان"),
        HijriEvent("غزوة بدر الكبرى", 9, 17, "يوم الفرقان يوم التقى الجمعان ونصر الله المؤمنين", false, "تاريخ إسلامي"),
        HijriEvent("فتح مكة المكرمة", 9, 20, "دخول النبي ﷺ مكة فاتحاً ودخول الناس في دين الله أفواجاً", false, "تاريخ إسلامي"),
        HijriEvent("ليلة القدر المباركة (تحري الأوتار)", 9, 27, "ليلة خير من ألف شهر تتنزل فيها الملائكة والروح", false, "رمضان"),
        HijriEvent("عيد الفطر المبارك", 10, 1, "يوم الجائزة والفرح والسرور بعد إتمام صيام شهر رمضان وزكاة الفطر", false, "عيد إسلامي"),
        HijriEvent("صيام الست من شوال", 10, 2, "من صام رمضان ثم أتبعه بست من شوال كان كصيام الدهر", true, "صيام مستحب"),
        HijriEvent("بداية الأشهر الحرم (ذو القعدة)", 11, 1, "أحد الأشهر الحرم الأربعة التي عظم الله شأنها", false, "أشهر حرم"),
        HijriEvent("عشر ذي الحجة المباركة", 12, 1, "أفضل أيام الدنيا، العمل الصالح فيها أحب إلى الله من سائر الأيام", true, "عشر ذي الحجة"),
        HijriEvent("يوم التروية", 12, 8, "انطلاق الحجاج إلى منى وبدء مناسك الحج المباركة", true, "مناسك الحج"),
        HijriEvent("يوم عرفة المبارك", 12, 9, "أعظم أيام الحج وصيامه يكفر سنة ماضية وسنة باقية لغير الحاج", true, "صيام مؤكد"),
        HijriEvent("عيد الأضحى المبارك (يوم النحر)", 12, 10, "يوم النحر والتقرب إلى الله بالأضاحي وذكر الله وتكبيره", false, "عيد إسلامي"),
        HijriEvent("أيام التشريق الثلاثة", 12, 11, "أيام أكل وشرب وذكر لله تعالى وتكبير مقيد خلف الصلوات", false, "عيد الأضحى")
    )

    val weeklySunnahs = listOf(
        "قراءة سورة الكهف يوم الجمعة نور ما بين الجمعتين",
        "الإكثار من الصلاة على النبي ﷺ ليلة الجمعة ويومها",
        "ساعة الاستجابة عصر يوم الجمعة قبل المغرب",
        "صيام يومي الإثنين والخميس ترفع فيهما الأعمال إلى الله",
        "صيام الأيام البيض (13 و 14 و 15 من كل شهر هجري)"
    )

    fun getEventsForDay(month: Int, day: Int): List<HijriEvent> {
        return importantEvents.filter { it.hijriMonth == month && it.hijriDay == day }
    }

    fun getEventsForMonth(month: Int): List<HijriEvent> {
        return importantEvents.filter { it.hijriMonth == month }
    }

    fun isWhiteDay(day: Int): Boolean = day in 13..15

    // High accuracy Julian Day & Hijri conversion algorithms
    fun gregorianToJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = (y / 100.0).toInt()
        val b = 2 - a + (a / 4.0).toInt()
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
    }

    fun julianDayToHijri(jd: Double): Triple<Int, Int, Int> {
        val l = (jd - 1948440 + 10632).toInt()
        val n = ((l - 1) / 10631).toInt()
        val l2 = l - 10631 * n + 354
        val j = (((10985 - l2) / 5316).toInt() * ((50 * l2) / 17719).toInt()) + (((l2) / 5670).toInt() * ((43 * l2) / 15238).toInt())
        val l3 = l2 - (((30 - j) / 15).toInt() * ((17719 * j) / 50).toInt()) - (((j) / 16).toInt() * ((15238 * j) / 43).toInt()) + 29
        val month = ((24 * l3) / 709).toInt()
        val day = l3 - ((709 * month) / 24).toInt()
        val year = 30 * n + j - 30
        return Triple(year, month.coerceIn(1, 12), day.coerceIn(1, 30))
    }

    fun hijriToJulianDay(year: Int, month: Int, day: Int): Double {
        return ((11 * year + 3) / 30) + 354.0 * year + 30.0 * month - ((month - 1) / 2) + day + 1948440 - 385
    }

    fun julianDayToGregorian(jd: Double): Calendar {
        val z = (jd + 0.5).toInt()
        val alpha = ((z - 1867216.25) / 36524.25).toInt()
        val a = if (z < 2299161) z else z + 1 + alpha - (alpha / 4)
        val b = a + 1524
        val c = ((b - 122.1) / 365.25).toInt()
        val d = (365.25 * c).toInt()
        val e = ((b - d) / 30.6001).toInt()
        val day = b - d - (30.6001 * e).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 12, 0, 0)
        return cal
    }

    fun gregorianToHijri(date: Date = Date()): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        cal.time = date
        val jd = gregorianToJulianDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        return julianDayToHijri(jd)
    }

    fun hijriToGregorian(year: Int, month: Int, day: Int): Date {
        val jd = hijriToJulianDay(year, month, day)
        return julianDayToGregorian(jd).time
    }

    fun getDaysInHijriMonth(year: Int, month: Int): Int {
        // Hijri months alternate between 30 and 29, with 12th month having 30 in leap years
        val isLeap = ((11 * year + 14) % 30) < 11
        return if (month % 2 == 1) 30 else if (month == 12 && isLeap) 30 else 29
    }
}

