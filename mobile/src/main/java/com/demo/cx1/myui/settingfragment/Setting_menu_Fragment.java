package com.demo.cx1.myui.settingfragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demo.cx1.myui.R;

/**
 * Created by cx1 on 2017/6/29.
 */

public class Setting_menu_Fragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_menu_fragment_layout , container , false);
        return view;
    }
}
