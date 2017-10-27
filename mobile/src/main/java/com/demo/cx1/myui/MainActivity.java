package com.demo.cx1.myui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private VideoView mainvidew21 ;
    private VideoView mainvidew22 ;
    private VideoView mainvidew23 ;
    private VideoView mainvidew24 ;
//    private VideoView mainvidew25 ;
//    private VideoView mainvidew26 ;
    private Button playvideobutton1;
    private Button playvideobutton2;
    private Button playvideobutton3;
    private Button playvideobutton4;
//    private Button mbutton25;
//    private Button mbutton26;
    private Button playallmbutton;


    private Button Selectfilebutton1;
    private Button Selectfilebutton2;
    private Button Selectfilebutton3;
    private Button Selectfilebutton4;

    private TextView FileTextView1;
    private TextView FileTextView2;
    private TextView FileTextView3;
    private TextView FileTextView4;

    File videofile =new File("storage/emulated/0/Download/test.mp4");

    int width;     // 屏幕宽度（像素）
    int height;   // 屏幕高度（像素）
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        setContentView(R.layout.activity_main);
        FileTextView1 = (TextView)findViewById(R.id.textView21);
        FileTextView2 = (TextView)findViewById(R.id.textView22);
        FileTextView3 = (TextView)findViewById(R.id.textView23);
        FileTextView4 = (TextView)findViewById(R.id.textView24);

        mainvidew21 = (VideoView)findViewById(R.id.videoView21);
        mainvidew22 = (VideoView)findViewById(R.id.videoView22);
        mainvidew23 = (VideoView)findViewById(R.id.videoView23);
        mainvidew24 = (VideoView)findViewById(R.id.videoView24);


        Selectfilebutton1 = (Button)findViewById(R.id.button21);
        Selectfilebutton2 = (Button)findViewById(R.id.button22);
        Selectfilebutton3 = (Button)findViewById(R.id.button23);
        Selectfilebutton4 = (Button)findViewById(R.id.button24);


        playvideobutton1 = (Button)findViewById(R.id.button11);
        playvideobutton2 = (Button)findViewById(R.id.button12);
        playvideobutton3 = (Button)findViewById(R.id.button13);
        playvideobutton4 = (Button)findViewById(R.id.button14);

        playallmbutton = (Button)findViewById(R.id.playallbutton2);

        width = metric.widthPixels;     // 屏幕宽度（像素）
        height = metric.heightPixels;   // 屏幕高度（像素）

        for (int i=1;i<=4;i++){
            setFileTextView(videofile.getPath(),i);
        }
        playallmbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=1;i<=4;i++){
                    PlayVideo(i);
                }
            }
        });
        playvideobutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideo(1);
            }
        });
        playvideobutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideo(2);
            }
        });
        playvideobutton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideo(3);
            }
        });
        playvideobutton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideo(4);
            }
        });


        Selectfilebutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File_Dialog mFile_Dialog = new File_Dialog(MainActivity.this, width, height ,1);
                mFile_Dialog.setFile_DialogListener(new File_Dialog.File_Dialogu_Listener() {
                    @Override
                    public void File_Dialogu__OnClick(String file, int Dialogid) {
                        setFileTextView( file,  Dialogid);
                    }
                });
            }
        });
        Selectfilebutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File_Dialog mFile_Dialog = new File_Dialog(MainActivity.this, width, height ,2);
                mFile_Dialog.setFile_DialogListener(new File_Dialog.File_Dialogu_Listener() {
                    @Override
                    public void File_Dialogu__OnClick(String file, int Dialogid) {
                        setFileTextView( file,  Dialogid);
                    }
                });
            }
        });
        Selectfilebutton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File_Dialog mFile_Dialog = new File_Dialog(MainActivity.this, width, height ,3);
                mFile_Dialog.setFile_DialogListener(new File_Dialog.File_Dialogu_Listener() {
                    @Override
                    public void File_Dialogu__OnClick(String file, int Dialogid) {
                        setFileTextView( file,  Dialogid);
                    }
                });
            }
        });
        Selectfilebutton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File_Dialog mFile_Dialog = new File_Dialog(MainActivity.this, width, height ,4);
                mFile_Dialog.setFile_DialogListener(new File_Dialog.File_Dialogu_Listener() {
                    @Override
                    public void File_Dialogu__OnClick(String file, int Dialogid) {
                        setFileTextView( file,  Dialogid);
                    }
                });
            }
        });

    }

    private void setFileTextView(String file, int i ){
        if (file == "" || file == null)
            return;
        switch (i){
            case 1:FileTextView1.setText(file); break;
            case 2:FileTextView2.setText(file); break;
            case 3:FileTextView3.setText(file); break;
            case 4:FileTextView4.setText(file); break;
        }
    }
    private void PlayVideo( int i ){

        switch (i) {
            case 1:
                if (isVideoFile(i)) {
                    mainvidew21.setVideoPath(getFile(i).getPath());
                    MediaController mc = new MediaController(MainActivity.this);//Video是我类名，是你当前的类
                    mainvidew21.setMediaController(mc);//设置VedioView与MediaController相关联
                    mainvidew21.start();
                }
                break;
            case 2:
                if (isVideoFile(i)) {
                    mainvidew22.setVideoPath(getFile(i).getPath());
                    MediaController mc = new MediaController(MainActivity.this);//Video是我类名，是你当前的类
                    mainvidew22.setMediaController(mc);//设置VedioView与MediaController相关联
                    mainvidew22.start();
                }
                break;
            case 3:
                if (isVideoFile(i)) {
                    mainvidew23.setVideoPath(getFile(i).getPath());
                    MediaController mc = new MediaController(MainActivity.this);//Video是我类名，是你当前的类
                    mainvidew23.setMediaController(mc);//设置VedioView与MediaController相关联
                    mainvidew23.start();
                }
                break;
            case 4:
                if (isVideoFile(i)) {
                    MediaController mc = new MediaController(MainActivity.this);//Video是我类名，是你当前的类
                    mainvidew24.setMediaController(mc);//设置VedioView与MediaController相关联
                    mainvidew24.setVideoPath(getFile(i).getPath());
                    mainvidew24.start();
                }
                break;
        }
    }


    File mfile1;// =new File(FileTextView1.toString());
    File mfile2;// =new File(FileTextView1.toString());
    File mfile3;// =new File(FileTextView1.toString());
    File mfile4;// =new File(FileTextView1.toString());
    private boolean isVideoFile(int i) {
        switch (i) {
            case 1:
                mfile1 = new File(FileTextView1.getText().toString());
                if (mfile1.exists()) {
                    return true;
                } else {
                    return false;
                }
            case 2:
                mfile2 = new File(FileTextView2.getText().toString());
                if (mfile2.exists()) {
                    return true;
                } else {
                    return false;
                }
            case 3:
                mfile3 = new File(FileTextView3.getText().toString());
                if (mfile3.exists()) {
                    return true;
                } else {
                    return false;
                }
            case 4:
                mfile4 = new File(FileTextView4.getText().toString());
                if (mfile4.exists()) {
                    return true;
                } else {
                    return false;
                }
        }
        return false;
    }


    private File getFile( int i ){
        switch (i){
            case 1:return mfile1;
            case 2:return mfile2;
            case 3:return mfile3;
            case 4:return mfile4;
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
