package com.furkan.fikirasistani

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.util.Locale
import java.util.UUID

/**
 * "Furkan, ordamısın?" akışını yürüten ön plan servisi:
 * 15 saniye cevap yoksa bir kez daha sorar, yine yoksa sessiz kalıp
 * fikri bekleyen listesine ekler. "Buradayım" derse fikri anlatır,
 * "15 dakika sonra" gibi bir cevap verirse ErtelemeZamanlayici ile erteler.
 */
class SesliAnlatimService : Service() {

    private var tts: TextToSpeech? = null
    private var tanima: SpeechRecognizer? = null
    private val isleyici = Handler(Looper.getMainLooper())
    private var mevcutFikir: Fikir? = null
    private var zamanAsimiCalismasi: Runnable? = null

    companion object {
        const val EK_DOSYA_ADI = "dosya_adi"
        private const val BILDIRIM_KANALI = "sesli_anlatim"
        private const val BILDIRIM_ID = 1
        private const val ZAMAN_ASIMI_MS = 15_000L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SesliAnlatimService", "onStartCommand çağrıldı, extra dosya_adi=${intent?.getStringExtra(EK_DOSYA_ADI)}")
        bildirimiBaslat()
        val dosyaAdi = intent?.getStringExtra(EK_DOSYA_ADI)
        if (dosyaAdi == null) {
            Log.w("SesliAnlatimService", "dosya_adi extra'sı yok, servis durduruluyor")
            stopSelf()
            return START_NOT_STICKY
        }
        Thread {
            val fikir = FikirDeposu.fikirGetir(dosyaAdi)
            Log.d("SesliAnlatimService", "fikirGetir($dosyaAdi) sonucu: $fikir")
            isleyici.post {
                if (fikir == null) {
                    stopSelf()
                } else {
                    mevcutFikir = fikir
                    ttsHazirla()
                }
            }
        }.start()
        return START_NOT_STICKY
    }

    private fun bildirimiBaslat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val kanal = NotificationChannel(
                BILDIRIM_KANALI, "Sesli Fikir Anlatımı", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(kanal)
        }
        val bildirim = NotificationCompat.Builder(this, BILDIRIM_KANALI)
            .setContentTitle("Fikir Asistanı")
            .setContentText("Furkan'a sesleniyor...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
        startForeground(BILDIRIM_ID, bildirim)
    }

    private fun ttsHazirla() {
        tts = TextToSpeech(this) { durum ->
            if (durum == TextToSpeech.SUCCESS) {
                tts?.language = Locale("tr", "TR")
                soruSor(ilkDeneme = true)
            } else {
                stopSelf()
            }
        }
    }

    private fun soruSor(ilkDeneme: Boolean) {
        konus("Furkan, ordamısın?") { dinlemeyeBasla(ilkDeneme) }
    }

    private fun konus(metin: String, bitince: (() -> Unit)? = null) {
        val id = UUID.randomUUID().toString()
        if (bitince != null) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == id) isleyici.post { bitince() }
                }
                @Deprecated("eski API uyumluluğu için")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == id) isleyici.post { bitince() }
                }
            })
        }
        tts?.speak(metin, TextToSpeech.QUEUE_FLUSH, Bundle(), id)
    }

    private fun dinlemeyeBasla(ilkDeneme: Boolean) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
        tanima?.destroy()
        tanima = SpeechRecognizer.createSpeechRecognizer(this)
        val niyet = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        var sonuclandi = false

        val zamanAsimi = Runnable {
            if (!sonuclandi) {
                sonuclandi = true
                tanima?.stopListening()
                zamanAsimiSonrasi(ilkDeneme)
            }
        }
        zamanAsimiCalismasi = zamanAsimi
        isleyici.postDelayed(zamanAsimi, ZAMAN_ASIMI_MS)

        tanima?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                if (!sonuclandi) {
                    sonuclandi = true
                    isleyici.removeCallbacks(zamanAsimi)
                    zamanAsimiSonrasi(ilkDeneme)
                }
            }

            override fun onResults(results: Bundle?) {
                if (sonuclandi) return
                sonuclandi = true
                isleyici.removeCallbacks(zamanAsimi)
                val metinler = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                cevabiIsle(metinler?.joinToString(" ") ?: "", ilkDeneme)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        tanima?.startListening(niyet)
    }

    private fun zamanAsimiSonrasi(ilkDeneme: Boolean) {
        if (ilkDeneme) soruSor(ilkDeneme = false) else sessizKalVeKaydet()
    }

    private fun cevabiIsle(metinHam: String, ilkDeneme: Boolean) {
        val metin = metinHam.lowercase(Locale("tr", "TR"))
        val ertelemeDakikasi = ertelemeDakikasiBul(metin)
        when {
            "burada" in metin || "buraday" in metin || "burda" in metin || "evet" in metin -> fikriAnlat()
            ertelemeDakikasi != null -> ertele(ertelemeDakikasi)
            ilkDeneme -> soruSor(ilkDeneme = false)
            else -> sessizKalVeKaydet()
        }
    }

    private fun ertelemeDakikasiBul(metin: String): Int? {
        if ("yarım saat" in metin || "otuz dakika" in metin || "30 dakika" in metin) return 30
        if ("on beş dakika" in metin || "onbeş dakika" in metin || "15 dakika" in metin) return 15
        return Regex("""(\d+)\s*dakika""").find(metin)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun fikriAnlat() {
        val fikir = mevcutFikir ?: return
        val metin = "${fikir.baslik}. ${fikir.ozet} Detaylarını GitHub'daki fikir dosyasında bulabilirsin."
        konus(metin) {
            YerelDurum.islendiIsaretle(this, fikir.dosyaAdi)
            stopSelf()
        }
    }

    private fun ertele(dakika: Int) {
        val fikir = mevcutFikir ?: return
        konus("Tamam, $dakika dakika sonra tekrar soracağım.") {
            ErtelemeZamanlayici.zamanla(this, fikir.dosyaAdi, dakika)
            stopSelf()
        }
    }

    private fun sessizKalVeKaydet() {
        val fikir = mevcutFikir ?: return
        YerelDurum.beklemedekiFikreEkle(this, fikir)
        YerelDurum.islendiIsaretle(this, fikir.dosyaAdi)
        stopSelf()
    }

    override fun onDestroy() {
        zamanAsimiCalismasi?.let { isleyici.removeCallbacks(it) }
        tanima?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
