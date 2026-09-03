package com.mohadev.word.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mohadev.word.data.local.HadithData
import com.mohadev.word.data.local.HadithItem
import com.mohadev.word.ui.components.GlassCard
import com.mohadev.word.ui.theme.*
import com.mohadev.word.ui.viewmodel.MainViewModel

@Composable
fun HadithLibraryDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedBook by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedHadithId by remember { mutableStateOf<Int?>(null) }
    var savedHadithIds by remember { mutableStateOf(setOf<Int>()) }

    val books = listOf("الكل", "الأربعين النووية", "رياض الصالحين")

    val filteredHadiths = remember(selectedBook, searchQuery) {
        HadithData.hadiths.filter { h ->
            val matchBook = selectedBook == "الكل" || h.book == selectedBook
            val matchSearch = searchQuery.isBlank() ||
                    h.title.contains(searchQuery, ignoreCase = true) ||
                    h.matn.contains(searchQuery, ignoreCase = true) ||
                    h.narrator.contains(searchQuery, ignoreCase = true) ||
                    h.chapter.contains(searchQuery, ignoreCase = true)
            matchBook && matchSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IslamicEmeraldDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الموسوعة الحديثية الشريفة",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الأربعين النووية ورياض الصالحين بالشرح والفوائد",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    IconButton(onClick = {
                        viewModel.showNotification("الموسوعة الحديثية", "تضم الأحاديث الصحيحة مع الشرح والتخريج والفوائد العملية")
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "معلومات", tint = IslamicGoldPrimary)
                    }
                }

                Divider(color = Color(0x33E2B84D))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Search box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث في متون الأحاديث والرواة والأبواب...", color = IslamicTextSecondary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", tint = IslamicTextSecondary)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGoldPrimary,
                            unfocusedBorderColor = Color(0x44E2B84D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0x33072016),
                            unfocusedContainerColor = Color(0x22072016)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Books filter
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(books) { book ->
                            val isSelected = selectedBook == book
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedBook = book },
                                label = { Text(book, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IslamicGoldPrimary,
                                    selectedLabelColor = IslamicEmeraldDark,
                                    containerColor = Color(0x330B291D),
                                    labelColor = IslamicGoldLight
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0x44E2B84D),
                                    selectedBorderColor = IslamicGoldPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredHadiths) { hadith ->
                            val isExpanded = expandedHadithId == hadith.id
                            val isSaved = savedHadithIds.contains(hadith.id)

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color(0x440A291E),
                                borderColor = if (isExpanded) IslamicGoldPrimary else Color(0x33E2B84D)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    // Top tag & Book
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(0x33E2B84D),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${hadith.book} • ${hadith.chapter}",
                                                color = IslamicGoldPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    savedHadithIds = if (isSaved) savedHadithIds - hadith.id else savedHadithIds + hadith.id
                                                    viewModel.showNotification(
                                                        if (!isSaved) "تم الحفظ" else "تمت الإزالة",
                                                        if (!isSaved) "تمت إضافة الحديث لقائمة حفظك" else "تمت إزالة الحديث من القائمة"
                                                    )
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = null,
                                                    tint = if (isSaved) IslamicGoldPrimary else IslamicTextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    val sendIntent = Intent().apply {
                                                        action = Intent.ACTION_SEND
                                                        putExtra(Intent.EXTRA_TEXT, "${hadith.title}\n\nعن ${hadith.narrator}:\n${hadith.matn}\n\n[${hadith.takhrij}]\n\nتطبيق الورد اليومي")
                                                        type = "text/plain"
                                                    }
                                                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الحديث"))
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicTextSecondary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = hadith.title,
                                        color = IslamicGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "عن ${hadith.narrator}:",
                                        color = Color(0xFFA5D6A7),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Matn
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0x33061A12),
                                        border = BorderStroke(0.5.dp, Color(0x33E2B84D)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = hadith.matn,
                                            color = Color.White,
                                            fontSize = 14.5.sp,
                                            lineHeight = (24).sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = hadith.takhrij,
                                        color = IslamicTextSecondary,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                expandedHadithId = if (isExpanded) null else hadith.id
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = IslamicGoldPrimary),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isExpanded) "إخفاء الشرح والفوائد" else "عرض الشرح والفوائد المستنبطة",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.toggleHadithFavoriteLocal(
                                                        hadithId = hadith.id.toLong(),
                                                        narrator = hadith.narrator,
                                                        arabicText = hadith.matn,
                                                        book = hadith.book,
                                                        chapter = hadith.chapter,
                                                        grade = hadith.takhrij
                                                    )
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.BookmarkBorder,
                                                    contentDescription = "حفظ في قاعدة البيانات",
                                                    tint = IslamicGoldPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString("${hadith.title}\n\nعن ${hadith.narrator}:\n${hadith.matn}\n\nالتخريج: ${hadith.takhrij}"))
                                                    viewModel.showNotification("تم النسخ", "تم نسخ الحديث الشريف إلى الحافظة")
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    // Expanded Explanation & Benefits
                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Divider(color = Color(0x22E2B84D))

                                            Text(
                                                text = "شرح الحديث:",
                                                color = IslamicGoldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )
                                            Text(
                                                text = hadith.explanation,
                                                color = Color(0xFFE0E0E0),
                                                fontSize = 13.sp,
                                                lineHeight = 22.sp
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = "الفوائد والدروس المستنبطة:",
                                                color = IslamicGoldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )

                                            hadith.benefits.forEachIndexed { idx, benefit ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .background(IslamicGoldPrimary, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("${idx + 1}", color = IslamicEmeraldDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = benefit,
                                                        color = Color(0xFFC8E6C9),
                                                        fontSize = 12.5.sp,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
