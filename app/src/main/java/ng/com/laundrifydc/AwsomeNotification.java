package ng.com.laundrifydc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AwsomeNotification extends Service {

    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final int STOP_NOTIFICATION_ID = 23;
    private String NOTIF_CHANNEL_ID = "Persitant123";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if (intent != null) {
            // do your jobs here
            createNotificationChannel();
            startForeground();
            new MidNotification();
        } else {
            notifyStoped("Lowmem");
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i("test", "Service destroyed!");
        notifyStoped("Destroy");
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        notifyStoped("unbind");
        super.unbindService(conn);
    }

    private void notifyStoped(String motive) {
        Log.i("test", "Service destroyed!" + motive);
        Toast.makeText(this, "Service destroyed!" + motive, Toast.LENGTH_SHORT).show();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
                    .setContentTitle("Laundrify stopped running")
                    .setContentText("Click to resume service")
                    .setSmallIcon(R.drawable.ic_local_laundry_service_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
            //Log.i("icon", String.valueOf(R.drawable.ic_fashion));
        } else {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Laundrify stopped running")
                    .setContentText("Click to resume service")
                    .setSmallIcon(R.drawable.ic_local_laundry_service_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.ticker_text))
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .build();
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(STOP_NOTIFICATION_ID, notification);
    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification;

        notification = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
                    .setContentTitle(getText(R.string.notification_title))
                    .setContentText(getText(R.string.notification_message))
                    .setSmallIcon(R.drawable.ic_visibility_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setOngoing(true)
                    .build();
            //Log.i("icon", String.valueOf(R.drawable.ic_fashion));
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel;
            channel = new NotificationChannel(NOTIF_CHANNEL_ID, getString(R.string.channel_name), importance);
            channel.setShowBadge(false);
            channel.setDescription(getString(R.string.channel_description));

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


/*
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // Read properties from result.

        // Return true to stop the notification from displaying.
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    */
}
