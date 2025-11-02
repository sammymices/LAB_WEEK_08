package com.example.lab_week_08

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.*

class MainActivity : AppCompatActivity() {

    // Instance dari WorkManager untuk mengelola semua worker
    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi WorkManager
        workManager = WorkManager.getInstance(this)

        // Buat constraint: hanya jalan kalau ada koneksi internet
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        // === First Worker Request ===
        val firstRequest = OneTimeWorkRequestBuilder<com.example.lab_week_08.worker.FirstWorker>()
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(com.example.lab_week_08.worker.FirstWorker.INPUT_DATA_ID, id))
            .build()

        // === Second Worker Request ===
        val secondRequest = OneTimeWorkRequestBuilder<com.example.lab_week_08.worker.SecondWorker>()
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(com.example.lab_week_08.worker.SecondWorker.INPUT_DATA_ID, id))
            .build()

        // Jalankan worker berurutan: First → Second
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        // Observasi hasil FirstWorker
        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showResult("First process is done")
                }
            }

        // Observasi hasil SecondWorker
        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showResult("Second process is done")
                }
            }
    }

    // Fungsi untuk membentuk input data
    private fun getIdInputData(idKey: String, idValue: String): Data =
        Data.Builder()
            .putString(idKey, idValue)
            .build()

    // Fungsi untuk menampilkan toast hasil kerja
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
