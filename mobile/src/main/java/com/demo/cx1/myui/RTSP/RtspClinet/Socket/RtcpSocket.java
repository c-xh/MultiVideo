package com.demo.cx1.myui.RTSP.RtspClinet.Socket;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This class is used to send the rtcp packet to the server
 */
public class RtcpSocket {

    private final static String tag = "RtcpSocket";

    private DatagramSocket mSocket;
    private DatagramPacket mPacket;
    private Handler mHandler;
    private byte[] message = new byte[2048];
    private String serverIp;
    private int serverPort;
    private boolean isStoped;
    private HandlerThread thread;

    public RtcpSocket(int port, String serverIp, int serverPort) {
        try {
            this.serverIp = serverIp;
            this.serverPort = serverPort;
            mSocket = new DatagramSocket(port);
            mPacket = new DatagramPacket(message, message.length);
            thread = new HandlerThread("RTCPSocketThread");
            thread.start();
            isStoped = false;
            mHandler = new Handler(thread.getLooper());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                while (!isStoped) {
                    try {
                        mSocket.receive(mPacket);
                    } catch (IOException e) {
                        Log.e(tag, e.toString());
                    }
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public void stop() {
        isStoped = true;
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
        mPacket = null;
        thread.quit();
    }

    public void sendReciverReport() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startSendReport();
            }
        }).start();
    }

    private void startSendReport() {
        byte[] sendData = new byte[2];
        sendData[0] = (byte) Integer.parseInt("10000000", 2);
        sendData[1] = (byte) 201;
        try {
            mPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(serverIp), serverPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
