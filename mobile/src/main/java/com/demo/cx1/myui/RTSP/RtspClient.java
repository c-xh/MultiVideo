package com.demo.cx1.myui.RTSP;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;

import com.demo.cx1.myui.RTSP.RtspClinet.FrameCallback;
import com.demo.cx1.myui.RTSP.RtspClinet.Socket.RtpSocket;
import com.demo.cx1.myui.RTSP.RtspClinet.Video.H264Stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class RtspClient {

    private final static String tag = "RtspClient";
    private final static String UserAgent = "Rtsp/0.1";
    private final static int STATE_STARTED = 0x00;
    private final static int STATE_STARTING = 0x01;
    private final static int STATE_STOPPING = 0x02;
    private final static int STATE_STOPPED = 0x03;

    private Socket mSocket;
    private BufferedReader mBufferreader;
    private OutputStream mOutputStream;
    private Parameters mParams;
    private Handler mHandler;
    private int CSeq;
    private int mState;
    private String mSession;
    private RtpSocket mRtpSocket;
    private static boolean Describeflag = false; //used to get SDP info
    private static SDPInfo sdpInfo;
    private String authorName, authorPassword, authorBase64;
    private HandlerThread thread;

    private H264Stream mH264Stream;

    private FrameCallback mFrameDispatch;//dispatch类实现了该接口，用于回调

    private SurfaceView surfaceView;

    private class Parameters {
        public String host;
        public String address;
        public int port;
        public int rtpPort;
        public int serverPort;
    }

    public class SDPInfo {
        public boolean audioTrackFlag;
        public boolean videoTrackFlag;
        public String videoTrack;
        public String audioTrack;
        public String SPS;
        public String PPS;
        public int packetizationMode;
    }


    public RtspClient(FrameCallback fcb, String address, String name, String password, int port, SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        mFrameDispatch = fcb;
        String url = address.substring(address.indexOf("//") + 2);
        url = url.substring(0, url.indexOf("/"));
        authorName = name;
        authorPassword = password;
        ClientConfig(url, address, port);
    }


    @TargetApi(Build.VERSION_CODES.FROYO)
    private void ClientConfig(String host, String address, int port) {
        mParams = new Parameters();
        sdpInfo = new SDPInfo();
        mParams.host = host;
        mParams.port = port;
        mParams.address = address.substring(7);
        CSeq = 0;
        mState = STATE_STOPPED;
        mSession = null;
        if (authorName == null && authorPassword == null) {
            authorBase64 = null;
        } else {
            authorBase64 = Base64.encodeToString((authorName + ":" + authorPassword).getBytes(), Base64.DEFAULT);
        }

        final Semaphore signal = new Semaphore(0);
        thread = new HandlerThread("RTSPCilentThread") {
            protected void onLooperPrepared() {
                mHandler = new Handler();
                signal.release();
            }
        };
        thread.start();
        signal.acquireUninterruptibly();
    }

    public void start() {
        mHandler.post(startConnection);
    }

    private Runnable startConnection = new Runnable() {
        @Override
        public void run() {
            if (mState != STATE_STOPPED) return;
            mState = STATE_STARTING;

            Log.d(tag, "Start to connect the server...");

            try {
                tryConnection();
                mHandler.post(sendGetParameter);
            } catch (IOException e) {
                Log.e(tag, e.toString());
                abort();
            }
        }
    };

    private Runnable sendGetParameter = new Runnable() {
        @Override
        public void run() {
            try {
                sendRequestGetParameter();
                mHandler.postDelayed(sendGetParameter, 10 * 1000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void abort() {
        try {
            if (mState == STATE_STARTED) sendRequestTeardown();
        } catch (IOException e) {
        }
        try {
            if (mSocket != null) mSocket.close();
        } catch (IOException e) {
        }
        mState = STATE_STOPPED;
        mHandler.removeCallbacks(startConnection);
        mHandler.removeCallbacks(sendGetParameter);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public void shutdown() {
        if (mState == STATE_STARTED ||
                mState == STATE_STARTING) {
            mHandler.removeCallbacks(startConnection);
            mHandler.removeCallbacks(sendGetParameter);
            try {
                if (mH264Stream != null) {
                    mH264Stream.stop();
                    mH264Stream = null;
                }
                if (mRtpSocket != null) {
                    mRtpSocket.stop();
                    mRtpSocket = null;
                }
                if (mState == STATE_STARTED) sendRequestTeardown();
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }
                mState = STATE_STOPPED;
                thread.quit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void tryConnection() throws IOException {
        mSocket = new Socket(mParams.host, mParams.port);
        mBufferreader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        mOutputStream = mSocket.getOutputStream();
        mState = STATE_STARTING;
        sendRequestOptions();
        sendRequestDescribe();
        sendRequestSetup();
        sendRequestPlay();
    }

    private void sendRequestOptions() throws IOException {
        String request = "OPTIONS rtsp://" + mParams.address + " RTSP/1.0\r\n" + addHeaders();
        Log.d(tag, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes("UTF-8"));
        Response.parseResponse(mBufferreader);
    }

    private void sendRequestDescribe() throws IOException {
        String request = "DESCRIBE rtsp://" + mParams.address + " RTSP/1.0\r\n" + addHeaders();
        Log.d(tag, request.substring(0, request.indexOf("\r\n")));
        Describeflag = true;
        mOutputStream.write(request.getBytes("UTF-8"));
        Response.parseResponse(mBufferreader);
    }

    private void sendRequestSetup() throws IOException {
        Matcher matcher;
        String request;
        request = "SETUP rtsp://" + mParams.address + "/" + sdpInfo.videoTrack + " RTSP/1.0\r\n"
                + "Transport: RTP/AVP/UDP;unicast;client_port=55640-55641" + "\r\n"
                + addHeaders();
        Log.d(tag, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes("UTF-8"));
        Response mResponse = Response.parseResponse(mBufferreader);

        //there has two different session type, one is without timeout , another is with timeout
        matcher = Response.regexSessionWithTimeout.matcher(mResponse.headers.get("session"));
        if (matcher.find()) mSession = matcher.group(1);
        else mSession = mResponse.headers.get("session");
        Log.d(tag, "the session is " + mSession);

        //get the port information and start the RTP socket, ready to receive data
        matcher = Response.regexUDPTransport.matcher(mResponse.headers.get("transport"));
        if (matcher.find()) {
            mParams.rtpPort = Integer.parseInt(matcher.group(1));
            mParams.serverPort = Integer.parseInt(matcher.group(2));

            //prepare for the video decoder
            mH264Stream = new H264Stream(sdpInfo, mFrameDispatch);
            mH264Stream.setSurfaceView(surfaceView);
            mRtpSocket = new RtpSocket(mParams.rtpPort, mParams.host, mParams.serverPort);
            mRtpSocket.startRtpSocket();
            mRtpSocket.setStream(mH264Stream);
        }
    }

    private void sendRequestPlay() throws IOException {
        String request = "PLAY rtsp://" + mParams.address + " RTSP/1.0\r\n"
                + "Range: npt=0.000-\r\n"
                + addHeaders();
        Log.d(tag, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes("UTF-8"));
        Response.parseResponse(mBufferreader);
    }

    private void sendRequestTeardown() throws IOException {
        String request = "TEARDOWN rtsp://" + mParams.address + "/" + sdpInfo.videoTrack + " RTSP/1.0\r\n" + addHeaders();
        Log.d(tag, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes("UTF-8"));
        mState = STATE_STOPPING;
    }

    private void sendRequestGetParameter() throws IOException {
        String request = "GET_PARAMETER rtsp://" + mParams.address + "/" + sdpInfo.videoTrack + " RTSP/1.0\r\n" + addHeaders();
        Log.d(tag, request.substring(0, request.indexOf("\r\n")));
        mOutputStream.write(request.getBytes("UTF-8"));
    }

    private String addHeaders() {
        return "CSeq: " + (++CSeq) + "\r\n"
                + ((authorBase64 == null) ? "" : ("Authorization: Basic " + authorBase64 + "\r\n"))
                + "User-Agent: " + UserAgent + "\r\n"
                + ((mSession == null) ? "" : ("Session: " + mSession + "\r\n"))
                + "\r\n";
    }

    static class Response {

        public static final Pattern regexStatus = Pattern.compile("RTSP/\\d.\\d (\\d+) .+", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexHeader = Pattern.compile("(\\S+): (.+)", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexUDPTransport = Pattern.compile("client_port=(\\d+)-\\d+;server_port=(\\d+)-\\d+", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexSessionWithTimeout = Pattern.compile("(\\S+);timeout=(\\d+)", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexSDPgetTrack1 = Pattern.compile("trackID=(\\d+)", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexSDPgetTrack2 = Pattern.compile("control:(\\S+)", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexSDPmediadescript = Pattern.compile("m=(\\S+) .+", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexSDPpacketizationMode = Pattern.compile("packetization-mode=(\\d);", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexSDPspspps = Pattern.compile("sprop-parameter-sets=(\\S+),(\\S+)", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexSDPlength = Pattern.compile("Content-length: (\\d+)", Pattern.CASE_INSENSITIVE);
        public static final Pattern regexSDPstartFlag = Pattern.compile("v=(\\d)", Pattern.CASE_INSENSITIVE);

        public int state;
        public static HashMap<String, String> headers = new HashMap<>();

        public static Response parseResponse(BufferedReader input) throws IOException {
            Response response = new Response();
            String line;
            Matcher matcher;
            int sdpContentLength = 0;
            if ((line = input.readLine()) == null) throw new IOException("Connection lost");
            matcher = regexStatus.matcher(line);
            if (matcher.find())
                response.state = Integer.parseInt(matcher.group(1));
            else
                while ((line = input.readLine()) != null) {
                    matcher = regexStatus.matcher(line);
                    if (matcher.find()) {
                        response.state = Integer.parseInt(matcher.group(1));
                        break;
                    }
                }
            Log.d(tag, "The response state is: " + response.state);

            int foundMediaType = 0;
            int sdpHaveReadLength = 0;
            boolean sdpStartFlag = false;

            while ((line = input.readLine()) != null) {
                if (line.length() > 3 || Describeflag) {
                    Log.d(tag, "The line is: " + line + "...");
                    matcher = regexHeader.matcher(line);
                    if (matcher.find())
                        headers.put(matcher.group(1).toLowerCase(Locale.US), matcher.group(2)); //$ to $

                    matcher = regexSDPlength.matcher(line);
                    if (matcher.find()) {
                        sdpContentLength = Integer.parseInt(matcher.group(1));
                        sdpHaveReadLength = 0;
                    }
                    //Here is trying to get the SDP information from the describe response
                    if (Describeflag) {
                        matcher = regexSDPmediadescript.matcher(line);
                        if (matcher.find())
                            if (matcher.group(1).equalsIgnoreCase("audio")) {
                                foundMediaType = 1;
                                sdpInfo.audioTrackFlag = true;
                            } else if (matcher.group(1).equalsIgnoreCase("video")) {
                                foundMediaType = 2;
                                sdpInfo.videoTrackFlag = true;
                            }

                        matcher = regexSDPpacketizationMode.matcher(line);
                        if (matcher.find()) {
                            sdpInfo.packetizationMode = Integer.parseInt(matcher.group(1));
                        }

                        matcher = regexSDPspspps.matcher(line);
                        if (matcher.find()) {
                            sdpInfo.SPS = matcher.group(1);
                            sdpInfo.PPS = matcher.group(2);
                        }

                        matcher = regexSDPgetTrack1.matcher(line);
                        if (matcher.find())
                            if (foundMediaType == 1)
                                sdpInfo.audioTrack = "trackID=" + matcher.group(1);
                            else if (foundMediaType == 2)
                                sdpInfo.videoTrack = "trackID=" + matcher.group(1);


                        matcher = regexSDPgetTrack2.matcher(line);
                        if (matcher.find())
                            if (foundMediaType == 1) sdpInfo.audioTrack = matcher.group(1);
                            else if (foundMediaType == 2) sdpInfo.videoTrack = matcher.group(1);


                        matcher = regexSDPstartFlag.matcher(line);
                        if (matcher.find()) sdpStartFlag = true;
                        if (sdpStartFlag) sdpHaveReadLength += line.getBytes().length + 2;
                        if ((sdpContentLength < sdpHaveReadLength + 2) && (sdpContentLength != 0)) {
                            Describeflag = false;
                            break;
                        }
                    }
                } else {
                    break;
                }

            }

            if (line == null) throw new IOException("Connection lost");

            return response;
        }
    }
}
