package com.furkan.fikirasistani

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ErtelemeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dosyaAdi = intent.getStringExtra(ErtelemeZamanlayici.EK_DOSYA_ADI) ?: return
        val servisNiyeti = Intent(context, SesliAnlatimService::class.java).apply {
            putExtra(SesliAnlatimService.EK_DOSYA_ADI, dosyaAdi)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(servisNiyeti)
        } else {
            context.startService(servisNiyeti)
        }
    }
}
