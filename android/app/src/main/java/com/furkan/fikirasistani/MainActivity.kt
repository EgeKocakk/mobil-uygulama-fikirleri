package com.furkan.fikirasistani

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {

    private lateinit var durumMetni: TextView

    private val izinIsteyici =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            izinleriKontrolEt()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val kok = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        val aciklama = TextView(this).apply {
            text = "Fikir Asistanı, depoya eklenen yeni oyun fikrini sesli olarak sana anlatır.\n\n" +
                "Başlamak için mikrofon ve bildirim iznini ver."
            textSize = 16f
        }
        val izinButonu = Button(this).apply {
            text = "Mikrofon ve bildirim iznini ver"
            setOnClickListener { izinIsteyici.launch(gerekliIzinler()) }
        }
        val simdiKontrolEtButonu = Button(this).apply {
            text = "Şimdi kontrol et"
            setOnClickListener {
                WorkManager.getInstance(this@MainActivity)
                    .enqueue(OneTimeWorkRequestBuilder<FikirKontrolWorker>().build())
                durumMetni.text = "Kontrol ediliyor..."
            }
        }
        durumMetni = TextView(this).apply {
            textSize = 14f
            setPadding(0, 48, 0, 0)
        }
        kok.addView(aciklama)
        kok.addView(izinButonu)
        kok.addView(simdiKontrolEtButonu)
        kok.addView(durumMetni)
        setContentView(kok)

        FikirKontrolWorker.zamanla(this)
        izinleriKontrolEt()

        if (intent?.getBooleanExtra("hemenKontrolEt", false) == true) {
            WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<FikirKontrolWorker>().build())
        }
    }

    override fun onResume() {
        super.onResume()
        izinleriKontrolEt()
    }

    private fun gerekliIzinler(): Array<String> {
        val izinler = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            izinler.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return izinler.toTypedArray()
    }

    private fun izinleriKontrolEt() {
        val tumuVerildi = gerekliIzinler().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        durumMetni.text = if (tumuVerildi) {
            "Hazır. Arka planda her 15 dakikada bir yeni fikir kontrol edilecek."
        } else {
            "İzinler eksik — sesli anlatım çalışmayacak."
        }
    }
}
