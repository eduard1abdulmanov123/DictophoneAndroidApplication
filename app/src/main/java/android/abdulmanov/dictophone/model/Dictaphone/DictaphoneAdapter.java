package android.abdulmanov.dictophone.model.Dictaphone;

public interface DictaphoneAdapter {

    void initial();

    void prepareDataSourceConfigured();

    boolean start();

    void stop();

    void resume();

    void pause();

    void reset();

    void release();

    void save(String nameFile);

    boolean isRecording();

    boolean isPause();

    long getSecond();
}
