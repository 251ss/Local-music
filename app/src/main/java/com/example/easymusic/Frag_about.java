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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Frag_about extends Fragment implements View.OnTouchListener {
    TextView qq, email, blog;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_about, container, false);
        view.setOnTouchListener((View.OnTouchListener) this);
        qq = view.findViewById(R.id.link_qq);
        blog = view.findViewById(R.id.link_blog);
        email = view.findViewById(R.id.link_email);

        qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "qq:1127983768", Toast.LENGTH_SHORT).show();
            }
        });
        blog.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG ); //下划线
        blog.getPaint().setAntiAlias(true);//抗锯齿
        blog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/251ss"));
                startActivity(Intent.createChooser(intent, "Choose a Browser"));
            }
        });
        email.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG ); //下划线
        email.getPaint().setAntiAlias(true);//抗锯齿
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"1127983768@qq.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Hi, This is a test mail..");
                intent.putExtra(Intent.EXTRA_TEXT   , "Welcome! Contact me anytime!  --from author");
                startActivity(Intent.createChooser(intent, "Choose an Email Client"));
            }
        });

        return view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }
}
