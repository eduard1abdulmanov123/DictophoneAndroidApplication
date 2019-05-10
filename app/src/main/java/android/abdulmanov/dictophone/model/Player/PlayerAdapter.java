package android.abdulmanov.dictophone.model.Player;

public interface PlayerAdapter {

    void loadMedia(String path);

    void release();

    boolean isPlaying();

    void play();

    void reset();

    void stop();

    void pause();

    void initializeProgressCallback();

    void seekTo(int position);
}
