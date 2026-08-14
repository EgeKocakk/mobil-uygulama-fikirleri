package com.furkan.fikirasistani

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object ErtelemeZamanlayici {
    const val EK_DOSYA_ADI = "dosya_adi"

    fun zamanla(context: Context, dosyaAdi: String, dakika: Int) {
        val alarmYoneticisi = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val niyet = Intent(context, ErtelemeReceiver::class.java).apply {
            putExtra(EK_DOSYA_ADI, dosyaAdi)
        }
        val bekleyenNiyet = PendingIntent.getBroadcast(
            context,
            dosyaAdi.hashCode(),
            niyet,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val tetiklemeZamani = System.currentTimeMillis() + dakika * 60_000L
        // Kesin saniye hassasiyeti gerekmiyor; ekstra "tam alarm" izni istemeden çalışır.
        alarmYoneticisi.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tetiklemeZamani, bekleyenNiyet)
    }
}
