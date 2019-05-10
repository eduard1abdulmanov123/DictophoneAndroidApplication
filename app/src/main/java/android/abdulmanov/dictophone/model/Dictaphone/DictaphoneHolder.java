package android.abdulmanov.dictophone.model.Dictaphone;

import android.abdulmanov.dictophone.utilities.FileManager;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Binder;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DictaphoneHolder extends Binder implements DictaphoneAdapter {

    private static final long PLAYBACK_POSITION_REFRESH_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1);
    private static final String FILE_NAME_FOR_BUFF = "FILE_NAME_FOR_BUFF";
    private static final String FORMAT = ".m4a";
    private String mDirName;
    private MediaRecorder mMediaRecorder;
    private List<PlaybackInfoListener> mPlaybackInfoListener;
    private ScheduledExecutorService mUpdateGUI;
    private Context mContext;
    private List<File> mFilesBuffer;
    private boolean mRecording;
    private boolean mPause;
    private long second;


    public DictaphoneHolder(Context context, String dirName) {
        mContext = context.getApplicationContext();
        mDirName = dirName;
        mFilesBuffer = new ArrayList<>();
        mPlaybackInfoListener = new ArrayList<>();
    }

    public void addPlaybackInfoListener(
            PlaybackInfoListener playbackInfoListener) {
        mPlaybackInfoListener.add(playbackInfoListener);
    }

    public void removePlaybackInfoListener(PlaybackInfoListener playbackInfoListener) {
        mPlaybackInfoListener.remove(playbackInfoListener);
    }

    private void stopUpdatingCallbackWithPosition() {
        if (mUpdateGUI != null) {
            mUpdateGUI.shutdownNow();
            mUpdateGUI = null;
            second = 0;
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
                                       }, PLAYBACK_POSITION_REFRESH_INTERVAL_MS, PLAYBACK_POSITION_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
    }

    private void updateProgressCallbackTask() {
        if (mMediaRecorder != null && mRecording && !mPause) {
            second++;
            for (PlaybackInfoListener listener : mPlaybackInfoListener) {
                listener.currentTimeRecording(second);
            }
        }
    }

    @Override
    public void initial() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
            mFilesBuffer.clear();
            FileManager.clearInternalStorage(mContext);
        }
    }

    @Override
    public void prepareDataSourceConfigured() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mFilesBuffer.add(FileManager.createFileInternalStorage(mContext,
                    FILE_NAME_FOR_BUFF + mFilesBuffer.size() + FORMAT));
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioChannels(1);
            mMediaRecorder.setAudioEncodingBitRate(96000);
            mMediaRecorder.setAudioSamplingRate(44100);
            mMediaRecorder.setOutputFile(mFilesBuffer.get(mFilesBuffer.size() - 1).getPath());
            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean start() {
        if (!mRecording) {
            initial();
            prepareDataSourceConfigured();
            try {
                mMediaRecorder.start();
            } catch (RuntimeException error) {
                reset();
                release();
                return false;
            }
            mPause = false;
            mRecording = true;
            for (PlaybackInfoListener listener : mPlaybackInfoListener) {
                listener.startRecording();
            }
            startUpdatingCallbackPosition();
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        if (mMediaRecorder != null && mRecording) {
            try {
                if (!mPause) {
                    mMediaRecorder.stop();
                }
            } catch (RuntimeException re) {
                mFilesBuffer.get(mFilesBuffer.size() - 1).delete();
                mFilesBuffer.remove(mFilesBuffer.size() - 1);
            } finally {
                mRecording = false;
                mPause = false;
                release();
                stopUpdatingCallbackWithPosition();
                for (PlaybackInfoListener listener : mPlaybackInfoListener) {
                    listener.stopRecording();
                }
            }
        }
    }

    @Override
    public void resume() {
        if (mMediaRecorder != null && mPause) {
            mPause = false;
            prepareDataSourceConfigured();
            mMediaRecorder.start();
            for (PlaybackInfoListener listener : mPlaybackInfoListener) {
                listener.resumeRecording();
            }
        }
    }

    @Override
    public void pause() {
        if (mMediaRecorder != null && !mPause) {
            mPause = true;
            for (PlaybackInfoListener listener : mPlaybackInfoListener) {
                listener.pauseRecording();
            }
            try {
                mMediaRecorder.stop();
            } catch (RuntimeException re) {
                mFilesBuffer.get(mFilesBuffer.size() - 1).delete();
                mFilesBuffer.remove(mFilesBuffer.size() - 1);
            } finally {
                reset();
            }
        }
    }

    @Override
    public void reset() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
        }
    }

    @Override
    public void release() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    public void save(String nameFile) {
        try {
            File fileMusic = FileManager
                    .createFileExternalStoragePublic(mDirName, nameFile + FORMAT);
            List<File> list = new ArrayList<>();
            for (File file : mFilesBuffer) {
                list.add(file);
            }
            if (list.size() != 0) {
                Movie resultMovie = new Movie();
                List<Movie> inMovies = new ArrayList<>();
                for (File file : list)
                    inMovies.add(MovieCreator.build(file.getPath()));
                List<Track> audioTracks = new LinkedList<Track>();
                for (Movie m : inMovies) {
                    for (Track track : m.getTracks()) {
                        if (track.getHandler().equals("soun")) {
                            audioTracks.add(track);
                        }
                    }
                }
                if (!audioTracks.isEmpty())
                    resultMovie.addTrack(
                            new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));

                Container container = new DefaultMp4Builder().build(resultMovie);
                FileChannel fc = new RandomAccessFile(fileMusic, "rw").getChannel();
                container.writeContainer(fc);
                fc.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        for (PlaybackInfoListener listener : mPlaybackInfoListener) {
            listener.saveFinish();
        }
    }

    @Override
    public boolean isRecording() {
        return mRecording;
    }

    @Override
    public boolean isPause() {
        return mPause;
    }

    @Override
    public long getSecond() {
        return second;
    }
}
