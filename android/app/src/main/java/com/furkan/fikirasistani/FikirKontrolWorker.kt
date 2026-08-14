package com.furkan.fikirasistani

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Her 15 dakikada bir depoyu kontrol eder; henüz sorulmamış bir fikir bulursa
 * SesliAnlatimService'i başlatır. Android'in izin verdiği en kısa periyodik iş aralığı 15 dakikadır.
 */
class FikirKontrolWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val IS_ADI = "fikir_kontrol_isi"

        fun zamanla(context: Context) {
            val istek = PeriodicWorkRequestBuilder<FikirKontrolWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(IS_ADI, ExistingPeriodicWorkPolicy.KEEP, istek)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val dosyalar = FikirDeposu.fikirDosyalariniGetir()
            Log.d("FikirKontrolWorker", "index.md'den okunan dosyalar: $dosyalar")
            val islenmemis = dosyalar.firstOrNull { !YerelDurum.islendiMi(applicationContext, it) }
            Log.d("FikirKontrolWorker", "işlenmemiş dosya: $islenmemis")
            if (islenmemis == null) return Result.success()

            val niyet = Intent(applicationContext, SesliAnlatimService::class.java).apply {
                putExtra(SesliAnlatimService.EK_DOSYA_ADI, islenmemis)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(niyet)
            } else {
                applicationContext.startService(niyet)
            }
            Log.d("FikirKontrolWorker", "SesliAnlatimService başlatma isteği gönderildi: $islenmemis")
            Result.success()
        } catch (e: Exception) {
            Log.e("FikirKontrolWorker", "doWork hata", e)
            Result.retry()
        }
    }
}
