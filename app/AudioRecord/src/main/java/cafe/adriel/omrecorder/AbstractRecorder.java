package cafe.adriel.omrecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Kailash Dabhi on 22-08-2016.
 * You can contact us at kailash09dabhi@gmail.com OR on skype(kailash.09)
 * Copyright (c) 2016 Kingbull Technology. All rights reserved.
 */
abstract class AbstractRecorder implements Recorder {
    protected final PullTransport pullTransport;
    protected final File file;
    private OutputStream outputStream;
    private boolean isPaused;

    protected AbstractRecorder(PullTransport pullTransport, File file) {
        this.pullTransport = pullTransport;
        this.file = file;
        this.outputStream = outputStream(file);
    }

    @Override
    public void startRecording() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pullTransport.start(outputStream);
                } catch (IOException e) {
                    new RuntimeException(e);
                }
            }
        }).start();
    }

    private OutputStream outputStream(File file) {
        if (file == null) throw new RuntimeException("file is null !");
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("could not build OutputStream from" +
                    " this file" + file.getName(), e);
        }
        return outputStream;
    }

    @Override
    public void stopRecording() {
        isPaused = false;
        pullTransport.stop();
        try {
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseRecording() {
        isPaused = true;
        pullTransport.source().isEnableToBePulled(false);
    }

    @Override
    public void resumeRecording() {
        isPaused = false;
        pullTransport.source().isEnableToBePulled(true);
        startRecording();
    }

    @Override
    public boolean isRecording() {
        return pullTransport.source().isEnableToBePulled();
    }

    @Override
    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public int getRecordedTime(int sampleRate, int channelCount, int audioEncoding) {
         /*sampleRate = pullTransport.pullableSource().preparedToBePulled().getSampleRate();
         channelCount = pullTransport.pullableSource().preparedToBePulled().getChannelCount();
         audioEncoding = pullTransport.pullableSource().config().audioEncoding();*/
        //long a_ = Math.round(file.length() / ((sampleRate * channelCount * audioEncoding)/1000));
        return (int) (file.length() / ((sampleRate * channelCount * audioEncoding) / 1000));
    }
}
