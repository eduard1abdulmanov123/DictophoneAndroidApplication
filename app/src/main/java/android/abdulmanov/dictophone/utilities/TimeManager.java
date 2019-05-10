package android.abdulmanov.dictophone.utilities;

public class TimeManager {

    public static String getTime(long currentTime) {
        Integer time = Integer.valueOf((int) currentTime);
        int h = (int) (time.longValue() / 3600);
        int m = (int) (time.longValue() - h * 3600) / 60;
        int s = (int) (time.longValue() - h * 3600 - m * 60);
        return (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
    }
}
