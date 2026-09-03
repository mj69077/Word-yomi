package com.mohadev.word.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mohadev.word.data.model.*
import com.mohadev.word.ui.theme.*
import com.mohadev.word.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MONTHLY_VIEW("التقويم والمواقيت", Icons.Default.CalendarMonth),
    ISLAMIC_EVENTS("المناسبات والسنن", Icons.Default.Mosque),
    MY_APPOINTMENTS("مواعيدي المضافة", Icons.Default.EventNote)
}

@Composable
fun HijriCalendarDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val livePrayerData by viewModel.prayerTimes.collectAsState()
    val customEvents by viewModel.customHijriEvents.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    var selectedTab by remember { mutableStateOf(CalendarTab.MONTHLY_VIEW) }

    // Current Hijri calculation based on current date
    val todayCal = remember { Calendar.getInstance() }
    val todayHijri = remember { HijriCalendarData.gregorianToHijri(todayCal.time) }
    val todayHijriYear = todayHijri.first
    val todayHijriMonth = todayHijri.second
    val todayHijriDay = todayHijri.third

    // Month Navigation State
    var displayedHijriYear by remember { mutableIntStateOf(todayHijriYear) }
    var displayedHijriMonth by remember { mutableIntStateOf(todayHijriMonth) }

    // Selected Day State
    var selectedHijriDay by remember { mutableIntStateOf(todayHijriDay) }

    // Add Event Dialog State
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Selected Day Calculated Information
    val selectedGregorianDate = remember(displayedHijriYear, displayedHijriMonth, selectedHijriDay) {
        HijriCalendarData.hijriToGregorian(displayedHijriYear, displayedHijriMonth, selectedHijriDay)
    }

    val selectedDayPrayerTimes = remember(displayedHijriYear, displayedHijriMonth, selectedHijriDay, currentCity) {
        viewModel.calculatePrayerTimesForDate(selectedGregorianDate)
    }

    val daysInMonth = remember(displayedHijriYear, displayedHijriMonth) {
        HijriCalendarData.getDaysInHijriMonth(displayedHijriYear, displayedHijriMonth)
    }

    // Determine day of week for the 1st of the Hijri month (0 = Saturday, 1 = Sunday, ..., 6 = Friday)
    val firstDayGregorian = remember(displayedHijriYear, displayedHijriMonth) {
        HijriCalendarData.hijriToGregorian(displayedHijriYear, displayedHijriMonth, 1)
    }

    val firstDayOffset = remember(firstDayGregorian) {
        val cal = Calendar.getInstance()
        cal.time = firstDayGregorian
        val javaDay = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ..., 7=Saturday
        // Convert to Arabic calendar week starting Saturday (Saturday = 0, Sunday = 1, ..., Friday = 6)
        when (javaDay) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 0
        }
    }

    // Gregorian span for month header
    val lastDayGregorian = remember(displayedHijriYear, displayedHijriMonth, daysInMonth) {
        HijriCalendarData.hijriToGregorian(displayedHijriYear, displayedHijriMonth, daysInMonth)
    }

    val gregorianSpanText = remember(firstDayGregorian, lastDayGregorian) {
        val format = SimpleDateFormat("d MMMM", Locale("ar"))
        val yearFormat = SimpleDateFormat("yyyy", Locale.US)
        "${format.format(firstDayGregorian)} - ${format.format(lastDayGregorian)} ${yearFormat.format(lastDayGregorian)} م"
    }

    // Islamic events on the selected day
    val selectedDayIslamicEvents = remember(displayedHijriMonth, selectedHijriDay) {
        HijriCalendarData.getEventsForDay(displayedHijriMonth, selectedHijriDay)
    }

    // Custom events on the selected day
    val selectedDayCustomEvents = remember(customEvents, displayedHijriYear, displayedHijriMonth, selectedHijriDay) {
        customEvents.filter {
            it.hijriMonth == displayedHijriMonth && it.hijriDay == selectedHijriDay && it.hijriYear == displayedHijriYear
        }
    }

    // Check if selected day is Monday or Thursday for fasting
    val selectedDayOfWeek = remember(selectedGregorianDate) {
        val cal = Calendar.getInstance()
        cal.time = selectedGregorianDate
        cal.get(Calendar.DAY_OF_WEEK)
    }
    val isMondayOrThursday = selectedDayOfWeek == Calendar.MONDAY || selectedDayOfWeek == Calendar.THURSDAY
    val isWhiteDay = HijriCalendarData.isWhiteDay(selectedHijriDay)
    val isFriday = selectedDayOfWeek == Calendar.FRIDAY

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 14.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF03140F),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0C2E22), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "التقويم الهجري والمناسبات",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "مربوط بمواقيت: $currentCity",
                            style = MaterialTheme.typography.labelSmall,
                            color = IslamicMintLight
                        )
                    }

                    // Add Event Quick Button
                    IconButton(
                        onClick = { showAddEventDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(IslamicGoldPrimary, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة مناسبة", tint = Color(0xFF041812), modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF082218), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalendarTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTab = tab },
                            color = if (isSelected) IslamicGoldPrimary else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF041812) else IslamicTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color(0xFF041812) else IslamicTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content View according to selected tab
                when (selectedTab) {
                    CalendarTab.MONTHLY_VIEW -> {
                        MonthlyCalendarView(
                            displayedYear = displayedHijriYear,
                            displayedMonth = displayedHijriMonth,
                            selectedDay = selectedHijriDay,
                            todayYear = todayHijriYear,
                            todayMonth = todayHijriMonth,
                            todayDay = todayHijriDay,
                            daysInMonth = daysInMonth,
                            firstDayOffset = firstDayOffset,
                            gregorianSpanText = gregorianSpanText,
                            selectedGregorianDate = selectedGregorianDate,
                            selectedDayPrayerTimes = selectedDayPrayerTimes,
                            selectedDayIslamicEvents = selectedDayIslamicEvents,
                            selectedDayCustomEvents = selectedDayCustomEvents,
                            allCustomEvents = customEvents,
                            isWhiteDay = isWhiteDay,
                            isMondayOrThursday = isMondayOrThursday,
                            isFriday = isFriday,
                            onMonthChange = { newMonth, newYear ->
                                displayedHijriMonth = newMonth
                                displayedHijriYear = newYear
                                selectedHijriDay = 1.coerceAtMost(HijriCalendarData.getDaysInHijriMonth(newYear, newMonth))
                            },
                            onJumpToToday = {
                                displayedHijriYear = todayHijriYear
                                displayedHijriMonth = todayHijriMonth
                                selectedHijriDay = todayHijriDay
                            },
                            onSelectDay = { day ->
                                selectedHijriDay = day
                            },
                            onAddEventClick = { showAddEventDialog = true },
                            onDeleteCustomEvent = { event -> viewModel.deleteCustomHijriEvent(event) }
                        )
                    }

                    CalendarTab.ISLAMIC_EVENTS -> {
                        IslamicEventsListView(
                            displayedMonth = displayedHijriMonth,
                            onSelectMonthEvent = { month, day ->
                                displayedHijriMonth = month
                                selectedHijriDay = day
                                selectedTab = CalendarTab.MONTHLY_VIEW
                            }
                        )
                    }

                    CalendarTab.MY_APPOINTMENTS -> {
                        MyCustomAppointmentsView(
                            customEvents = customEvents,
                            onAddEventClick = { showAddEventDialog = true },
                            onDeleteEvent = { viewModel.deleteCustomHijriEvent(it) },
                            onJumpToDate = { year, month, day ->
                                displayedHijriYear = year
                                displayedHijriMonth = month
                                selectedHijriDay = day
                                selectedTab = CalendarTab.MONTHLY_VIEW
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Custom Event Dialog
    if (showAddEventDialog) {
        AddCustomHijriEventDialog(
            initialHijriYear = displayedHijriYear,
            initialHijriMonth = displayedHijriMonth,
            initialHijriDay = selectedHijriDay,
            onDismiss = { showAddEventDialog = false },
            onSaveEvent = { title, year, month, day, category, desc, prayer, isFasting ->
                viewModel.addCustomHijriEvent(
                    title = title,
                    hijriYear = year,
                    hijriMonth = month,
                    hijriDay = day,
                    category = category,
                    description = desc,
                    linkedPrayer = prayer,
                    isFasting = isFasting
                )
                showAddEventDialog = false
            }
        )
    }
}

@Composable
private fun MonthlyCalendarView(
    displayedYear: Int,
    displayedMonth: Int,
    selectedDay: Int,
    todayYear: Int,
    todayMonth: Int,
    todayDay: Int,
    daysInMonth: Int,
    firstDayOffset: Int,
    gregorianSpanText: String,
    selectedGregorianDate: Date,
    selectedDayPrayerTimes: PrayerTimesData,
    selectedDayIslamicEvents: List<HijriEvent>,
    selectedDayCustomEvents: List<HijriCustomEvent>,
    allCustomEvents: List<HijriCustomEvent>,
    isWhiteDay: Boolean,
    isMondayOrThursday: Boolean,
    isFriday: Boolean,
    onMonthChange: (Int, Int) -> Unit,
    onJumpToToday: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onAddEventClick: () -> Unit,
    onDeleteCustomEvent: (HijriCustomEvent) -> Unit
) {
    val monthName = HijriCalendarData.hijriMonths.getOrElse(displayedMonth - 1) { "" }
    val monthDesc = HijriCalendarData.hijriMonthsDescriptions.getOrElse(displayedMonth - 1) { "" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Month Selector Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09291D)),
                border = BorderStroke(1.dp, Color(0x55E2B84D))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Month Button
                        IconButton(
                            onClick = {
                                if (displayedMonth > 1) {
                                    onMonthChange(displayedMonth - 1, displayedYear)
                                } else {
                                    onMonthChange(12, displayedYear - 1)
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF113D2C), CircleShape)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "الشهر السابق", tint = IslamicGoldPrimary)
                        }

                        // Month & Year Display
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$monthName $displayedYear هـ",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = gregorianSpanText,
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Next Month Button
                        IconButton(
                            onClick = {
                                if (displayedMonth < 12) {
                                    onMonthChange(displayedMonth + 1, displayedYear)
                                } else {
                                    onMonthChange(1, displayedYear + 1)
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF113D2C), CircleShape)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "الشهر التالي", tint = IslamicGoldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Month Description & Today Jump
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monthDesc,
                            color = IslamicMintLight,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = onJumpToToday,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, tint = IslamicGoldLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الذهاب لليوم", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Calendar Week Days Header (Sat -> Fri)
        item {
            val weekDaysArabic = listOf("سبت", "أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B291D), RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDaysArabic.forEach { dayName ->
                    Text(
                        text = dayName,
                        color = if (dayName == "جمعة") IslamicGoldPrimary else IslamicMintLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Calendar Days Grid
        item {
            val totalCells = firstDayOffset + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(modifier = Modifier.fillMaxWidth()) {
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val isSelected = dayNumber == selectedDay
                                val isToday = (displayedYear == todayYear && displayedMonth == todayMonth && dayNumber == todayDay)

                                // Check events on this day
                                val hasIslamicEvent = remember(displayedMonth, dayNumber) {
                                    HijriCalendarData.getEventsForDay(displayedMonth, dayNumber).isNotEmpty()
                                }
                                val hasCustomEvent = remember(allCustomEvents, displayedYear, displayedMonth, dayNumber) {
                                    allCustomEvents.any {
                                        it.hijriMonth == displayedMonth && it.hijriDay == dayNumber && it.hijriYear == displayedYear
                                    }
                                }
                                val isCellWhiteDay = HijriCalendarData.isWhiteDay(dayNumber)

                                // Corresponding Gregorian Day
                                val cellGregorian = remember(displayedYear, displayedMonth, dayNumber) {
                                    val cal = Calendar.getInstance()
                                    cal.time = HijriCalendarData.hijriToGregorian(displayedYear, displayedMonth, dayNumber)
                                    cal.get(Calendar.DAY_OF_MONTH)
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onSelectDay(dayNumber) },
                                    color = when {
                                        isSelected -> IslamicGoldPrimary
                                        isToday -> Color(0xFF174734)
                                        else -> Color(0xFF071C14)
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected || isToday) 1.5.dp else 1.dp,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> IslamicGoldPrimary
                                            hasIslamicEvent -> Color(0x88E2B84D)
                                            else -> Color(0x22E2B84D)
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Hijri Day Number
                                        Text(
                                            text = "$dayNumber",
                                            color = if (isSelected) Color(0xFF041812) else if (isToday) IslamicGoldLight else IslamicTextPrimary,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )

                                        // Indicators row (dots / badges)
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.height(8.dp)
                                        ) {
                                            if (hasIslamicEvent) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color(0xFF041812) else IslamicGoldPrimary)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                            }
                                            if (hasCustomEvent) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color(0xFF08412C) else Color(0xFF2DD4BF))
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                            }
                                            if (isCellWhiteDay) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color(0xFF041812) else Color(0xFF81E6D9))
                                                )
                                            }
                                        }

                                        // Gregorian Day Subtitle
                                        Text(
                                            text = "$cellGregorian",
                                            color = if (isSelected) Color(0xFF1B4031) else IslamicTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            } else {
                                // Empty spacer cell
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Selected Day Details Card with Prayer Times & Events
        item {
            SelectedDayDetailsSection(
                displayedYear = displayedYear,
                displayedMonth = displayedMonth,
                selectedDay = selectedDay,
                selectedGregorianDate = selectedGregorianDate,
                prayerTimes = selectedDayPrayerTimes,
                islamicEvents = selectedDayIslamicEvents,
                customEvents = selectedDayCustomEvents,
                isWhiteDay = isWhiteDay,
                isMondayOrThursday = isMondayOrThursday,
                isFriday = isFriday,
                onAddEventClick = onAddEventClick,
                onDeleteCustomEvent = onDeleteCustomEvent
            )
        }
    }
}

@Composable
private fun SelectedDayDetailsSection(
    displayedYear: Int,
    displayedMonth: Int,
    selectedDay: Int,
    selectedGregorianDate: Date,
    prayerTimes: PrayerTimesData,
    islamicEvents: List<HijriEvent>,
    customEvents: List<HijriCustomEvent>,
    isWhiteDay: Boolean,
    isMondayOrThursday: Boolean,
    isFriday: Boolean,
    onAddEventClick: () -> Unit,
    onDeleteCustomEvent: (HijriCustomEvent) -> Unit
) {
    val monthName = HijriCalendarData.hijriMonths.getOrElse(displayedMonth - 1) { "" }
    val fullGregorianStr = remember(selectedGregorianDate) {
        SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")).format(selectedGregorianDate)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF062016)),
        border = BorderStroke(1.2.dp, IslamicGoldPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header for Selected Day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "يوم $selectedDay $monthName $displayedYear هـ",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الموافق: $fullGregorianStr م",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicMintLight
                    )
                }

                // Quick Add Event Button
                Button(
                    onClick = onAddEventClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E2B)),
                    border = BorderStroke(1.dp, IslamicGoldPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة موعد", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Sunnah & Fasting Badges Row
            if (isWhiteDay || isMondayOrThursday || isFriday) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isWhiteDay) {
                        Surface(
                            color = Color(0x332DD4BF),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0x662DD4BF))
                        ) {
                            Text(
                                text = "🌙 صيام الأيام البيض (سنة مؤكدة)",
                                color = Color(0xFF2DD4BF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (isMondayOrThursday) {
                        Surface(
                            color = Color(0x33E2B84D),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0x66E2B84D))
                        ) {
                            Text(
                                text = "⭐ صيام تطوع (الإثنين/الخميس)",
                                color = IslamicGoldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (isFriday) {
                        Surface(
                            color = Color(0x3310B981),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0x6610B981))
                        ) {
                            Text(
                                text = "🕌 يوم الجمعة (سورة الكهف والصلاة على النبي)",
                                color = Color(0xFF6EE7B7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x33E2B84D), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Prayer Times Linked Section for this day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مواقيت الصلاة لهذا اليوم",
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
                Text(
                    text = "موقع: ${prayerTimes.locationName}",
                    color = IslamicTextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prayer Times 6-grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val prayers = listOf(
                    Triple("الفجر", prayerTimes.fajr, Icons.Default.Brightness3),
                    Triple("الشروق", prayerTimes.sunrise, Icons.Default.WbSunny),
                    Triple("الظهر", prayerTimes.dhuhr, Icons.Default.WbTwilight),
                    Triple("العصر", prayerTimes.asr, Icons.Default.Brightness5),
                    Triple("المغرب", prayerTimes.maghrib, Icons.Default.NightsStay),
                    Triple("العشاء", prayerTimes.isha, Icons.Default.Bedtime)
                )

                prayers.forEach { (name, time, icon) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp)),
                        color = Color(0xFF0B2F21),
                        border = BorderStroke(0.8.dp, Color(0x33E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(name, color = IslamicMintLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(time, color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Events on This Day Section
            Text(
                text = "المناسبات والمواعيد المقيدة بهذا اليوم",
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Islamic Official Events
            if (islamicEvents.isNotEmpty()) {
                islamicEvents.forEach { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF103627)),
                        border = BorderStroke(1.dp, IslamicGoldPrimary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1C523C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title, color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                Text(event.description, color = IslamicTextSecondary, fontSize = 10.5.sp)
                            }
                            if (event.isFastingRecommended) {
                                Surface(
                                    color = Color(0x44E2B84D),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("يستحب صيامه", color = IslamicGoldLight, fontSize = 9.5.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            // User Custom Events
            if (customEvents.isNotEmpty()) {
                customEvents.forEach { customEvent ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2B1E)),
                        border = BorderStroke(1.dp, Color(0xFF2DD4BF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134C37)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(customEvent.title, color = IslamicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0x332DD4BF),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = customEvent.category,
                                            color = Color(0xFF2DD4BF),
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                if (customEvent.linkedPrayer.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = customEvent.linkedPrayer,
                                            color = IslamicGoldLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (customEvent.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(customEvent.description, color = IslamicTextSecondary, fontSize = 10.sp)
                                }
                            }

                            // Delete Custom Event
                            IconButton(
                                onClick = { onDeleteCustomEvent(customEvent) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // If no events
            if (islamicEvents.isEmpty() && customEvents.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAddEventClick() },
                    color = Color(0x220C3827),
                    border = BorderStroke(1.dp, Color(0x22E2B84D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = IslamicMintLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "لا توجد مواعيد مخصصة — اضغط لإضافة موعد أو مناسبة",
                            color = IslamicMintLight,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IslamicEventsListView(
    displayedMonth: Int,
    onSelectMonthEvent: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09291D)),
                border = BorderStroke(1.dp, Color(0x66E2B84D))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🕌 دليل المناسبات الإسلامية والسنن الكبرى",
                        style = MaterialTheme.typography.titleSmall,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مناسبات وأيام مباركة وأوقات تحري السنن والفضائل المأثورة على مدار العام الهجري",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicMintLight,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Weekly Sunnahs
        item {
            Text(
                text = "⭐ سنن وفضائل أسبوعية مأثورة",
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        items(HijriCalendarData.weeklySunnahs) { sunnah ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF071C14)),
                border = BorderStroke(1.dp, Color(0x22E2B84D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(sunnah, color = IslamicTextPrimary, fontSize = 11.5.sp, modifier = Modifier.weight(1f))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // All Major Islamic Events List
        item {
            Text(
                text = "📅 المناسبات والشهور الهجرية (اضغط للانتقال في التقويم)",
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        items(HijriCalendarData.importantEvents) { event ->
            val monthName = HijriCalendarData.hijriMonths.getOrElse(event.hijriMonth - 1) { "" }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectMonthEvent(event.hijriMonth, event.hijriDay) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF082218)),
                border = BorderStroke(1.dp, Color(0x44E2B84D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .width(55.dp)
                            .background(Color(0xFF113D2C), RoundedCornerShape(10.dp))
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${event.hijriDay}", color = IslamicGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(monthName, color = IslamicMintLight, fontSize = 9.sp, maxLines = 1)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(event.title, color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0x33E2B84D),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(event.category, color = IslamicGoldLight, fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(event.description, color = IslamicTextSecondary, fontSize = 10.5.sp)
                    }

                    if (event.isFastingRecommended) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.NightsStay, contentDescription = "صيام", tint = Color(0xFF2DD4BF), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MyCustomAppointmentsView(
    customEvents: List<HijriCustomEvent>,
    onAddEventClick: () -> Unit,
    onDeleteEvent: (HijriCustomEvent) -> Unit,
    onJumpToDate: (Int, Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09291D)),
                border = BorderStroke(1.dp, Color(0x66E2B84D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📝 مواعيدي ومناسباتي الشخصية",
                            style = MaterialTheme.typography.titleSmall,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "إجمالي المواعيد المحفوظة: ${customEvents.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicMintLight,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onAddEventClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF041812), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("موعد جديد", color = Color(0xFF041812), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        if (customEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF071C14)),
                    border = BorderStroke(1.dp, Color(0x22E2B84D))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("لا توجد مواعيد مسجلة حتى الآن", color = IslamicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "يمكنك جدولة مناسباتك، ختماتك، صيامك، أو حلقات العلم وربطها بمواقيت الصلاة",
                            color = IslamicTextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onAddEventClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF113D2C)),
                            border = BorderStroke(1.dp, IslamicGoldPrimary)
                        ) {
                            Text("إضافة موعدك الأول", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(customEvents) { event ->
                val monthName = HijriCalendarData.hijriMonths.getOrElse(event.hijriMonth - 1) { "" }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onJumpToDate(event.hijriYear, event.hijriMonth, event.hijriDay) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF082218)),
                    border = BorderStroke(1.dp, Color(0x442DD4BF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .width(60.dp)
                                .background(Color(0xFF0D3325), RoundedCornerShape(10.dp))
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${event.hijriDay}", color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(monthName, color = IslamicMintLight, fontSize = 9.sp, maxLines = 1)
                            Text("${event.hijriYear}هـ", color = IslamicTextSecondary, fontSize = 8.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(event.title, color = IslamicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0x332DD4BF),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(event.category, color = Color(0xFF2DD4BF), fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }

                            if (event.linkedPrayer.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(event.linkedPrayer, color = IslamicGoldLight, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (event.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(event.description, color = IslamicTextSecondary, fontSize = 10.5.sp)
                            }
                        }

                        IconButton(
                            onClick = { onDeleteEvent(event) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomHijriEventDialog(
    initialHijriYear: Int,
    initialHijriMonth: Int,
    initialHijriDay: Int,
    onDismiss: () -> Unit,
    onSaveEvent: (
        title: String,
        year: Int,
        month: Int,
        day: Int,
        category: String,
        description: String,
        linkedPrayer: String,
        isFasting: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedYear by remember { mutableIntStateOf(initialHijriYear) }
    var selectedMonth by remember { mutableIntStateOf(initialHijriMonth) }
    var selectedDay by remember { mutableIntStateOf(initialHijriDay) }
    var selectedCategory by remember { mutableStateOf("موعد شخصي") }
    var selectedPrayer by remember { mutableStateOf("بعد صلاة الفجر") }
    var isFastingDay by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    val categories = listOf("موعد شخصي", "مناسبة إسلامية", "صيام وتطوع", "ورد وعبادة", "حلقة علم")
    val prayerOptions = listOf(
        "بدون ربط بوقت صلاة",
        "بعد صلاة الفجر 🌅",
        "وقت الضحى ☀️",
        "قبل صلاة الظهر 🕌",
        "بعد صلاة الظهر 🕋",
        "بعد صلاة العصر ⏱️",
        "عند صلاة المغرب (وقت الإفطار) 🌇",
        "بعد صلاة العشاء 🌌",
        "وقت السحر وقيام الليل 🌙"
    )

    val quickTitles = listOf("ختمة سورة الكهف", "صيام تطوع", "مجلس تدبر وحديث", "صلة رحم وزيارة", "عمرة وزيارة الحرم", "ورد أذكار خاص")

    val daysInSelectedMonth = remember(selectedYear, selectedMonth) {
        HijriCalendarData.getDaysInHijriMonth(selectedYear, selectedMonth)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
                .clip(RoundedCornerShape(22.dp)),
            color = Color(0xFF041812),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 إضافة موعد أو مناسبة هجرية",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المناسبة أو الموعد *", color = IslamicMintLight) },
                    placeholder = { Text("مثال: صيام يوم الإثنين، درس فقهي...", color = IslamicTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGoldPrimary,
                        unfocusedBorderColor = Color(0x44E2B84D),
                        focusedTextColor = IslamicTextPrimary,
                        unfocusedTextColor = IslamicTextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick suggestions
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickTitles) { suggestion ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { title = suggestion },
                            color = Color(0xFF0A2B1E),
                            border = BorderStroke(0.6.dp, Color(0x33E2B84D))
                        ) {
                            Text(
                                text = suggestion,
                                color = IslamicGoldLight,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hijri Date Pickers Row (Day + Month + Year)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Day Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("اليوم (${selectedDay})", color = IslamicMintLight, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color(0xFF0B291D),
                            border = BorderStroke(1.dp, Color(0x44E2B84D))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (selectedDay > 1) selectedDay-- else selectedDay = daysInSelectedMonth },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                }
                                Text("$selectedDay", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                IconButton(
                                    onClick = { if (selectedDay < daysInSelectedMonth) selectedDay++ else selectedDay = 1 },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Month Selector
                    Column(modifier = Modifier.weight(1.8f)) {
                        Text("الشهر الهجري", color = IslamicMintLight, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color(0xFF0B291D),
                            border = BorderStroke(1.dp, Color(0x44E2B84D))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (selectedMonth > 1) selectedMonth-- else selectedMonth = 12 },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    HijriCalendarData.hijriMonths.getOrElse(selectedMonth - 1) { "" },
                                    color = IslamicGoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                                IconButton(
                                    onClick = { if (selectedMonth < 12) selectedMonth++ else selectedMonth = 1 },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Selection
                Text("التصنيف", color = IslamicMintLight, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat },
                            color = if (isSelected) IslamicGoldPrimary else Color(0xFF0A2B1E),
                            border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0x33E2B84D))
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color(0xFF041812) else IslamicTextPrimary,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Linked Prayer Time Selector
                Text("الربط بموعد صلاة محدد", color = IslamicMintLight, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(prayerOptions) { prayer ->
                        val isSelected = selectedPrayer == prayer
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPrayer = prayer },
                            color = if (isSelected) Color(0xFF1B4D39) else Color(0xFF082218),
                            border = BorderStroke(1.dp, if (isSelected) IslamicGoldPrimary else Color(0x22E2B84D))
                        ) {
                            Text(
                                text = prayer,
                                color = if (isSelected) IslamicGoldLight else IslamicTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fasting Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF09251B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تذكير بصيام هذا اليوم المبارك", color = IslamicTextPrimary, fontSize = 11.5.sp)
                    }
                    Switch(
                        checked = isFastingDay,
                        onCheckedChange = { isFastingDay = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF041812),
                            checkedTrackColor = IslamicGoldPrimary,
                            uncheckedTrackColor = Color(0xFF041812)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes / Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("تفاصيل وملاحظات إضافية (اختياري)", color = IslamicMintLight) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGoldPrimary,
                        unfocusedBorderColor = Color(0x44E2B84D),
                        focusedTextColor = IslamicTextPrimary,
                        unfocusedTextColor = IslamicTextPrimary
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Save Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Text("إلغاء", color = IslamicTextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSaveEvent(
                                    title.trim(),
                                    selectedYear,
                                    selectedMonth,
                                    selectedDay,
                                    selectedCategory,
                                    description.trim(),
                                    if (selectedPrayer == "بدون ربط بوقت صلاة") "" else selectedPrayer,
                                    isFastingDay
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF041812), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ الموعد 📅", color = Color(0xFF041812), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
