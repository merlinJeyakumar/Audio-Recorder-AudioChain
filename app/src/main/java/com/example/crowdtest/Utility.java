package com.example.crowdtest;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class Utility {
    private static String TAG = "Utility";

    public static AndroidAudioRecorder takeAudioIntent(Activity activity, int requestCode) {
        File filePath = getAudioFile(activity, 100);
        if (filePath == null) {
            Log.e(TAG, "takeAudioIntent: empty file data");
            return null;
        }

        return AndroidAudioRecorder.with(activity)
                .setFilePath(filePath.getAbsolutePath())
                .setColor(activity.getResources().getColor(R.color.black_transparent))
                .setRequestCode(requestCode)
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.MONO)
                .setBitDepth(16)
                .setSampleRate(AudioSampleRate.HZ_44100) //AudioEncoding_Frequency
                .setKeepDisplayOn(true);
    }

    private static File getAudioFile(Activity activity, int campaignId) {
        File mVideoPath = getCampaignAudioPath(activity, campaignId);
        mVideoPath.getParentFile().mkdirs();
        //Log.i(TAG, "getVideoFile: mVideoPath " + mVideoPath.getAbsolutePath());

        try {
            mVideoPath.createNewFile();
        } catch (IOException e) {
            return null;
        }
        return mVideoPath;
    }

    public static File getCampaignAudioPath(Activity activity, int campaignId) {
        return new File(getCampaignParentPath(activity, campaignId)
                + File.separator + "audio"
                + File.separator + "campaign_" + campaignId + "_" + System.currentTimeMillis() + ".wav");
    }

    public static File getCampaignParentPath(Activity activity, int campaignId) {
        return new File(activity.getApplicationContext().getFilesDir()
                + File.separator + "campaign"
                + File.separator + "campaign_" + campaignId);
    }

    public static String getDurationInMillis(Long milliSeconds) {
        String _Default = "00:00";
        if (milliSeconds == null) {
            return _Default;
        }
        String _Time;

        int hours = 0, seconds = 0, minutes = 0;
        try {
            seconds = (int) ((Math.round(Double.parseDouble(milliSeconds.toString())/1000)) % 60);
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
            return _Time.concat(" Hour.");

        } else if (minutes >= 1) {
            _Time = String.format(
                    Locale.getDefault(), "%02d:%02d",
                    minutes,
                    seconds
            );
            return _Time.concat(" Min.");

        } else {
            _Time = String.format(
                    Locale.getDefault(), "%02d",
                    seconds
            );
            return _Time.concat(" Sec.");
        }
    }
}
