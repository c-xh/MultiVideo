package com.demo.cx1.myui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.cx1.myui.settingfragment.Channel_Settings_Fragment;
import com.demo.cx1.myui.settingfragment.Coding_Settings_Fragment;
import com.demo.cx1.myui.settingfragment.Holder_Setting_Fragment;

/**
 * Created by cx1 on 2017/6/28.
 */

public class Channel_Setting_Activity extends Activity {

    Button button1;
    Button button2;
    Button button3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_setting_layout);
        initbutton();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Channel_Settings_Fragment fragment = new Channel_Settings_Fragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager. beginTransaction();
                transaction.replace( R.id.fragment2 , fragment );
                transaction.commit();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Holder_Setting_Fragment fragment = new Holder_Setting_Fragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager. beginTransaction();
                transaction.replace( R.id.fragment2 , fragment);
                transaction.commit();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Coding_Settings_Fragment fragment = new Coding_Settings_Fragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager. beginTransaction();
                transaction.replace( R.id.fragment2 , fragment);
                transaction.commit();
            }
        });
    }

    private void initbutton(){
        button1 = (Button)findViewById(R.id.Channel_Settings_button);
        button2 = (Button)findViewById(R.id.holder_setting_button);
        button3 = (Button)findViewById(R.id.Encoding_Setting_button);
    }

}
