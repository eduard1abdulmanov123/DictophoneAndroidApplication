package android.abdulmanov.dictophone.service.DictaphoneService;

import android.abdulmanov.dictophone.R;
import android.abdulmanov.dictophone.activities.DictaphoneActivity;
import android.abdulmanov.dictophone.model.Dictaphone.DictaphoneHolder;
import android.abdulmanov.dictophone.model.Dictaphone.PlaybackInfoListener;
import android.abdulmanov.dictophone.utilities.TimeManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.RemoteViews;


public class DictaphoneService extends Service implements PlaybackInfoListener {

    private static final String NAME_DIR = "Dictaphone";
    private static final String ACTION_PAUSE = "ActionStop";
    private static final String ACTION_RECORDING = "ActionRecording";
    private static final int NOTIFICATION_ID = 1;
    private RemoteViews mRemoteViews;
    private DictaphoneHolder mDictaphone;
    private Notification.Builder mBuilderNotification;
    private WakeLock mWakeLock;

    public static Intent newIntent(Context packageContext, String action) {
        Intent intent = new Intent(packageContext, DictaphoneService.class);
        if (action != null) {
            intent.setAction(action);
        }
        return intent;
    }

    private void initializeRemoteViews() {
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        mRemoteViews.setOnClickPendingIntent(R.id.custom_notification_view,
                PendingIntent.getActivity(getApplicationContext(),
                        0,
                        DictaphoneActivity.newIntent(getApplicationContext(), null),
                        0
                ));
        mRemoteViews.setOnClickPendingIntent(R.id.custom_notification_clear_btn,
                PendingIntent.getActivity(getApplicationContext(),
                        0,
                        DictaphoneActivity.newIntent(getApplicationContext(),
                                DictaphoneActivity.ACTION_DELETE),
                        0
                ));
        mRemoteViews.setOnClickPendingIntent(R.id.custom_notification_save_btn,
                PendingIntent.getActivity(getApplicationContext(),
                        0,
                        DictaphoneActivity
                                .newIntent(getApplicationContext(), DictaphoneActivity.ACTION_SAVE),
                        0
                ));
    }

    private void initializeBuilderNotification() {
        mBuilderNotification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_micro);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getClass().getCanonicalName());
        mDictaphone = new DictaphoneHolder(getApplicationContext(), NAME_DIR);
        mDictaphone.addPlaybackInfoListener(this);
        initializeRemoteViews();
        initializeBuilderNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_PAUSE)) {
                mDictaphone.pause();
            } else if (intent.getAction().equals(ACTION_RECORDING)) {
                mDictaphone.resume();
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDictaphone;
    }

    @Override
    public void startRecording() {
        mWakeLock.acquire();
        mRemoteViews.setTextViewText(R.id.custom_notification_state_text_view,
                getString(R.string.state_recording));
        mRemoteViews.setImageViewResource(R.id.custom_notification_recording_pause_btn,
                R.drawable.pause_dark);
        mRemoteViews.setOnClickPendingIntent(R.id.custom_notification_recording_pause_btn,
                PendingIntent.getService(getApplicationContext(),
                        0,
                        newIntent(getApplicationContext(), ACTION_PAUSE),
                        0
                ));
        Notification notification = mBuilderNotification.setContent(mRemoteViews).build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void stopRecording() {
        stopForeground(true);
        mWakeLock.release();
    }

    @Override
    public void pauseRecording() {
        mRemoteViews.setTextViewText(R.id.custom_notification_state_text_view,
                getString(R.string.state_pause));
        mRemoteViews.setImageViewResource(R.id.custom_notification_recording_pause_btn,
                R.drawable.micro_dark);
        mRemoteViews.setOnClickPendingIntent(R.id.custom_notification_recording_pause_btn,
                PendingIntent.getService(getApplicationContext(),
                        0,
                        newIntent(getApplicationContext(), ACTION_RECORDING),
                        0
                ));
        Notification notification = mBuilderNotification.setContent(mRemoteViews).build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void resumeRecording() {
        mRemoteViews.setTextViewText(R.id.custom_notification_state_text_view,
                getString(R.string.state_recording));
        mRemoteViews.setImageViewResource(R.id.custom_notification_recording_pause_btn,
                R.drawable.pause_dark);
        mRemoteViews.setOnClickPendingIntent(R.id.custom_notification_recording_pause_btn,
                PendingIntent.getService(getApplicationContext(),
                        0,
                        newIntent(getApplicationContext(), ACTION_PAUSE),
                        0
                ));
        Notification notification = mBuilderNotification.setContent(mRemoteViews).build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void currentTimeRecording(long time) {
        mRemoteViews.setTextViewText(R.id.custom_notification_time, TimeManager.getTime(time));
        Notification notification = mBuilderNotification.setContent(mRemoteViews).build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void saveFinish() {

    }
}

