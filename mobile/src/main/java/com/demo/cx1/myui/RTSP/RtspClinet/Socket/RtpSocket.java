package com.demo.cx1.myui.RTSP.RtspClinet.Socket;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.demo.cx1.myui.RTSP.RtspClinet.Stream.RtpStream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * RtpSocket is used to set up the rtp socket and receive the data via udp or tcp protocol
 * 1. set up the socket , four different socket : video udp socket, audio udp socket, video tcp socket, audio tcp socket
 * 2. make a thread to get the data form rtp server
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class RtpSocket implements Runnable {
    private final static String tag = "RtpSocket";

    private DatagramSocket mUdpSocket;
    private DatagramPacket mUdpPackets;

    private RtcpSocket mRtcpSocket;
    private Thread mThread;
    private byte[] message = new byte[2048];
    private int port;
    private String ip;
    private RtpStream mRtpStream;
    private int serverPort;
    private long recordTime = 0;

    public RtpSocket(int port, String ip, int serverPort) {
        this.port = port;
        this.ip = ip;
        this.serverPort = serverPort;
        try {
            setupUdpSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupUdpSocket() throws IOException {
        Log.d(tag, "Start to setup the udp socket , the port is:  " + port + "....");
        mUdpSocket = new DatagramSocket(port);
        mUdpPackets = new DatagramPacket(message, message.length);
        mRtcpSocket = new RtcpSocket(port + 1, ip, serverPort + 1);
        mRtcpSocket.start();
    }

    public void startRtpSocket() {
        Log.d(tag, "start to run rtp socket thread");
        mThread = new Thread(this, "RTPSocketThread");
        mThread.start();
    }


    private void startUdpReading() throws IOException {
        long currentTime;
        mUdpSocket.receive(mUdpPackets);
        byte[] buffer = new byte[mUdpPackets.getLength()];
        System.arraycopy(mUdpPackets.getData(), 0, buffer, 0, mUdpPackets.getLength());
        //Use Rtp stream thread to decode the receive data
        mRtpStream.receiveData(buffer, mUdpPackets.getLength());

        //every 30s send a rtcp packet to server
        currentTime = System.currentTimeMillis();
        if (currentTime - 30000 > recordTime) {
            recordTime = currentTime;
            mRtcpSocket.sendReciverReport();
        }
    }

    public void setStream(RtpStream stream) {
        mRtpStream = stream;
    }

    @Override
    public void run() {
        Log.d(tag, "start to get rtp data via socket...");
        try {

            while (!Thread.interrupted())
                startUdpReading();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        mUdpSocket.close();
        mUdpPackets = null;
        if (mRtcpSocket != null) {
            mRtcpSocket.stop();
            mRtcpSocket = null;
        }
        Thread.interrupted();
    }
}
