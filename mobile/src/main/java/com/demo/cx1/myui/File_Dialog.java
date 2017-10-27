package com.demo.cx1.myui;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cxh on 2017/9/28}:29.
 * E-mail: shon.chen@rock-chips.com
 */

public class File_Dialog {


    Context mContext;

    final AlertDialog alertDialog;
    ListView listView;
    TextView textView;
    // 记录当前的父文件夹
    File currentParent;
    // 记录当前路径下的所有文件的文件数组
    File[] currentFiles;
    int Dialogid;


    public File_Dialog(Context context, int width, int height , int id ) {
        mContext = context;
        this.Dialogid = id;

        // 创建对话框构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 获取布局
        View view2 = View.inflate(mContext, R.layout.file_dialog, null);

        // 获取列出全部文件的ListView
        listView = (ListView) view2.findViewById(R.id.list);
        textView = (TextView) view2.findViewById(R.id.path);
        // 获取系统的SD卡的目录
        File root = new File("/mnt/sdcard/Download/");
        // 如果 SD卡存在
        if (root.exists()) {// exists意思为存在
            currentParent = root;
            currentFiles = root.listFiles();// 调用java库中的File.listFiles()可以列出所有所有文件
            // 使用当前目录下的全部文件、文件夹来填充ListView
            inflateListView(currentFiles);
        }
        // 为ListView的列表项的单击事件绑定监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //此处以确定点击的具体位置，判断后做出响应
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 用户单击了文件，直接返回，不做任何处理
                if (currentFiles[position].isFile()) {
                    if (listener != null) {
                        listener.File_Dialogu__OnClick(currentFiles[position].getPath() , Dialogid);
                        alertDialog.dismiss();// 对话框消失
                    }
//                    return;// 此处或许可以扩展，调用其它的方法，实现某个目的
                }
                // 获取用户点击的文件夹下的所有文件
                File[] tmp = currentFiles[position].listFiles();
                if (tmp == null || tmp.length == 0) {
                    //消息模式，点击后出现提示信息，以前没有用过，比较有意思
                    Toast.makeText(mContext, "当前路径不可访问或该路径下没有文件", Toast.LENGTH_LONG).show();
                } else {
                    // 获取用户单击的列表项对应的文件夹，设为当前的父文件夹
                    currentParent = currentFiles[position];
                    // 保存当前的父文件夹内的全部文件和文件夹
                    currentFiles = tmp;
                    // 再次更新ListView
                    inflateListView(currentFiles);
                }
            }
        });
        // 获取上一级目录的按钮
        Button parent = (Button) view2.findViewById(R.id.parent);
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View source) {
                try {
                    if (!currentParent.getCanonicalPath().equals("/mnt/sdcard")) {
                        // 获取上一级目录
                        currentParent = currentParent.getParentFile();
                        // 列出当前目录下所有文件
                        currentFiles = currentParent.listFiles();
                        // 再次更新ListView
                        inflateListView(currentFiles);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Button cancle = (Button) view2.findViewById(R.id.cancel);
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.File_Dialogu__OnClick("", Dialogid);
                alertDialog.dismiss();// 对话框消失
            }
        });
        // 设置参数
        builder.setTitle("高级设置").setView(view2);
        // 创建对话框
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(width/2,  height/1);
        alertDialog.setCanceledOnTouchOutside(false);//设置带点击弹窗外面没反应

    }

    //声明按钮点击监听
    private File_Dialogu_Listener listener;

    //向外提供一个设置监听的方法
    public void setFile_DialogListener(File_Dialogu_Listener listener){
        this.listener = listener;
    }



    //创建一个监听按钮点击的接口
    public interface File_Dialogu_Listener{
        public void File_Dialogu__OnClick(String file , int Dialogid);
    }

    private void inflateListView(File[] files) {
        // 创建一个List集合，List集合的元素是Map
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < files.length; i++) {
            Map<String, Object> listItem = new HashMap<String, Object>();
            // 如果当前File是文件夹，使用folder图标；否则使用file图标
            if (files[i].isDirectory()) {
                listItem.put("icon", R.drawable.folder);//此处的几张图片就不上传了，做个记号，免得以后出错找半天！！！
            } else {
                listItem.put("icon", R.drawable.file);//图片为icon，folder，home和file
            }
            listItem.put("fileName", files[i].getName());
            // 添加List项
            listItems.add(listItem);
        }
        // 创建一个SimpleAdapter
        SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, listItems,
                R.layout.line, new String[] { "icon", "fileName" }, new int[] {
                R.id.icon, R.id.file_name });
        // 为ListView设置Adapter
        listView.setAdapter(simpleAdapter);
        try {
            textView.setText("当前路径为：" + currentParent.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
