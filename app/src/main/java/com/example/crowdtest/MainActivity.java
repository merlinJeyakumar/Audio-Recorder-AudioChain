package com.example.crowdtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.Util;

import android.Manifest;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String filePath;
    public static int REQUEST_CODE_AUDIO = 1001;
    private MainActivity mInstance;
    private TextView audioResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInstance = this;
        audioResult = findViewById(R.id.audioResult);
    }

    public void doRecord(View v) {
        TedPermission.with(mInstance)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        AndroidAudioRecorder audioRecorder = Utility.takeAudioIntent(mInstance, REQUEST_CODE_AUDIO);
                        filePath = audioRecorder.getFilePath();
                        audioRecorder.record();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        showToast("Require Audio Permission");
                    }
                })
                .setDeniedMessage("Require Permission")
                .setPermissions(Manifest.permission.RECORD_AUDIO)
                .check();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AUDIO) {
            if (resultCode == RESULT_OK) {
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(mInstance, Uri.fromFile(new File(filePath)));
                    String mediaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    audioResult.setText(Utility.getDurationInMillis(Long.valueOf(mediaDuration)));
                } catch (Exception e) {
                    showToast("Failed " + e.getMessage());
                    showToast("Failed to generate Duration");
                    e.printStackTrace();
                }
            } else {
                showToast("Request Not okay ");
            }
        } else {
            showToast("invalid Request ");
        }
    }

    private void showToast(String toast) {
        Toast.makeText(mInstance, toast, Toast.LENGTH_LONG).show();
    }
}
