package android.abdulmanov.dictophone.model.Player;

public interface PlaybackInfoListener {

    void onDurationChanged(int duration);

    void onPositionChanged(int position);

    void onPlaybackCompleted();

    void onPauseMedia();
}