package com.furkan.fikirasistani

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fikirlerin hangisinin kullanıcıya zaten sorulduğunu/anlatıldığını cihazda tutar.
 * GitHub deposuna hiçbir şey yazılmaz; durum tamamen yereldir.
 */
object YerelDurum {
    private const val TERCIHLER = "fikir_asistani_durum"
    private const val ANAHTAR_ISLENEN = "islenen_dosyalar"
    private const val BEKLEYEN_DOSYA_ADI = "bekleyen_fikirler.txt"

    private fun tercihler(context: Context) =
        context.getSharedPreferences(TERCIHLER, Context.MODE_PRIVATE)

    fun islendiMi(context: Context, dosyaAdi: String): Boolean =
        tercihler(context).getStringSet(ANAHTAR_ISLENEN, emptySet())?.contains(dosyaAdi) == true

    fun islendiIsaretle(context: Context, dosyaAdi: String) {
        val prefs = tercihler(context)
        val mevcut = prefs.getStringSet(ANAHTAR_ISLENEN, emptySet())?.toMutableSet() ?: mutableSetOf()
        mevcut.add(dosyaAdi)
        prefs.edit().putStringSet(ANAHTAR_ISLENEN, mevcut).apply()
    }

    fun beklemedekiFikreEkle(context: Context, fikir: Fikir) {
        val dosya = File(context.getExternalFilesDir(null) ?: context.filesDir, BEKLEYEN_DOSYA_ADI)
        val zaman = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("tr", "TR")).format(Date())
        dosya.appendText("[$zaman] ${fikir.baslik}\n${fikir.ozet}\n\n")
    }
}
