package ch.hsr.appquest.coincollector;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationUtil {

    private static final String CHANNEL_ID = "CoinChannelId";
    private static final String CHANNEL_NAME = "CoinChannel";
    private static final String CHANNEL_DESCRIPTION = "Channel for sending coin found notifications";

    private Context context;
    private int notificationId = 0;


    NotificationUtil(Context context) { this.context = context; }

    public void sendNotificationToUser(String title, String message, int areaId) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        RegionIdentifier.GetRegionPhotoFile(areaId)))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setContentText(message);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        mNotificationManager.notify(notificationId, mBuilder.build());

    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.coin), attributes);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
