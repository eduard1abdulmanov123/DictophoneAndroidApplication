package android.abdulmanov.dictophone.activities;

import android.abdulmanov.dictophone.R;
import android.abdulmanov.dictophone.model.Player.MediaPlayerHolder;
import android.abdulmanov.dictophone.model.Player.PlaybackInfoListener;
import android.abdulmanov.dictophone.model.Player.PlayerAdapter;
import android.abdulmanov.dictophone.utilities.TimeManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;

public class DialogPlayerActivity extends AppCompatActivity implements PlaybackInfoListener {

    private static final String ARG_FILE_SOUND = "FileSound";
    private PlayerAdapter mPlayerAdapter;
    private SeekBar mSeekBar;
    private ImageButton mPlayStopButton;
    private TextView mNameText;
    private TextView mDurationText;
    private TextView mCurrentTime;
    private boolean mUserIsSeeking;

    public static Intent newIntent(Context packageContext, File file) {
        Intent intent = new Intent(packageContext, DialogPlayerActivity.class);
        intent.putExtra(ARG_FILE_SOUND, file);
        return intent;
    }

    private void initializeUI() {
        mSeekBar = (SeekBar) findViewById(R.id.activity_dialog_player_seek_bar);
        mCurrentTime = (TextView) findViewById(R.id.activity_dialog_player_current_time);
        mDurationText = (TextView) findViewById(R.id.activity_dialog_player_duration);
        mNameText = (TextView) findViewById(R.id.activity_dialog_player_name_file);
        mPlayStopButton = (ImageButton) findViewById(R.id.activity_dialog_player_play_stop_btn);
        mPlayStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayerAdapter.isPlaying()) {
                    mPlayerAdapter.pause();
                    mPlayStopButton.setImageResource(R.drawable.ic_play);
                } else {
                    mPlayerAdapter.play();
                    mPlayStopButton.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    private void initializeSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userSelectionPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    userSelectionPosition = i;
                    mCurrentTime.setText(TimeManager.getTime(i / 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = false;
                mPlayerAdapter.seekTo(userSelectionPosition);
            }
        });
    }

    private void initializeCallbackController() {
        MediaPlayerHolder mediaPlayerHolder = new MediaPlayerHolder(this);
        mediaPlayerHolder.setPlaybackInfoListener(this);
        mPlayerAdapter = mediaPlayerHolder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_player);
        initializeUI();
        initializeSeekBar();
        initializeCallbackController();
        File file = (File) getIntent().getSerializableExtra(ARG_FILE_SOUND);
        mNameText.setText(file.getName());
        mPlayerAdapter.loadMedia(file.getAbsolutePath());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerAdapter.stop();
        mPlayerAdapter.release();
    }

    @Override
    public void onDurationChanged(int duration) {
        mSeekBar.setMax(duration);
        mDurationText.setText(TimeManager.getTime(duration / 1000));
    }

    @Override
    public void onPositionChanged(final int position) {
        if (!mUserIsSeeking) {
            mSeekBar.setProgress(position);
            mCurrentTime.post(new Runnable() {
                @Override
                public void run() {
                    mCurrentTime.setText(TimeManager.getTime(position / 1000));
                }
            });
        }
    }

    @Override
    public void onPlaybackCompleted() {
        mPlayStopButton.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void onPauseMedia() {
        mPlayStopButton.setImageResource(R.drawable.ic_play);
    }
}
