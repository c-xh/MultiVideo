package com.demo.cx1.myui.settingfragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.demo.cx1.myui.R;

/**
 * Created by cx1 on 2017/6/29.
 */

public class Channel_Settings_Fragment extends Fragment {
    private Spinner spinner;
    private EditText editText;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_channel_option_fragment_layout , container , false);
        spinner = (Spinner) view.findViewById(R.id.display_mode_spinner);
        editText = (EditText) view.findViewById(R.id.editText);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String string = spinner.getSelectedItemId() + "";
                editText.setText(string);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }

}
