package com.demo.cx1.myui.RTSP.RtspClinet;

/**
 *
 *  取到的流的回调
 *
 * Created by tl on 2016/10/4.
 */
public interface FrameCallback {
    void onDecodeFrame(byte[] data);
}
