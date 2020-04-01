package com.example.crowdtest;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public class FileLoggingTree extends Timber.DebugTree {

    private static final String LOG_TAG = FileLoggingTree.class.getSimpleName();

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        try {
            String fileNameTimeStamp = new SimpleDateFormat("dd-MM-yyyy",
                    Locale.getDefault()).format(new Date());
            String logTimeStamp = new SimpleDateFormat("E MMM dd yyyy 'at' hh:mm:ss:SSS aaa",
                    Locale.getDefault()).format(new Date());

            // Create file
            File file = getLogFile();

            // If file created or exists save logs
            if (file != null) {
                FileWriter writer = new FileWriter(file, true);
                writer.append("<p style=\"background:lightgray;\"><strong "
                        + "style=\"background:lightblue;\">&nbsp&nbsp")
                        .append(logTimeStamp)
                        .append(" :&nbsp&nbsp</strong><strong>&nbsp&nbsp")
                        .append(tag)
                        .append("</strong> - ")
                        .append(message)
                        .append("</p>");
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while logging into file : " + e);
        }
    }

    @Override
    protected String createStackElementTag(StackTraceElement element) {
        // Add log statements line number to the log
        return super.createStackElementTag(element) + " - " + element.getLineNumber();
    }

    /*  Helper method to create file*/
    @Nullable
    private static File generateFile(@NonNull String path, @NonNull String fileName) {
        return new File(AppController.getInstance().getFilesDir().getAbsolutePath(), fileName);
        /*
        if (isExternalStorageAvailable()) {
            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    BuildConfig.APPLICATION_ID + File.separator + path);

            boolean dirExists = true;

            if (!root.exists()) {
                dirExists = root.mkdirs();
            }

            if (dirExists) {
                file = new File(root, fileName);
            }
        }
        return file;*/
    }

    public static File getLogFile() {
        return new File(AppController.getInstance().getFilesDir().getAbsolutePath(), "LogFile.html");
    }
}