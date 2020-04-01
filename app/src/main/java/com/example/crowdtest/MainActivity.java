package com.example.crowdtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import timber.log.Timber;

import android.Manifest;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String filePath;
    public static int REQUEST_CODE_AUDIO = 1001;
    private MainActivity mInstance;
    private TextView audioResult, version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInstance = this;
        audioResult = findViewById(R.id.audioResult);
        version = findViewById(R.id.version);
        Timber.i("MainActivity initialized");
        version.setText(BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
    }

    public void doRecord(View v) {
        Timber.i("Checking Permission");
        TedPermission.with(mInstance)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Timber.i("PermissionGranted");
                        AndroidAudioRecorder audioRecorder = Utility.takeAudioIntent(mInstance, REQUEST_CODE_AUDIO);
                        filePath = audioRecorder.getFilePath();
                        audioRecorder.record();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Timber.i("PermissionDenied");
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
            Timber.i("Audio ResultCode = RequestCodeAudio");
            if (resultCode == RESULT_OK) {
                Timber.i("Audio ResultOK");
                try {
                    Uri uri = Uri.fromFile(new File(filePath));
                    Timber.i("MediaMetaDataRetriever initializing");
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    Timber.i("MediaMetaDataRetriever setDataSource");
                    retriever.setDataSource(mInstance, Uri.fromFile(new File(filePath)));
                    Timber.i("MediaMetaDataRetriever extractMetaData");
                    String mediaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    //Timber.i("MediaMetaDataRetriever setTextAudioResult to UI");
                    //setDisplayDuration(Long.valueOf(mediaDuration));

                    Timber.i("MediaMetaDataRetriever getDuration from MediaPlayer");
                    getDuration(uri);
                } catch (Exception e) {
                    Timber.e(e);
                    showToast("Failed " + e.getMessage());
                    showToast("Failed to generate Duration");
                    e.printStackTrace();
                }
            } else {
                Timber.e("Audio Result not Okay");
                showToast("Request Not okay ");
            }
        } else {
            Timber.e("Invalid request");
            showToast("invalid Request ");
        }
    }

    private void getDuration(Uri mFileURI) throws IOException {
        Timber.i("MediaMetaDataRetriever initializing mediaPlayer");
        MediaPlayer mp = new MediaPlayer();
        Timber.i("MediaMetaDataRetriever settingDatasource");
        mp.setDataSource(mInstance, mFileURI);
        Timber.i("MediaMetaDataRetriever prepareAsync");
        mp.prepareAsync();
        Timber.i("MediaMetaDataRetriever setPrepareAsyncListener");
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Timber.i("MediaMetaDataRetriever mediaPlayerOnPrepared");
                setDisplayDuration(mediaPlayer.getDuration());
                Timber.i("MediaMetaDataRetriever mediaPlayerRelease");
                mediaPlayer.release();
            }
        });
    }

    private void setDisplayDuration(long inMillis) {
        audioResult.setText(Utility.getDurationInMillis(inMillis));
    }

    private void showToast(String toast) {
        Toast.makeText(mInstance, toast, Toast.LENGTH_LONG).show();
    }

    public void doAudioShare(View view) {
        if (filePath == null) {
            showToast("Record something to share");
            return;
        }
        shareNow(new File(filePath));
    }

    public void doLogShare(View view) {
        shareNow(FileLoggingTree.getLogFile());
    }

    public void shareNow(File file) {
        Uri photoURI = FileProvider.getUriForFile(mInstance, mInstance.getApplicationContext().getPackageName() + ".provider", file);

        ShareCompat.IntentBuilder.from(mInstance)
                .setStream(photoURI)
                .setType(URLConnection.guessContentTypeFromName(file.getName()))
                .startChooser();
    }
}
