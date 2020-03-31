package cafe.adriel.androidaudiorecorder;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import cafe.adriel.omrecorder.AudioChunk;
import cafe.adriel.omrecorder.OmRecorder;
import cafe.adriel.omrecorder.PullTransport;
import cafe.adriel.omrecorder.Recorder;


public class AudioRecorderActivity extends AppCompatActivity
        implements PullTransport.OnAudioChunkPulledListener, MediaPlayer.OnCompletionListener, Recorder.OnSilenceListener {
    private String TAG = getClass().getSimpleName();

    private String filePath;

    private int color;
    private boolean autoStart;
    private boolean keepDisplayOn;

    private static MediaPlayer player;
    private static Recorder recorder;
    private VisualizerHandler visualizerHandler;

    private Timer timer;
    private MenuItem saveMenuItem;

    private FrameLayout contentLayout;
    private TextView statusView;
    private TextView timerView;
    private ImageView restartView;
    private ImageView recordView;
    private ImageView playView;
    private boolean isCompleted;
    private long stopTimeMillis;
    //private int audioBitDepth; //Audio Encoding BitRate
    private String INITIALIZED_TIME = "INITIALIZED_TIME";
    public static String RECORDING_DURATION = "RECORDING_DURATION";
    private static int audioDuration;
    private String LONG_VALUE = "LONG_VALUE";
    private String FILE_PATH = "FILE_PATH";
    //private RecorderVisualizerView visualizerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aar_activity_audio_recorder);

        filePath = getIntent().getStringExtra(AndroidAudioRecorder.EXTRA_FILE_PATH);
//        source = (AudioSource) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SOURCE);
        //channel = (AudioChannel) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_CHANNEL);
        //sampleRate = (AudioSampleRate) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
        //audioBitDepth = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_BIT_DEPTH, AudioFormat.ENCODING_PCM_16BIT); //TODO: PCM_16_BIT
        color = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_COLOR, Color.BLACK);
        autoStart = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_AUTO_START, false);
        keepDisplayOn = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON, false);

        if (keepDisplayOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(Util.getDarkerColor(color)));
            getSupportActionBar().setHomeAsUpIndicator(
                    ContextCompat.getDrawable(this, R.drawable.aar_ic_clear));
        }

        contentLayout = findViewById(R.id.content);
        statusView = findViewById(R.id.status);
        timerView = findViewById(R.id.timer);
        restartView = findViewById(R.id.restart);
        recordView = findViewById(R.id.record);
        playView = findViewById(R.id.play);
        //visualizerView = findViewById(R.id.visualizer);

        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (autoStart && !isRecording()) {
            toggleRecording(null);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        filePath = savedInstanceState.getString(AndroidAudioRecorder.EXTRA_FILE_PATH);
        //source = (AudioSource) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_SOURCE);
        //channel = (AudioChannel) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_CHANNEL);
        //sampleRate = (AudioSampleRate) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
        //audioBitDepth = savedInstanceState.getInt(AndroidAudioRecorder.EXTRA_BIT_DEPTH);
        color = savedInstanceState.getInt(AndroidAudioRecorder.EXTRA_COLOR);
        autoStart = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_AUTO_START);
        keepDisplayOn = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AndroidAudioRecorder.EXTRA_FILE_PATH, filePath);
        outState.putInt(AndroidAudioRecorder.EXTRA_COLOR, color);
        //outState.putSerializable(AndroidAudioRecorder.EXTRA_SOURCE, source);
        //outState.putSerializable(AndroidAudioRecorder.EXTRA_CHANNEL, channel);
        //outState.putSerializable(AndroidAudioRecorder.EXTRA_SAMPLE_RATE, sampleRate);
        outState.putSerializable(AndroidAudioRecorder.EXTRA_COLOR, color);
        outState.putSerializable(AndroidAudioRecorder.EXTRA_AUTO_START, autoStart);
        outState.putSerializable(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON, keepDisplayOn);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aar_audio_recorder, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.aar_ic_check));
        return true;
    }

    private void selectAudio() {
        stopRecording();
        Intent intent = new Intent();
        intent.putExtra(LONG_VALUE, getIntent().getLongExtra(INITIALIZED_TIME, 0));
        intent.putExtra(FILE_PATH, filePath);
        intent.putExtra(RECORDING_DURATION,audioDuration);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean isClicked;

    public void toggleRecording(View v) {
        if (isCompleted) {
            selectAudio();
            return;
        }
        stopPlaying();
        if (isClicked) {
            return;
        }
        isClicked = true;
        Util.wait(!isPaused() && isRecording() ? 600 : 100, new Runnable() {
            @Override
            public void run() {
                isClicked = false;
                if (isRecording() && !isPaused()) { //When recordStarted & recording
                    pauseRecording();
                } else if (!isRecording() || isPaused()) { //When recordNotStarted & paused
                    resumeRecording();
                }
            }
        });
    }

    public void togglePlaying(View v) {
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isPlaying()) {
                    stopPlaying();
                } else {
                    startPlaying();
                }
            }
        });
    }

    public void restartRecording(View v) {
        isCompleted = false;
        if (isRecording() || recorder != null && (recorder.isPaused())) {
            stopRecording();
        } else if (isPlaying()) {
            stopPlaying();
        }
        if (saveMenuItem != null) {
            saveMenuItem.setVisible(false);
        }
        statusView.setVisibility(View.INVISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.ic_record);
        timerView.setText(Util.formatSeconds(0));
    }

    private void resumeRecording() {
        if (saveMenuItem != null) {
            saveMenuItem.setVisible(false);
        }
        statusView.setText(R.string.aar_recording);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.ic_pause);
        playView.setImageResource(R.drawable.ic_play_with_circle);

        boolean isFresh = false;
        if (recorder == null) {
            isFresh = true;
            timerView.setText(Util.formatSeconds(0));
            recorder = OmRecorder.wav(
                    new PullTransport.Default(Util.getMic(getAudioSource(), getAudioChannel(), getAudioSampleRate(), getAudioBitDepth()), this),
                    new File(filePath));
            /*recorder = OmRecorder.wav(
                    new PullTransport.Noise(Util.getMic(source, channel, sampleRate, audioBitDepth), this),
                    new File(filePath));*/
        }
        if (isFresh) {
            //getIntent().putExtra(INITIALIZED_TIME, Utility.DateTimeUtil.getUTCTime());
            recorder.startRecording();
            //startTimer(true);
        } else {
            //startTimer(false);
            recorder.resumeRecording();
        }
    }

    private void pauseRecording() {
        if (!isFinishing()) {
            if (saveMenuItem != null) {
                saveMenuItem.setVisible(true);
            }
        }
        statusView.setText(R.string.aar_paused);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.VISIBLE);
        playView.setVisibility(View.VISIBLE);
        recordView.setImageResource(R.drawable.ic_record);
        playView.setImageResource(R.drawable.ic_play_with_circle);


        if (recorder != null) {
            recorder.pauseRecording();
        }

        stopTimer();
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                audioDuration = recorder.getRecordedTime(getAudioSampleRate().getSampleRate(), getChannelCount(), getAudioEncoding());
                recorder.stopRecording();
                recordView.setImageResource(R.drawable.ic_done);
            } catch (Exception e) {
                e.printStackTrace();
            }
            recorder = null;
        }

        stopTimer();
    }

    private void startPlaying() {
        try {
            isCompleted = true;
            stopRecording();
            player = new MediaPlayer();
            player.setOnCompletionListener(this);
            player.setDataSource(filePath);
            player.prepare();
            player.start();

            timerView.setText(Util.formatSeconds(0));
            statusView.setText(R.string.aar_playing);
            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.ic_pause_circle);
            startTimer(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        playView.setImageResource(R.drawable.ic_play_with_circle);

        if (player != null) {
            try {
                player.stop();
                player.reset();
            } catch (Exception e) {
            }
        }

        stopTimer();
    }

    private boolean isPlaying() {
        try {
            return player != null && player.isPlaying() && !isRecording();
        } catch (Exception e) {
            return false;
        }
    }

    private void startTimer(boolean isFreshRecording) {
        if (isFreshRecording) {
            stopTimer();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void updateTimer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPlaying()) {
                    timerView.setText(Util.formatMilliSeconds(player.getCurrentPosition()));
                }
            }
        });
    }

    public boolean isPaused() {
        return isRecording() && recorder.isPaused();
    }

    public boolean isRecording() {
        return recorder != null && (recorder.isRecording() || recorder.isPaused());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        //restartRecording(null);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.action_save) {
            selectAudio();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk, AudioRecord audioRecord) {
        float amplitude = isRecording() ? (float) audioChunk.maxAmplitude() : 0f;
//        System.out.println("amplitude " + audioChunk.toBytes().length);

        int audioEncoding_ = getAudioBitDepth() == AudioFormat.ENCODING_PCM_8BIT ? 1 : 2; //BITDEPTH
        if (timerView != null && recorder != null && getAudioSampleRate() != null) {
            timerView.setText(Util.formatMilliSeconds(recorder.getRecordedTime(getAudioSampleRate().getSampleRate(), getChannelCount(), getAudioEncoding())));
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopPlaying();
    }

    @Override
    public void onSilence(long silenceTime) {
    }

    private AudioSource getAudioSource() {
        return (AudioSource) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SOURCE);
    }

    private AudioChannel getAudioChannel(){
        return (AudioChannel) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_CHANNEL);
    }

    private AudioSampleRate getAudioSampleRate(){
        return (AudioSampleRate) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
    }

    private int getAudioBitDepth(){
        return getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_BIT_DEPTH, AudioFormat.ENCODING_PCM_16BIT); //TODO: PCM_16_BIT
    }

    private int getChannelCount(){
        return 1; //MONO
    }

    private int getAudioEncoding() {
        return 2;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        restartRecording(null);
        recorder = null;
        player = null;
        setResult(RESULT_CANCELED);
        System.gc();
    }

}
