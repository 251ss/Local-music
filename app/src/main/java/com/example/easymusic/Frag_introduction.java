package com.example.easymusic;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Frag_introduction extends Fragment implements View.OnTouchListener {
    TextView textView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_introduction, container, false);
        view.setOnTouchListener((View.OnTouchListener) this);
        textView = view.findViewById(R.id.link_details);
        textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        textView.getPaint().setAntiAlias(true);//抗锯齿
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.bilibili.com/video/BV1gR4y147fp?p=3&spm_id_from=333.1007.top_right_bar_window_history.content.click"));
                startActivity(Intent.createChooser(intent, "Choose a Browser"));
            }
        });
        return view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }
}
