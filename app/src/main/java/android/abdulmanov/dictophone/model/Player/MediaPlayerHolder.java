package android.abdulmanov.dictophone.model.Player;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.PowerManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaPlayerHolder implements PlayerAdapter, OnAudioFocusChangeListener {

    private static final long PLAYBACK_POSITION_REFRESH_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1);
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mUpdateGUI;
    private String mPath;
    private Context mContext;

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                pause();
                if (mPlaybackInfoListener != null) {
                    mPlaybackInfoListener.onPauseMedia();
                }
            }
        }
    };

    public MediaPlayerHolder(Context context) {
        mContext = context.getApplicationContext();
    }

    public void setPlaybackInfoListener(
            PlaybackInfoListener playbackInfoListener) {
        mPlaybackInfoListener = playbackInfoListener;
    }

    private void initializeMediaPlayer() {
        if (mMediaPlayer == null) {
            IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            mContext.registerReceiver(mNoisyReceiver, filter);
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (mPlaybackInfoListener != null) {
                        mPlaybackInfoListener.onPlaybackCompleted();
                        mPlaybackInfoListener.onPositionChanged(mediaPlayer.getCurrentPosition());
                    }
                    stopUpdatingCallbackWithPosition(true);
                    mAudioManager.abandonAudioFocus(MediaPlayerHolder.this);
                }
            });
        }
    }

    private void stopUpdatingCallbackWithPosition(boolean resetUIPlaybackPosition) {
        if (mUpdateGUI != null) {
            mUpdateGUI.shutdownNow();
            mUpdateGUI = null;
            if (resetUIPlaybackPosition && mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(0);
            }
        }
    }

    private void startUpdatingCallbackPosition() {
        if (mUpdateGUI == null) {
            mUpdateGUI = Executors.newSingleThreadScheduledExecutor();
        }
        mUpdateGUI.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateProgressCallbackTask();
            }
        }, 0, PLAYBACK_POSITION_REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void updateProgressCallbackTask() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(currentPosition);
            }
        }
    }

    @Override
    public void loadMedia(String path) {
        mPath = path;
        initializeMediaPlayer();
        try {
            mMediaPlayer.setDataSource(mPath);
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeProgressCallback();
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mMediaPlayer.start();
                startUpdatingCallbackPosition();
            }
        }
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            loadMedia(mPath);
            stopUpdatingCallbackWithPosition(true);
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mAudioManager.abandonAudioFocus(MediaPlayerHolder.this);
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void initializeProgressCallback() {
        final int duration = mMediaPlayer.getDuration();
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onDurationChanged(duration);
            mPlaybackInfoListener.onPositionChanged(0);
        }
    }

    @Override
    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            pause();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPauseMedia();
            }
        }
    }
}