package android.abdulmanov.dictophone.model.Dictaphone;

public interface PlaybackInfoListener {

    void startRecording();

    void stopRecording();

    void pauseRecording();

    void resumeRecording();

    void currentTimeRecording(long time);

    void saveFinish();
}
