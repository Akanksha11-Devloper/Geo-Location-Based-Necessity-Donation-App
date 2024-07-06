package com.example.necessity.SendNotificationPack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.necessity.MainActivityDonar;
import com.example.necessity.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MyService extends Service {

    PendingIntent pendingIntent;
    Intent intent1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        intent1 = new Intent(this, MainActivityDonar.class);

        pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        FirebaseFirestore.getInstance().collection("notifications").document(FirebaseAuth.getInstance().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value.exists()) {
                    FirebaseFirestore.getInstance().collection("Ngos").document(value.get("id", String.class)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String name = documentSnapshot.get("name", String.class);
                            String email = documentSnapshot.get("email", String.class);
                            String phone = documentSnapshot.get("phone", String.class);

                            FirebaseFirestore.getInstance().collection("notifications").document(FirebaseAuth.getInstance().getUid()).delete();

                            createNotificationChannel(name, email, phone);
                        }
                    });
                }

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "ChannelID2", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }

        Notification notification = new NotificationCompat.Builder(this, "ChannelID2")
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Application is running in background")
                .setSmallIcon(R.drawable.notify)
                .setContentIntent(pendingIntent).build();

        startForeground(99, notification);

        return START_NOT_STICKY;

    }

    private void createNotificationChannel(String name, String email, String phone) {


        String message = "Name: " + name + "\nEmail: " + email + "\nPhone No: " + phone;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ChannelID2")
                .setContentTitle("NGO Notified you")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setSmallIcon(R.drawable.notify)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(11, builder.build());

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }
}
