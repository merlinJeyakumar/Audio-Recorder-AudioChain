package cafe.adriel.androidaudiorecorder;

import android.graphics.Color;
import android.media.AudioFormat;
import android.os.Handler;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.omrecorder.AudioSource;


public class Util {
    private static final Handler HANDLER = new Handler();

    private Util() {
    }

    public static void wait(int millis, Runnable callback) {
        HANDLER.postDelayed(callback, millis);
    }

    public static AudioSource getMic(cafe.adriel.androidaudiorecorder.model.AudioSource source,
                                     AudioChannel channel,
                                     AudioSampleRate sampleRate,
                                     int audioBitDepth) {
        return new AudioSource.Smart(
                source.getSource(),
                AudioFormat.ENCODING_PCM_16BIT,
                channel.getChannel(),
                sampleRate.getSampleRate());
    }

    public static boolean isBrightColor(int color) {
        if (android.R.color.transparent == color) {
            return true;
        }
        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};
        int brightness = (int) Math.sqrt(
                rgb[0] * rgb[0] * 0.241 +
                        rgb[1] * rgb[1] * 0.691 +
                        rgb[2] * rgb[2] * 0.068);
        return brightness >= 200;
    }

    public static int getDarkerColor(int color) {
        float factor = 0.8f;
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public static String formatSeconds(long _Seconds) {
        _Seconds = TimeUnit.SECONDS.toMillis(_Seconds);
        int seconds = (int) ((_Seconds / 1000) % 60);
        int minutes = (int) ((_Seconds / (1000 * 60)) % 60);
        int hours = (int) ((_Seconds / (1000 * 60 * 60)) % 24);

        return String.format(
                Locale.US, "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
        );
    }

    public static String formatMilliSeconds_(long millis) {
        //millis = Math.round(millis * 100) / 100;
        int seconds = (int) ((millis / 1000) % 60);
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) ((millis / (1000 * 60 * 60)) % 24);

        return String.format(
                Locale.US, "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
        );
    }

    public static String formatMilliSeconds(Integer milliSeconds) {
        String _Default = "00:00:00";
        if (milliSeconds == null) {
            return _Default;
        }
        String _Time;

        int hours = 0, seconds = 0, minutes = 0;
        try {
            seconds = (int) ((Math.round(Double.parseDouble(milliSeconds.toString()) / 1000)) % 60);
            minutes = (int) ((milliSeconds / (1000 * 60)) % 60);
            hours = (int) ((milliSeconds / (1000 * 60 * 60)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (seconds == 0) {
            return _Default;
        }

        if (hours >= 1) {
            _Time = String.format(
                    Locale.getDefault(), "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
            return _Time;

        } else if (minutes >= 1) {
            _Time = String.format(
                    Locale.getDefault(), "00:%02d:%02d",
                    minutes,
                    seconds
            );
            return _Time;

        } else {
            _Time = String.format(
                    Locale.getDefault(), "00:00:%02d",
                    seconds
            );
            return _Time;
        }
    }

    private static String getTwoDecimalsValue(int value) {
        if (value >= 0 && value <= 9) {
            return "0" + value;
        } else {
            return value + "";
        }
    }

}