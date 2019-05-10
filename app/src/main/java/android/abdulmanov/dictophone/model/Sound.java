package android.abdulmanov.dictophone.model;

import android.abdulmanov.dictophone.utilities.FileManager;
import android.media.MediaMetadataRetriever;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sound {

    private File mFile;
    private String mName;
    private String mDate;
    private String mTime;

    public Sound(File file) {
        mFile = file;
    }

    public interface CallbackSetupSpecifications{
        void finish();
    }
    public void sutupSpecifications(final CallbackSetupSpecifications callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mName == null)
                        installName();
                    if (mDate == null)
                        installDate();
                    if (mTime == null)
                        installTime();
                } catch (RuntimeException runtime){
                    String[] component = mFile.getPath().split("/");
                    String dir = component[component.length-2];
                    FileManager.clearFileInExternalStorage(dir,mFile.getName());
                }
                callback.finish();
            }
        }).start();
    }

    private void installName() {
        String[] component = mFile.getName().split("\\.");
        mName = component[0];
    }

    private void installDate() {
        mDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date(mFile.lastModified()));
    }

    private void installTime() {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mFile.getPath());
        Integer time = Integer.valueOf(mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        int h = (int) (time.longValue() / 3600000);
        int m = (int) (time.longValue() - h * 3600000) / 60000;
        int s = (int) (time.longValue() - h * 3600000 - m * 60000) / 1000;
        mTime = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s
                : s);
    }

    public void delete() {
        mFile.delete();
    }

    public void rename(String newName) {
        String[] component = mFile.getAbsolutePath().split("/");
        String directory = component[component.length - 2];
        FileManager.renameFileInExternalPublicStorage(directory, mFile.getName(), newName);
        mFile = FileManager.getFileWithExternalPublicStorage(directory, newName);
        installDate();
        installName();
    }

    public File getFile() {
        return mFile;
    }

    public String getName() {
        return mName;
    }

    public String getDate() {
        return mDate;
    }

    public String getTime() {
        return mTime;
    }
}
