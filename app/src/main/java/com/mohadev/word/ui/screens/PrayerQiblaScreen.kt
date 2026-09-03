package com.mohadev.word.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mohadev.word.data.model.CalculationMethod
import com.mohadev.word.data.model.PrayerTimesData
import com.mohadev.word.ui.components.GlassCard
import com.mohadev.word.ui.theme.*
import com.mohadev.word.ui.viewmodel.MainViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PrayerQiblaScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prayerData by viewModel.prayerTimes.collectAsState()
    val calculationMethod by viewModel.calculationMethod.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val deviceHeading by viewModel.deviceHeading.collectAsState()
    val qiblaAngle by viewModel.qiblaAngle.collectAsState()
    val isPrayerLoading by viewModel.isPrayerLoading.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var showHijriCalendarDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectDeviceLocation(context)
        } else {
            viewModel.showNotification("إذن الموقع", "يرجى منح إذن الوصول لتحديد موقعك للمواقيت بدقة")
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            viewModel.detectDeviceLocation(context)
        }
    }

    val requestLocationDetection: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.detectDeviceLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Compass continuous angle calculation to prevent 360-degree rotation glitches
    var continuousHeading by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(deviceHeading) {
        var diff = (deviceHeading - continuousHeading) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        continuousHeading += diff
    }

    val animatedHeading by animateFloatAsState(
        targetValue = continuousHeading,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "compass"
    )
    val compassRotation = -animatedHeading

    // Difference between device top axis and Qibla angle
    val diffToQibla = remember(deviceHeading, qiblaAngle) {
        ((qiblaAngle - deviceHeading + 180f + 360f) % 360f) - 180f
    }
    val isAligned = abs(diffToQibla) <= 4.5f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Auto GPS Detect Button
                    IconButton(
                        onClick = requestLocationDetection,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33E2B84D))
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = IslamicGoldPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "تحديد الموقع تلقائياً عبر GPS",
                                tint = IslamicGoldPrimary
                            )
                        }
                    }

                    // Select City Manual Button
                    IconButton(
                        onClick = { showCityDialog = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2EE2B84D))
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = "اختيار المدينة يدوياً",
                            tint = IslamicGoldPrimary
                        )
                    }

                    // Hijri Calendar Dialog Button
                    IconButton(
                        onClick = { showHijriCalendarDialog = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x332DD4BF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "التقويم الهجري والمناسبات",
                            tint = Color(0xFF2DD4BF)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مواقيت الصلاة والقبلة",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = IslamicGoldPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Qibla Compass Dial Card
        item {
            val cardBgColor by animateColorAsState(
                targetValue = if (isAligned) Color(0xFF0F3A28) else Color(0xFF0F2C20),
                label = "cardBg"
            )
            val cardBorderColor by animateColorAsState(
                targetValue = if (isAligned) IslamicGoldLight else Color(0x33E2B84D),
                label = "cardBorder"
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                contentPadding = 20.dp,
                backgroundColor = cardBgColor,
                borderColor = cardBorderColor
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33000000)
                        ) {
                            Text(
                                text = "اتجاه القبلة: ${(qiblaAngle).toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "بوصلة القبلة الدقيقة",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone forward direction indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAligned) Color(0x402ECC71) else Color(0x22FFFFFF))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = if (isAligned) Color(0xFF4EFA8B) else IslamicGoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مقدمة الهاتف (الأمام)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAligned) Color(0xFF4EFA8B) else Color(0xFFE0E0E0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compass Dial
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isAligned) {
                                        listOf(Color(0xFF1B533D), Color(0xFF0F3224), Color(0xFF081C14))
                                    } else {
                                        listOf(Color(0xFF133E2E), Color(0xFF0A2219), Color(0xFF06150F))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Rotating Dial with Cardinal markers
                        val goldColorArgb = IslamicGoldPrimary.toArgb()
                        val textMutedArgb = IslamicTextSecondary.toArgb()
                        val whiteArgb = android.graphics.Color.WHITE
                        val emeraldArgb = Color(0xFF4EFA8B).toArgb()

                        Canvas(modifier = Modifier.size(208.dp).rotate(compassRotation)) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = size.width / 2 - 8.dp.toPx()

                            // Outer Ring
                            drawCircle(
                                color = if (isAligned) IslamicGoldLight else IslamicBorderGold,
                                radius = radius,
                                center = center,
                                style = Stroke(width = if (isAligned) 3.dp.toPx() else 2.dp.toPx())
                            )

                            // Ticks and degree marks
                            for (i in 0 until 360 step 15) {
                                val rad = Math.toRadians(i.toDouble() - 90)
                                val isMajor = (i % 90 == 0)
                                val isSubMajor = (i % 45 == 0)
                                val tickLength = when {
                                    isMajor -> 14.dp.toPx()
                                    isSubMajor -> 10.dp.toPx()
                                    else -> 6.dp.toPx()
                                }
                                val start = Offset(
                                    x = (center.x + (radius - tickLength) * cos(rad)).toFloat(),
                                    y = (center.y + (radius - tickLength) * sin(rad)).toFloat()
                                )
                                val end = Offset(
                                    x = (center.x + radius * cos(rad)).toFloat(),
                                    y = (center.y + radius * sin(rad)).toFloat()
                                )
                                drawLine(
                                    color = if (isMajor) IslamicGoldPrimary else Color(0x55E2B84D),
                                    start = start,
                                    end = end,
                                    strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                                )
                            }

                            // Draw Cardinal Labels (شمال / شرق / جنوب / غرب)
                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    isAntiAlias = true
                                    textAlign = Paint.Align.CENTER
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                }

                                val cardinalRadius = radius - 24.dp.toPx()
                                val directions = listOf(
                                    0 to "ش", // North
                                    90 to "ق", // East
                                    180 to "ج", // South
                                    270 to "غ"  // West
                                )

                                for ((deg, label) in directions) {
                                    val rad = Math.toRadians(deg.toDouble() - 90)
                                    val x = (center.x + cardinalRadius * cos(rad)).toFloat()
                                    val y = (center.y + cardinalRadius * sin(rad)).toFloat() + 5.dp.toPx()

                                    paint.color = if (deg == 0) emeraldArgb else goldColorArgb
                                    paint.textSize = if (deg == 0) 15.sp.toPx() else 13.sp.toPx()
                                    canvas.nativeCanvas.drawText(label, x, y, paint)
                                }
                            }
                        }

                        // Qibla Pointer Indicator Needle pointing to Kaaba
                        Canvas(modifier = Modifier.size(190.dp).rotate(compassRotation + qiblaAngle)) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val pointerLength = size.width / 2 - 10.dp.toPx()

                            // Pointer Arrow pointing forward to Kaaba
                            val needlePath = Path().apply {
                                moveTo(center.x, center.y - pointerLength)
                                lineTo(center.x - 14.dp.toPx(), center.y)
                                lineTo(center.x, center.y - 6.dp.toPx())
                                lineTo(center.x + 14.dp.toPx(), center.y)
                                close()
                            }
                            drawPath(
                                path = needlePath,
                                color = if (isAligned) Color(0xFF4EFA8B) else IslamicGoldPrimary
                            )

                            // Kaaba Center Pin
                            drawCircle(
                                color = if (isAligned) Color(0xFF4EFA8B) else IslamicGoldAccent,
                                radius = 8.dp.toPx(),
                                center = center
                            )
                        }

                        // Center Kaaba Badge / Pivot
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isAligned) Color(0xFF0B3A26) else Color(0xFF04140E))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.Explore,
                                contentDescription = "مركز القبلة",
                                tint = if (isAligned) Color(0xFF4EFA8B) else IslamicGoldPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Guidance Feedback Status
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isAligned) Color(0x332ECC71) else Color(0x22000000),
                        border = BorderStroke(1.dp, if (isAligned) Color(0x882ECC71) else Color(0x22E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isAligned) {
                                Text(
                                    text = "🎯 أنت الآن متوجه مباشرة نحو الكعبة المشرفة!",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF4EFA8B),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "الله أكبر • تقبل الله طاعتكم",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IslamicMintLight,
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                val turnDirection = if (diffToQibla > 0) {
                                    "أدِر الهاتف ${diffToQibla.toInt()}° نحو اليمين ↻"
                                } else {
                                    "أدِر الهاتف ${abs(diffToQibla).toInt()}° نحو اليسار ↺"
                                }
                                Text(
                                    text = turnDirection,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IslamicGoldLight,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "اجعل مقدمة الهاتف متطابقة مع المؤشر الذهبي",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IslamicTextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Location & Calculation Method Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f).clickable { showCityDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = 12.dp,
                    backgroundColor = Color(0xFF10281F)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "المدينة الحالية", style = MaterialTheme.typography.labelSmall, color = IslamicTextMuted)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = currentCity, style = MaterialTheme.typography.titleSmall, color = IslamicGoldPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f).clickable { showMethodDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = 12.dp,
                    backgroundColor = Color(0xFF10281F)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "طريقة الحساب", style = MaterialTheme.typography.labelSmall, color = IslamicTextMuted)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = calculationMethod.titleArabic, style = MaterialTheme.typography.titleSmall, color = IslamicGoldPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Dedicated GPS Auto-Detect Button Card
        item {
            Button(
                onClick = requestLocationDetection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F3B2C)
                ),
                border = BorderStroke(1.dp, IslamicGoldPrimary)
            ) {
                if (isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = IslamicGoldPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "جاري تحديد الموقع عبر GPS...",
                        color = IslamicGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحديد موقعي الحالي تلقائياً (GPS)",
                        color = IslamicGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Full 5 Prayer Times Rows
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                contentPadding = 16.dp,
                backgroundColor = Color(0xFF0F2A1E)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Refresh button
                        IconButton(
                            onClick = { viewModel.fetchLivePrayerTimes() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isPrayerLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = IslamicGoldPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "تحديث المواقيت", tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "مواقيت اليوم (${prayerData.hijriDate})",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                            Text(
                                text = "مباشر من AlAdhan API • طريقة المساحة المصرية (Method 8)",
                                style = MaterialTheme.typography.labelSmall,
                                color = IslamicMintLight,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    PrayerRowItem("صلاة الفجر", prayerData.fajr, prayerData.nextPrayerName == "الفجر")
                    PrayerRowItem("الشروق", prayerData.sunrise, prayerData.nextPrayerName == "الشروق")
                    PrayerRowItem("صلاة الظهر", prayerData.dhuhr, prayerData.nextPrayerName == "الظهر")
                    PrayerRowItem("صلاة العصر", prayerData.asr, prayerData.nextPrayerName == "العصر")
                    PrayerRowItem("صلاة المغرب", prayerData.maghrib, prayerData.nextPrayerName == "المغرب")
                    PrayerRowItem("صلاة العشاء", prayerData.isha, prayerData.nextPrayerName == "العشاء")
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Interactive Hijri Calendar & Occasions Launcher Banner
        item {
            val customEvents by viewModel.customHijriEvents.collectAsState()
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showHijriCalendarDialog = true },
                shape = RoundedCornerShape(20.dp),
                contentPadding = 16.dp,
                backgroundColor = Color(0xFF072418),
                borderColor = Color(0xFF2DD4BF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F3E2B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF2DD4BF),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🌙 التقويم الهجري والمناسبات الإسلامية",
                                style = MaterialTheme.typography.titleSmall,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "عرض أيام الشهر، ربط المواعيد بمواقيت الصلاة، وتتبع المناسبات والسنن (${customEvents.size} موعد محفوظ)",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicMintLight,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "فتح التقويم",
                        tint = Color(0xFF2DD4BF)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // The Three Holy Mosques Virtues Card
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "المساجد الثلاثة وفضل الصلاة فيها",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HolyMosqueMiniCard(
                        title = "المسجد الحرام",
                        subtitle = "١٠٠ ألف صلاة",
                        icon = Icons.Default.Apartment,
                        modifier = Modifier.weight(1f)
                    )
                    HolyMosqueMiniCard(
                        title = "المسجد النبوي",
                        subtitle = "ألف صلاة",
                        icon = Icons.Default.Mosque,
                        modifier = Modifier.weight(1f)
                    )
                    HolyMosqueMiniCard(
                        title = "المسجد الأقصى",
                        subtitle = "٥٠٠ صلاة",
                        icon = Icons.Default.Place,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // City Selector Dialog
    if (showCityDialog) {
        var citySearchQuery by remember { mutableStateOf("") }
        val allCities = remember {
            listOf(
                // Saudi Arabia
                Triple("مكة المكرمة - السعودية", 21.4225, 39.8262),
                Triple("المدينة المنورة - السعودية", 24.4672, 39.6111),
                Triple("الرياض - السعودية", 24.7136, 46.6753),
                Triple("جدة - السعودية", 21.5433, 39.1728),
                Triple("الدمام - السعودية", 26.4207, 50.0888),
                Triple("الخبر - السعودية", 26.2172, 50.1971),
                Triple("الطائف - السعودية", 21.2854, 40.4222),
                Triple("تبوك - السعودية", 28.3835, 36.5662),
                Triple("أبها - السعودية", 18.2164, 42.5053),
                Triple("بريدة - السعودية", 26.3260, 43.9750),
                // Egypt
                Triple("القاهرة - مصر", 30.0444, 31.2357),
                Triple("الإسكندرية - مصر", 31.2001, 29.9187),
                Triple("الجيزة - مصر", 30.0131, 31.2089),
                Triple("المنصورة - مصر", 31.0409, 31.3785),
                Triple("طنطا - مصر", 30.7865, 31.0004),
                Triple("أسيوط - مصر", 27.1783, 31.1859),
                Triple("الزقازيق - مصر", 30.5877, 31.5020),
                Triple("بورسعيد - مصر", 31.2653, 32.3019),
                Triple("السويس - مصر", 29.9668, 32.5498),
                Triple("أسوان - مصر", 24.0889, 32.8998),
                Triple("الأقصر - مصر", 25.6872, 32.6396),
                // Palestine & Jordan & Levant
                Triple("القدس الشريف - فلسطين", 31.7683, 35.2137),
                Triple("غزة - فلسطين", 31.5017, 34.4668),
                Triple("رام الله - فلسطين", 31.9038, 35.2034),
                Triple("نابلس - فلسطين", 32.2211, 35.2544),
                Triple("الخليل - فلسطين", 31.5326, 35.0998),
                Triple("عمّان - الأردن", 31.9454, 35.9284),
                Triple("إربد - الأردن", 32.5568, 35.8469),
                Triple("الزرقاء - الأردن", 32.0728, 36.0880),
                Triple("العقبة - الأردن", 29.5321, 35.0063),
                Triple("دمشق - سوريا", 33.5138, 36.2765),
                Triple("حلب - سوريا", 36.2021, 37.1343),
                Triple("بيروت - لبنان", 33.8938, 35.5018),
                Triple("طرابلس - لبنان", 34.4367, 35.8497),
                Triple("بغداد - العراق", 33.3152, 44.3661),
                Triple("البصرة - العراق", 30.5081, 47.7835),
                Triple("أربيل - العراق", 36.1911, 44.0092),
                Triple("الموصل - العراق", 36.3400, 43.1300),
                // UAE & Gulf
                Triple("دبي - الإمارات", 25.2048, 55.2708),
                Triple("أبوظبي - الإمارات", 24.4539, 54.3773),
                Triple("الشارقة - الإمارات", 25.3573, 55.4033),
                Triple("عجمان - الإمارات", 25.4052, 55.5136),
                Triple("الكويت - الكويت", 29.3759, 47.9774),
                Triple("الدوحة - قطر", 25.2854, 51.5310),
                Triple("المنامة - البحرين", 26.2285, 50.5860),
                Triple("مسقط - سلطنة عمان", 23.5880, 58.3829),
                Triple("صلالة - سلطنة عمان", 17.0151, 54.0924),
                Triple("صنعاء - اليمن", 15.3694, 44.1910),
                Triple("عدن - اليمن", 12.7855, 45.0187),
                // North Africa
                Triple("الرباط - المغرب", 34.0209, -6.8416),
                Triple("الدار البيضاء - المغرب", 33.5731, -7.5898),
                Triple("مراكش - المغرب", 31.6295, -7.9811),
                Triple("فاس - المغرب", 34.0181, -5.0078),
                Triple("طنجة - المغرب", 35.7595, -5.8340),
                Triple("الجزائر - الجزائر", 36.7538, 3.0588),
                Triple("وهران - الجزائر", 35.6987, -0.6349),
                Triple("قسنطينة - الجزائر", 36.3650, 6.6147),
                Triple("تونس - تونس", 36.8065, 10.1815),
                Triple("صفاقس - تونس", 34.7406, 10.7603),
                Triple("سوسة - تونس", 35.8256, 10.6084),
                Triple("طرابلس - ليبيا", 32.8872, 13.1913),
                Triple("بنغازي - ليبيا", 32.1167, 20.0667),
                Triple("الخرطوم - السودان", 15.5007, 32.5599),
                Triple("نواكشوط - موريتانيا", 18.0735, -15.9582),
                // Turkey & Europe & World
                Triple("إسطنبول - تركيا", 41.0082, 28.9784),
                Triple("أنقرة - تركيا", 39.9334, 32.8597),
                Triple("لندن - المملكة المتحدة", 51.5074, -0.1278),
                Triple("باريس - فرنسا", 48.8566, 2.3522),
                Triple("برلين - ألمانيا", 52.5200, 13.4050),
                Triple("فرانكفورت - ألمانيا", 50.1109, 8.6821),
                Triple("نيويورك - أمريكا", 40.7128, -74.0060),
                Triple("واشنطن - أمريكا", 38.9072, -77.0369),
                Triple("تورونتو - كندا", 43.6532, -79.3832),
                Triple("سيدني - أستراليا", -33.8688, 151.2093),
                Triple("كوالالمبور - ماليزيا", 3.1390, 101.6869),
                Triple("جاكرتا - إندونيسيا", -6.2088, 106.8456)
            )
        }

        val filteredCities = remember(citySearchQuery) {
            if (citySearchQuery.isBlank()) {
                allCities
            } else {
                allCities.filter { it.first.contains(citySearchQuery.trim(), ignoreCase = true) }
            }
        }

        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "تحديد موقعك والمدينة لمواقيت الصلاة والقبلة",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Search Field
                    OutlinedTextField(
                        value = citySearchQuery,
                        onValueChange = { citySearchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        placeholder = { Text("ابحث عن مدينتك أو دولتك...", fontSize = 12.sp, color = IslamicTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGoldPrimary,
                            unfocusedBorderColor = Color(0x66E2B84D),
                            focusedTextColor = IslamicTextPrimary,
                            unfocusedTextColor = IslamicTextPrimary
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                        // Top GPS Auto-Detect Option
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        showCityDialog = false
                                        requestLocationDetection()
                                    },
                                color = Color(0xFF0F3B2C),
                                border = BorderStroke(1.dp, IslamicGoldPrimary)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = IslamicGoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "📍 تحديد موقعي الحالي تلقائياً (GPS)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = IslamicGoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Divider(
                                color = IslamicBorderGold.copy(alpha = 0.3f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        items(filteredCities) { (cityWithCountry, lat, lng) ->
                            val cleanCityName = cityWithCountry.substringBefore(" -")
                            val isSelected = currentCity.contains(cleanCityName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0x33E2B84D) else Color.Transparent)
                                    .clickable {
                                        viewModel.setLocation(cleanCityName, lat, lng)
                                        showCityDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                                } else {
                                    Spacer(modifier = Modifier.size(18.dp))
                                }
                                Text(text = cityWithCountry, style = MaterialTheme.typography.bodyMedium, color = IslamicTextPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("إغلاق", color = IslamicGoldPrimary)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }

    // Method Selector Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text(
                    text = "اختر هيئة الحساب الفلكي",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CalculationMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (method == calculationMethod) Color(0x33E2B84D) else Color.Transparent)
                                .clickable {
                                    viewModel.setCalculationMethod(method)
                                    showMethodDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = method == calculationMethod,
                                onClick = {
                                    viewModel.setCalculationMethod(method)
                                    showMethodDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = IslamicGoldPrimary)
                            )
                            Text(text = method.titleArabic, style = MaterialTheme.typography.bodyMedium, color = IslamicTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text("إغلاق", color = IslamicGoldPrimary)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }

    if (showHijriCalendarDialog) {
        HijriCalendarDialog(
            onDismiss = { showHijriCalendarDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun PrayerRowItem(
    name: String,
    time: String,
    isNext: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isNext) Color(0x33E2B84D) else Color(0x0FFFFFFF))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            color = if (isNext) IslamicGoldLight else IslamicTextPrimary,
            fontWeight = FontWeight.Bold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isNext) IslamicGoldPrimary else IslamicTextSecondary,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
            )
            if (isNext) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(IslamicGoldPrimary)
                )
            }
        }
    }
}

@Composable
private fun HolyMosqueMiniCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF09251B),
        border = BorderStroke(1.dp, Color(0x33E2B84D))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF051711)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = IslamicGoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = IslamicGoldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = IslamicMintLight,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

