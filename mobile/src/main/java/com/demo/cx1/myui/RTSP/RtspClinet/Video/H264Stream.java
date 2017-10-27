package com.demo.cx1.myui.RTSP.RtspClinet.Video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceView;

import com.demo.cx1.myui.RTSP.RtspClient;
import com.demo.cx1.myui.RTSP.RtspClinet.FrameCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;


/**
 *
 */
public class H264Stream extends VideoStream {
    private final static String tag = "H24Stream";

    private MediaCodec mMeidaCodec;
    private SurfaceView mSurfaceView;
    private ByteBuffer[] inputBuffers;
    private Handler mHandler;
    private LinkedBlockingDeque<byte[]> bufferQueue = new LinkedBlockingDeque<>();
    private int picWidth = 1280;
    private int picHeight = 720;
    private HandlerThread thread;
    private FrameCallback mFrameDispatch;

    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video   视频格式，  video/avc 指的是 H264格式

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    public H264Stream(RtspClient.SDPInfo sp, FrameCallback mFrameDispatch) {
        mSDPinfo = sp;
        this.mFrameDispatch = mFrameDispatch;
        thread = new HandlerThread("H264StreamThread");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    private void configMediaDecoder() {
        if (Build.VERSION.SDK_INT > 15) {
            try {
                mMeidaCodec = MediaCodec.createDecoderByType(MIME_TYPE);//创建解码器
                MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, picWidth, picHeight);//指定解码类型，数据尺寸
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar);
                mMeidaCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(), null, 0);//
//                mMeidaCodec.configure(mediaFormat, null, null, 0);//
                mMeidaCodec.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void startMediaHardwareDecode() {
        mHandler.post(hardwareDecodeThread);
    }

    private Runnable hardwareDecodeThread = new Runnable() {
        @Override
        public void run() {
            int mCount = 0;
            byte[] tmpByte;
            int framType;
            boolean startKeyFrame = false;

            configMediaDecoder();
            while (!Thread.interrupted()) {
                try {
                    tmpByte = bufferQueue.take();
                    framType = tmpByte[4] & 0x1F;
                    if (framType == 5) startKeyFrame = true;
                    if (startKeyFrame || framType == 7 || framType == 8) {
                        ByteBuffer[] inputBuffers = mMeidaCodec.getInputBuffers();
                        int inputBufferIndex = mMeidaCodec.dequeueInputBuffer(0);
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            inputBuffer.clear();
                            inputBuffer.put(tmpByte, 0, tmpByte.length);
                            mMeidaCodec.queueInputBuffer(inputBufferIndex, 0, tmpByte.length, mCount * 1000000, 0);
                            mCount++;
                        }
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        int outputBufferIndex = mMeidaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        byte[] outData = new byte[bufferInfo.size];
                        ByteBuffer[] outputBuffers = mMeidaCodec.getOutputBuffers();
                        while (outputBufferIndex >= 0) {
//                            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//                            outputBuffer.position(bufferInfo.offset);
//                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
//                            outputBuffer.get(outData);
                            mMeidaCodec.releaseOutputBuffer(outputBufferIndex, true);
                            outputBufferIndex = mMeidaCodec.dequeueOutputBuffer(bufferInfo, 0);
//                            mFrameDispatch.onDecodeFrame(outData);

                        }
                    }
                } catch (InterruptedException e) {
                    Log.e(tag, "Wait the buffer come..");
                }
            }
            bufferQueue.clear();
            mMeidaCodec.stop();
            mMeidaCodec.release();
            mMeidaCodec = null;
        }
    };

    public void stop() {
        bufferQueue.clear();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        mHandler.removeCallbacks(hardwareDecodeThread);
        if (mMeidaCodec != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mMeidaCodec.stop();
                mMeidaCodec.release();
            }
            mMeidaCodec = null;
        }
        super.stop();
        thread.quit();
    }


    public void setSurfaceView(SurfaceView s) {
        this.mSurfaceView = s;
        if (Build.VERSION.SDK_INT > 15) {
            startMediaHardwareDecode();
        } else {
            Log.e(tag, "The Platform not support the hardware decode H264!");
        }
    }

    @Override
    protected void decodeH264Stream() {
        try {
            bufferQueue.put(NALUnit);
        } catch (InterruptedException e) {
            Log.e(tag, "The buffer queue is full , wait for the place..");
        }
    }

}
