package android.abdulmanov.dictophone.utilities;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.IOException;

public class FileManager {

    public static File[] getFilesWithExternalPublicStorage(String directory) {
        if (isExternalStorageReadable()) {
            createDirExternalStoragePublic(directory);
            File sd = Environment.getExternalStoragePublicDirectory(directory);
            File[] files = sd.listFiles();
            return files;
        }
        return null;
    }

    public static File getFileWithExternalPublicStorage(String directory, String name) {
        if (isExternalStorageReadable()) {
            File sd = Environment.getExternalStoragePublicDirectory(directory);
            File file = new File(sd, name);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static boolean fileExistsInExternalPublicStorage(String directory, String name) {
        File sd = Environment.getExternalStoragePublicDirectory(directory);
        return new File(sd, name).exists();
    }

    public static void renameFileInExternalPublicStorage(String directory, String oldName,
            String newName) {
        if (isExternalStorageWritable()) {
            File sd = Environment.getExternalStoragePublicDirectory(directory);
            File oldFile = new File(sd, oldName);
            File newFile = new File(sd, newName);
            oldFile.renameTo(newFile);
        }
    }

    public static File createFileExternalStoragePublic(String dirName, String nameFile) {
        if (isExternalStorageWritable()) {
            File sd = Environment.getExternalStoragePublicDirectory(dirName);
            File newFile = new File(sd, nameFile);
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return newFile;
        }
        return null;
    }

    public static void clearFileInExternalStorage(String dirName, String nameFile) {
        if (isExternalStorageWritable()) {
            File sd = Environment.getExternalStoragePublicDirectory(dirName);
            File file = new File(sd, nameFile);
            if (file.exists())
                file.delete();
        }
    }

    public static boolean createDirExternalStoragePublic(String dirName) {
        if (isExternalStorageWritable()) {
            File file = Environment.getExternalStoragePublicDirectory(dirName);
            if (file.mkdirs())
                return true;
            return false;
        }
        return false;
    }


    public static File createFileInternalStorage(Context context, String nameFile) {
        File newFile = new File(context.getFilesDir(), nameFile);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return newFile;
    }

    public static void clearInternalStorage(Context context) {
        for (File file : context.getFilesDir().listFiles()) {
            file.delete();
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY
                .equals(state)) {
            return true;
        }
        return false;
    }
}
