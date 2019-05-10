package android.abdulmanov.dictophone.activities;

import android.abdulmanov.dictophone.R;
import android.abdulmanov.dictophone.model.Dictaphone.DictaphoneAdapter;
import android.abdulmanov.dictophone.model.Dictaphone.DictaphoneHolder;
import android.abdulmanov.dictophone.model.Dictaphone.PlaybackInfoListener;
import android.abdulmanov.dictophone.service.DictaphoneService.DictaphoneService;
import android.abdulmanov.dictophone.utilities.FileManager;
import android.abdulmanov.dictophone.utilities.TimeManager;
import android.abdulmanov.dictophone.views.CustomImageButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import java.util.UUID;

public class DictaphoneActivity extends AppCompatActivity implements PlaybackInfoListener {

    public static final String ACTION_SAVE = "ActionSave";
    public static final String ACTION_DELETE = "ActionDelete";
    private static final String NAME_DIR = "Dictaphone";
    private static final String FORMAT = ".m4a";
    private static final int REQUEST_SAVE = 1;
    private static final int REQUEST_NO_SAVE = 2;
    private DictaphoneAdapter mDictaphone;
    private TextView mStopwatch;
    private TextView mStateDictaphone;
    private CustomImageButton mRecordingStopButton;
    private CustomImageButton mListDeleteButton;
    private CustomImageButton mSaveButton;
    private String mDefaultNameFile;
    private ProgressDialog mProgressDialog;
    private boolean mBound;

    Handler handlerHideProgress = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mProgressDialog.hide();
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DictaphoneHolder dictaphoneHolder = (DictaphoneHolder) iBinder;
            dictaphoneHolder.addPlaybackInfoListener(DictaphoneActivity.this);
            mDictaphone = (DictaphoneAdapter) dictaphoneHolder;
            updateUI();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    public static Intent newIntent(Context packageContext, String action) {
        Intent intent = new Intent(packageContext, DictaphoneActivity.class);
        if (action != null) {
            intent.setAction(action);
        }
        return intent;
    }


    private void initializeUI() {

        mRecordingStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    if (!mDictaphone.isRecording() && !mDictaphone.isPause()) {
                        if (!mDictaphone.start()) {
                            Toast.makeText(DictaphoneActivity.this, R.string.error_start,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else if (mDictaphone.isPause()) {
                        mDictaphone.resume();
                    } else if (!mDictaphone.isPause()) {
                        mDictaphone.pause();
                    }
                }
            }
        });

        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mDictaphone.pause();
                mDefaultNameFile = getString(R.string.default_name_record) + UUID
                        .randomUUID().toString().split("-")[0];
                Intent intent = DialogEditTextActivity
                        .newIntent(getApplicationContext(), getString(R.string.name_record),
                                NAME_DIR,
                                mDefaultNameFile,
                                FORMAT);
                startActivityForResult(intent, REQUEST_SAVE);
            }
        });

        mListDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDictaphone.isRecording() && mBound) {
                    mDictaphone.pause();
                    Intent intent = DialogCancelActivity
                            .newIntent(getApplicationContext(), getString(R.string.no_save_file),
                                    getString(R.string.no_save_button));
                    startActivityForResult(intent, REQUEST_NO_SAVE);
                } else {
                    Intent intent = ListSoundActivity
                            .newIntent(getApplicationContext(), NAME_DIR, FORMAT);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictaphone);
        mStopwatch = (TextView) findViewById(R.id.activity_dictaphone_stop_watch);
        mStateDictaphone = (TextView) findViewById(R.id.activity_dictaphone_state_dictaphone);
        mRecordingStopButton = (CustomImageButton) findViewById(
                R.id.activity_dictaphone_recording_stop__btn);
        mSaveButton = (CustomImageButton) findViewById(R.id.activity_dictaphone_save_btn);
        mListDeleteButton = (CustomImageButton) findViewById(
                R.id.activity_dictaphone_list_delete__btn);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.saving));
        mProgressDialog.setCancelable(false);
        initializeUI();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_DELETE)) {
                mListDeleteButton.post(new Runnable() {
                    @Override
                    public void run() {
                        while (!mBound)
                            ;
                        mListDeleteButton.performClick();
                    }
                });
            } else if (intent.getAction().equals(ACTION_SAVE)) {
                mSaveButton.post(new Runnable() {
                    @Override
                    public void run() {
                        while (!mBound)
                            ;
                        mSaveButton.performClick();
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = DictaphoneService.newIntent(getApplicationContext(), null);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
        if (!mDictaphone.isRecording()) {
            ((DictaphoneHolder) mDictaphone).removePlaybackInfoListener(this);
            Intent intent = DictaphoneService.newIntent(getApplicationContext(), null);
            stopService(intent);
        }
        mProgressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if (requestCode == REQUEST_SAVE) {
            if (resultCode == Activity.RESULT_OK) {
                mDictaphone.stop();
                mProgressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mDictaphone.save(DialogEditTextActivity.getResult(data));
                    }
                }).start();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mDictaphone.resume();
            }
        } else if (requestCode == REQUEST_NO_SAVE) {
            if (resultCode == Activity.RESULT_OK) {
                mDictaphone.stop();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mDictaphone.resume();
            }
        }
    }

    private void updateUI() {
        if (mDictaphone.isRecording()) {
            mSaveButton.setEnabled(true);
            mListDeleteButton.setImageResource(R.drawable.ic_clear);
        } else {
            mSaveButton.setEnabled(false);
            mListDeleteButton.setImageResource(R.drawable.ic_list);
            mStateDictaphone.setText(R.string.start_recording);
        }

        if (mDictaphone.isRecording() && !mDictaphone.isPause()) {
            mRecordingStopButton.setImageResource(R.drawable.ic_pause);
            mStateDictaphone.setText(R.string.state_recording);
        } else if (mDictaphone.isRecording() && mDictaphone.isPause()) {
            mRecordingStopButton.setImageResource(R.drawable.ic_micro);
            mStateDictaphone.setText(R.string.state_pause);
        } else if (!mDictaphone.isRecording()) {
            mRecordingStopButton.setImageResource(R.drawable.ic_micro);
        }
        mStopwatch.setText(TimeManager.getTime(mDictaphone.getSecond()));
    }

    @Override
    public void startRecording() {
        updateUI();
    }

    @Override
    public void stopRecording() {
        updateUI();
    }

    @Override
    public void pauseRecording() {
        updateUI();
    }

    @Override
    public void resumeRecording() {
        updateUI();
    }

    @Override
    public void currentTimeRecording(final long time) {
        mStopwatch.post(new Runnable() {
            @Override
            public void run() {
                if (mDictaphone.isRecording()) {
                    mStopwatch.setText(TimeManager.getTime(time));
                }
            }
        });
    }

    @Override
    public void saveFinish() {
        handlerHideProgress.sendEmptyMessage(0);
    }
}

