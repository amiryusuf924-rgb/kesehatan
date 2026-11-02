package id.dietku.app.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import id.dietku.app.MainActivity

object ReminderNotifications {
    private const val CHANNEL_ID = "dietku_reminders"

    fun show(context: Context, type: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val (title, message) = when (type) {
            "meal_start" -> "Jendela makan dimulai" to "Saatnya menikmati menu yang sudah direncanakan."
            "window_close" -> "Jendela makan segera ditutup" to "Tersisa sekitar 30 menit sebelum waktu puasa."
            "fast_start" -> "Waktu puasa dimulai" to "Mulai puasa sesuai jadwal yang kamu pilih."
            "water" -> "Waktunya minum" to "Ambil jeda sebentar untuk minum air putih."
            "exercise" -> "Waktu bergerak" to "Jalan ringan juga termasuk olahraga."
            "weight" -> "Pengingat timbang berat" to "Timbang pada Minggu pagi sebelum makan."
            "meal_plan" -> "Rencanakan menu minggu ini" to "Periksa rencana dan daftar belanjamu."
            else -> "Bersiap untuk tidur" to "Jaga waktu tidur agar tubuh mendapat istirahat cukup."
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Pengingat DietKu", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        val openApp = PendingIntent.getActivity(
            context,
            100,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(type.hashCode(), notification)
    }
}