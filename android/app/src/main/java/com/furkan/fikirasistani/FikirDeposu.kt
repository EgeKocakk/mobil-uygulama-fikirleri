package com.furkan.fikirasistani

import java.net.HttpURLConnection
import java.net.URL

data class Fikir(
    val tarih: String,
    val dosyaAdi: String,
    val baslik: String,
    val ozet: String
)

/**
 * Depo public olduğu için kimlik doğrulama gerekmeden raw.githubusercontent.com üzerinden okunur.
 */
object FikirDeposu {
    private const val INDEX_URL =
        "https://raw.githubusercontent.com/EgeKocakk/mobil-uygulama-fikirleri/master/ideas/index.md"
    private const val HAM_TABAN =
        "https://raw.githubusercontent.com/EgeKocakk/mobil-uygulama-fikirleri/master/ideas/"

    fun fikirDosyalariniGetir(): List<String> {
        val icerik = urlIcerikGetir(INDEX_URL) ?: return emptyList()
        val satirRegex = Regex("""\[ideas/([^\]]+\.md)]""")
        return icerik.lines().mapNotNull { satirRegex.find(it)?.groupValues?.get(1) }
    }

    fun fikirGetir(dosyaAdi: String): Fikir? {
        val icerik = urlIcerikGetir(HAM_TABAN + dosyaAdi) ?: return null
        val baslik = Regex("""(?m)^#\s+(.+)$""").find(icerik)?.groupValues?.get(1)?.trim() ?: dosyaAdi
        val bolumler = bolumleriAyikla(icerik)
        val ozet = bolumler["Tek Cümlelik Özet"] ?: ""
        val tarih = dosyaAdi.removeSuffix(".md").take(10)
        return Fikir(tarih, dosyaAdi, baslik, ozet)
    }

    private fun bolumleriAyikla(icerik: String): Map<String, String> {
        val sonuc = mutableMapOf<String, String>()
        val parcalar = icerik.split(Regex("""(?m)^##\s+"""))
        for (parca in parcalar.drop(1)) {
            val ilkSatirSonu = parca.indexOf('\n')
            if (ilkSatirSonu == -1) continue
            val baslik = parca.substring(0, ilkSatirSonu).trim()
            val govde = parca.substring(ilkSatirSonu).trim()
            sonuc[baslik] = govde
        }
        return sonuc
    }

    private fun urlIcerikGetir(url: String): String? {
        return try {
            val baglanti = URL(url).openConnection() as HttpURLConnection
            baglanti.connectTimeout = 15_000
            baglanti.readTimeout = 15_000
            baglanti.requestMethod = "GET"
            if (baglanti.responseCode == 200) {
                baglanti.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
