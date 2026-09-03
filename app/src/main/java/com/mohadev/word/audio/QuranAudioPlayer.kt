package com.mohadev.word.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import com.mohadev.word.data.model.Muezzin
import com.mohadev.word.data.model.MuezzinData
import com.mohadev.word.data.model.RadioStation
import com.mohadev.word.data.model.Reciter
import com.mohadev.word.data.model.Surah
import com.mohadev.word.data.network.QuranApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentSurah: Surah? = null,
    val currentAyahNumber: Int? = null,
    val currentRadio: RadioStation? = null,
    val currentMuezzin: Muezzin? = null,
    val currentAudioTitle: String? = null,
    val currentReciter: Reciter = QuranApiService.availableReciters.first(),
    val selectedMuezzin: Muezzin = MuezzinData.availableMuezzins.first(),
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutesRemaining: Int = 0,
    val autoAdvanceAyah: Boolean = true,
    val isLooping: Boolean = false,
    val currentLoopCount: Int = 0,
    val targetLoopCount: Int = 1,
    val errorMessage: String? = null
)

class QuranAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun setSelectedMuezzin(muezzin: Muezzin) {
        _playbackState.value = _playbackState.value.copy(selectedMuezzin = muezzin)
    }

    fun playSurah(surah: Surah, reciter: Reciter = _playbackState.value.currentReciter) {
        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentSurah = surah,
            currentAyahNumber = null,
            currentRadio = null,
            currentMuezzin = null,
            currentAudioTitle = "سورة ${surah.nameArabic} - ${reciter.nameArabic}",
            currentReciter = reciter,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0,
            isLooping = false
        )

        val candidateUrls = QuranApiService.getSurahAudioUrlsWithFallbacks(reciter, surah.id)
        playCandidateUrls(
            urls = candidateUrls,
            index = 0,
            onComplete = {
                stopProgressTracking()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentPositionMs = 0
                )
            }
        )
    }

    fun playAyah(
        surah: Surah,
        ayahNumber: Int,
        reciter: Reciter = _playbackState.value.currentReciter,
        autoAdvance: Boolean = true
    ) {
        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentSurah = surah,
            currentAyahNumber = ayahNumber,
            autoAdvanceAyah = autoAdvance,
            currentRadio = null,
            currentMuezzin = null,
            currentAudioTitle = "سورة ${surah.nameArabic} (الآية $ayahNumber) - ${reciter.nameArabic}",
            currentReciter = reciter,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0
        )

        val candidateUrls = QuranApiService.getAyahAudioUrlsWithFallbacks(reciter, surah.id, ayahNumber)
        playCandidateUrls(
            urls = candidateUrls,
            index = 0,
            onComplete = {
                if (_playbackState.value.autoAdvanceAyah && ayahNumber < surah.versesCount) {
                    playAyah(surah, ayahNumber + 1, reciter, autoAdvance = true)
                } else {
                    stopProgressTracking()
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPositionMs = 0
                    )
                }
            }
        )
    }

    private fun playCandidateUrls(
        urls: List<String>,
        index: Int,
        onComplete: () -> Unit
    ) {
        if (index >= urls.size) {
            _playbackState.value = _playbackState.value.copy(
                isLoading = false,
                isPlaying = false,
                errorMessage = "تعذر تشغيل التلاوة. يرجى التحقق من الإنترنت أو اختيار قارئ آخر"
            )
            return
        }

        val currentUrl = urls[index]
        Log.d("QuranAudioPlayer", "Attempting playback ($index/${urls.size}): $currentUrl")

        try {
            mediaPlayer?.release()
            val mp = MediaPlayer()
            mediaPlayer = mp

            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            try {
                mp.setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            } catch (e: Exception) {
                // Ignore wake mode failure on restricted profiles
            }

            // ملاحظة: تم إسقاط تمرير Headers مخصصة (User-Agent/Connection) إلى setDataSource
            // لأنها كانت السبب في فشل التشغيل الصامت على كثير من الأجهزة وخوادم mp3quran.net
            // التي تُجري إعادة توجيه HTTP. الاعتماد على setDataSource(url) المباشر أكثر ثباتًا.
            try {
                mp.setDataSource(currentUrl)
            } catch (eRaw: Exception) {
                Log.e("QuranAudioPlayer", "setDataSource failed for $currentUrl: ${eRaw.message}")
                playCandidateUrls(urls, index + 1, onComplete)
                return
            }

            mp.setOnPreparedListener { preparedMp ->
                try {
                    applyPlaybackSpeed()
                    preparedMp.start()
                    _playbackState.value = _playbackState.value.copy(
                        isLoading = false,
                        isPlaying = true,
                        durationMs = preparedMp.duration
                    )
                    startProgressTracking()
                } catch (e: Exception) {
                    Log.e("QuranAudioPlayer", "Error starting prepared player: ${e.message}")
                    playCandidateUrls(urls, index + 1, onComplete)
                }
            }

            mp.setOnCompletionListener {
                onComplete()
            }

            mp.setOnErrorListener { _, what, extra ->
                Log.w("QuranAudioPlayer", "MediaPlayer error: what=$what, extra=$extra for $currentUrl")
                stopProgressTracking()
                playCandidateUrls(urls, index + 1, onComplete)
                true
            }

            mp.prepareAsync()

        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Exception initializing player for $currentUrl: ${e.message}")
            playCandidateUrls(urls, index + 1, onComplete)
        }
    }

    fun nextAyah() {
        val currentSurah = _playbackState.value.currentSurah ?: return
        val currentAyah = _playbackState.value.currentAyahNumber ?: return
        if (currentAyah < currentSurah.versesCount) {
            playAyah(currentSurah, currentAyah + 1, _playbackState.value.currentReciter)
        }
    }

    fun previousAyah() {
        val currentSurah = _playbackState.value.currentSurah ?: return
        val currentAyah = _playbackState.value.currentAyahNumber ?: return
        if (currentAyah > 1) {
            playAyah(currentSurah, currentAyah - 1, _playbackState.value.currentReciter)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        applyPlaybackSpeed()
    }

    private fun applyPlaybackSpeed() {
        try {
            mediaPlayer?.let { mp ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val params = mp.playbackParams
                    params.speed = _playbackState.value.playbackSpeed
                    mp.playbackParams = params
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = 0)
            return
        }
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = minutes)
        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60_000)
                remaining--
                _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = remaining)
            }
            stop()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = 0)
    }

    fun playRadio(radio: RadioStation) {
        if (_playbackState.value.currentRadio?.id == radio.id && _playbackState.value.isPlaying) {
            togglePlayPause()
            return
        }

        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentRadio = radio,
            currentSurah = null,
            currentMuezzin = null,
            currentAudioTitle = radio.name,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0,
            isLooping = false
        )

        val candidateUrls = listOf(
            radio.url,
            if (radio.url.startsWith("https://")) radio.url.replace("https://", "http://") else radio.url
        )

        playCandidateUrls(
            urls = candidateUrls,
            index = 0,
            onComplete = {
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            }
        )
    }

    fun playAdhan(muezzin: Muezzin = _playbackState.value.selectedMuezzin) {
        if (_playbackState.value.currentMuezzin?.id == muezzin.id && _playbackState.value.isPlaying) {
            stop()
            return
        }

        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentMuezzin = muezzin,
            selectedMuezzin = muezzin,
            currentSurah = null,
            currentRadio = null,
            currentAudioTitle = "صوت الأذان المبارك - ${muezzin.name}",
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0,
            isLooping = false
        )

        val candidateUrls = listOf(
            muezzin.audioUrl,
            if (muezzin.audioUrl.startsWith("https://")) muezzin.audioUrl.replace("https://", "http://") else muezzin.audioUrl
        )

        playCandidateUrls(
            urls = candidateUrls,
            index = 0,
            onComplete = {
                stopProgressTracking()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            }
        )
    }

    fun playAthkarPlaylist(title: String, audioUrl: String) {
        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentSurah = null,
            currentRadio = null,
            currentMuezzin = null,
            currentAudioTitle = title,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0,
            isLooping = false
        )

        val candidateUrls = listOf(
            audioUrl,
            if (audioUrl.startsWith("https://")) audioUrl.replace("https://", "http://") else audioUrl
        )

        playCandidateUrls(
            urls = candidateUrls,
            index = 0,
            onComplete = {
                stopProgressTracking()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            }
        )
    }

    fun togglePlayPause() {
        val mp = mediaPlayer
        if (mp == null) {
            val state = _playbackState.value
            val surah = state.currentSurah
            if (surah != null) {
                if (state.currentAyahNumber != null) {
                    playAyah(surah, state.currentAyahNumber, state.currentReciter)
                } else {
                    playSurah(surah, state.currentReciter)
                }
                return
            }
            val radio = state.currentRadio
            if (radio != null) {
                playRadio(radio)
                return
            }
            val muezzin = state.currentMuezzin
            if (muezzin != null) {
                playAdhan(muezzin)
                return
            }
            return
        }

        try {
            if (mp.isPlaying) {
                mp.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                stopProgressTracking()
            } else {
                mp.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracking()
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Error toggling play/pause: ${e.message}")
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Error seeking: ${e.message}")
        }
    }

    fun setReciter(reciter: Reciter) {
        _playbackState.value = _playbackState.value.copy(currentReciter = reciter)
        val currentSurah = _playbackState.value.currentSurah
        if (currentSurah != null && _playbackState.value.isPlaying) {
            playSurah(currentSurah, reciter)
        }
    }

    fun stop() {
        stopProgressTracking()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // MediaPlayer state might be uninitialized
        }
        mediaPlayer = null
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isLoading = false,
            currentPositionMs = 0,
            currentAudioTitle = null
        )
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            _playbackState.value = _playbackState.value.copy(
                                currentPositionMs = mp.currentPosition,
                                durationMs = mp.duration
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore transient exceptions during state changes
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
    }
}
