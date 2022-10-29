package com.example.easymusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView nextIv,playIv,lastIv,albumIv,menuIv, menuIv1, menuIv2, menuIv3;
    private TextView singerTv,songTv;
    private SearchView musicSearch;
    private RecyclerView musicRv;
    private List<LocalMusicBean> MainData, SetData;
    private LocalMusicAdapter MusicAdapter;

    //与服务有关的部分
    private int musicDataSize;
    private int currentId=-2;
    private MusicService.MyBinder mMyBinder;
    private ServiceConnection serviceConnection;
    private Handler handler;
    private SeekBar seekBar;
    private Runnable runnable;
    private BroadcastReceiver myReceiver;
    private boolean isNetAvailable=false;

    //侧滑栏部分
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Fragment fragment_about, fragment_introduction, fragment_message;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private int Notification_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化控件
        initView();

        setRecycleView();

        //读入数据
        loadLocalMusicData();

        //启动音乐服务
        Intent serviceStart = new Intent(this, MusicService.class);
        startService(serviceStart);
        //绑定服务
        Intent mediaServiceIntent = new Intent(this, MusicService.class);
        serviceConn();
        bindService(mediaServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        //设置广播接受者
        setReceiver();

        //设置搜索框
        setSearchList();

        //设置侧滑栏
        setDrawer();

        //设置点击事件
        setEventListener();

        //设置菜单fragment页面
        setFragment();

    }

    private void setRecycleView() {
        //设置RecyclerView
        MainData = new ArrayList<>();
        SetData = new ArrayList<>();
        //创建适配器对象
        MusicAdapter = new LocalMusicAdapter(this, SetData);
        musicRv.setAdapter(MusicAdapter);
        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(layoutManager);

    }

    private void loadLocalMusicData() {
        //加载本地存储当中的音乐mp3文件到集合当中
        // 获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
        // 获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
        // 遍历Cursor
        int id = -1;
        LocalMusicBean bean;

        while (cursor.moveToNext()) {
            //歌曲时间
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            //限制时长
            if (duration>30*1000) {
                //歌名
                String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                //歌手
                String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                //专辑
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                if(album.equals("Sounds")) continue;
                ++id;
                String sid = String.valueOf(id+1);
                //路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));


                bean = new LocalMusicBean(sid, song, singer, album, duration, path);
                MainData.add(bean);
            }
        }
        if(id==0) {
            bean = new LocalMusicBean("0", "没有找到本地歌曲", "", "", 0, "");
            MainData.add(bean);
            currentId=-2;
        } else{
            currentId=-1;
        }
        musicDataSize = MainData.size();
        SetData.addAll(MainData);
        //数据源变化，提示适配器更新
        MusicAdapter.notifyDataSetChanged();
    }

    private void serviceConn() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mMyBinder = (MusicService.MyBinder) iBinder;

                //如果主activity启动时正在播放
                if(mMyBinder.getMediaPlayState()==1) {
                    playIv.setImageResource(R.mipmap.icon_pause);
                    songTv.setText(mMyBinder.getMusicSong());
                    singerTv.setText(mMyBinder.getMusicSinger());
                    currentId=mMyBinder.getMusicId();
                    //设置进度条大小
                    seekBar.setMax(mMyBinder.getMusicDuration());
                    Log.d("currentId", Integer.toString(currentId));
                }

                //传递播放列表
                mMyBinder.setDatas(MainData);

                //初始化进度条
                seekBar.setProgress(0);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        //响应用户点击设置进度条
                        if(b && currentId!=-1 &&currentId!=-2) {
                            mMyBinder.seekToPosition(seekBar.getProgress());
                            playIv.setImageResource(R.drawable.ripple_pause);
                        } else if(b){
                            Toast.makeText(MainActivity.this, "请选择播放音乐", Toast.LENGTH_SHORT).show();
                            seekBar.setProgress(0);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //mMyBinder.seekToPosition(seekBar.getProgress());
                    }
                });

                //设置进度条控制线程
                handler = new Handler();
                runnable = new Runnable() {
                    private int pre=-1, pos;
                    @Override
                    public void run() {
                        pos = mMyBinder.getPlayPosition();


                        if(currentId!=-1 && currentId!=-2)
                            seekBar.setProgress(pos);
                        Log.d("RunnablePos", String.valueOf(pos));

                        if(pre!=pos) handler.postDelayed(runnable, 1000);
                        else handler.postDelayed(runnable, 2000);

//                        //修复最后一首时播放图标bug
//                        if(currentId==listSiz && pos!=pre) playIv.setImageResource(R.mipmap.icon_pause);
                        pre = pos;
                    }
                };
                handler.post(runnable);
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
    }

    private void setEventListener() {
        /* 设置点击事件*/
        MusicAdapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                LocalMusicBean bean = SetData.get(position);
                playMusicOnService(Integer.parseInt(bean.getId())-1);
            }
        });

        //设置单曲循环/列表循环
        albumIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMyBinder.setPlayModule();
                if(mMyBinder.getPlayModule()){
                    Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                    albumIv.setImageResource(R.mipmap.icon_song2_loop_blue);
                } else {
                    Toast.makeText(MainActivity.this, "列表循环", Toast.LENGTH_SHORT).show();
                    albumIv.setImageResource(R.mipmap.icon_song2);
                }
            }
        });

        menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setReceiver() {
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                songTv.setText(intent.getStringExtra("music_song"));
                singerTv.setText(intent.getStringExtra("music_singer"));
                currentId = intent.getIntExtra("music_id", -1);
                seekBar.setMax(intent.getIntExtra("music_duration", 0));
            }
        };
        IntentFilter intentFilter = new IntentFilter("UI_info");
        registerReceiver(myReceiver, intentFilter);
    }

    private void setSearchList() {
        //设置SearchView默认是否自动缩小为图标
        musicSearch.setIconifiedByDefault(true);
        musicSearch.setFocusable(false);
        //设置搜索框监听器
        musicSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //点击搜索按钮时激发
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //输入时激发
                if(TextUtils.isEmpty(s)){
                    //没有过滤条件内容
                    SetData.clear();
                    SetData.addAll(MainData);
                    MusicAdapter.notifyDataSetChanged();
                } else {
                    //根据输入内容对RecycleView搜索
//                    List<LocalMusicBean> FilterData = new ArrayList<>();
                    SetData.clear();
                    for (LocalMusicBean bean:MainData){
                        if(bean.getSong().contains(s) || bean.getSinger().contains(s)){
//                            FilterData.add(bean);
                            SetData.add(bean);
                        }
                    }
//                    SetData.clear();
//                    SetData.addAll(FilterData);
                    MusicAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

    }

    private void setDrawer() {
        navigationView.setItemIconTintList(null);
        navigationView.getChildAt(0).setVerticalScrollBarEnabled(false);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.item_0:
                        Log.d("ItemSelectedListener", "item0");
                        //fragmentManager.popBackStackImmediate(null, 1);
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_about).commit();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_introduction).commit();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_message).commit();
//                        Toast.makeText(MainActivity.this, "!!!!", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.item_1:
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment, fragment_message).commit();
                        Log.d("ItemSelectedListener", "item1");
                        Toast.makeText(MainActivity.this, "没有新消息嗷~", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.item_2:
                        //弹出功能介绍
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment, fragment_introduction).commit();
                        Log.d("ItemSelectedListener", "item2");
//                        Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.item_3:
                        //弹出软件开发介绍
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment, fragment_about).commit();
                        Log.d("ItemSelectedListener", "item3");
//                        Toast.makeText(MainActivity.this, "3", Toast.LENGTH_SHORT).show();
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                transaction.addToBackStack(null);
                return true;
            }
        });

        //设置滑动主activity跟随
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                //获取高度宽度
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                Display display = MainActivity.this.getWindowManager().getDefaultDisplay();
                display.getMetrics(metrics);
                //设置activity高度，注意要加上状态栏高度
                RelativeLayout relativeLayout = findViewById(R.id.main_activity);
                relativeLayout.layout(drawerView.getRight(), Notification_height, drawerView.getRight()+metrics.widthPixels, metrics.heightPixels);

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void setFragment() {
        fragmentManager = getSupportFragmentManager();
        fragment_message = new Frag_message();
        fragment_introduction = new Frag_introduction();
        fragment_about = new Frag_about();
        transaction = fragmentManager.beginTransaction();
    }

    public void playMusicOnService(int Id) {
        /*根据传入ID播放音乐*/
        if(Id<10 && isNetAvailable) Toast.makeText(MainActivity.this, "在线歌曲", Toast.LENGTH_SHORT).show();
        //设置服务信息
        mMyBinder.setMusic(Id);
        //切换按钮图片
        playIv.setImageResource(R.drawable.ripple_pause);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bottom_iv_last:
                if(currentId==-2){
                    //没有播放音乐
                    Toast.makeText(this, "没有获取到音乐", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(currentId==-1){
                    //没有播放音乐
                    Toast.makeText(this, "开始播放第一首~", Toast.LENGTH_SHORT).show();
                    currentId=1;
                }
                if (currentId==0) {
                    Toast.makeText(this,"已经是第一首了嗷~",Toast.LENGTH_SHORT).show();
                    return;
                }
                currentId = currentId-1;
                playMusicOnService(currentId);

                break;
            case R.id.bottom_iv_next:
                if(currentId==-2){
                    //没有播放音乐
                    Toast.makeText(this, "没有获取到音乐", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(currentId==-1){
                    //没有播放音乐
                    Toast.makeText(this, "开始播放最后一首~", Toast.LENGTH_SHORT).show();
                    currentId = musicDataSize-2;
                }
                if (currentId==musicDataSize-1) {
                    Toast.makeText(this,"没有下一首了嗷~",Toast.LENGTH_SHORT).show();
                    return;
                }
                currentId=currentId+1;
                playMusicOnService(currentId);

                break;
            case R.id.bottom_iv_play:
                if(currentId==-1){
                    //没有播放音乐
                    Toast.makeText(this, "请选择播放音乐", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(currentId==-2){
                    Toast.makeText(this, "请打开软件存储权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                int state = mMyBinder.getMediaPlayState();
                if(state==1) {
                    mMyBinder.pauseMusic();
                    playIv.setImageResource(R.drawable.ripple_play);
                } else if(state==0){
                    mMyBinder.playMusic();
                    playIv.setImageResource(R.drawable.ripple_pause);
                } else if(state==2){
                    Toast.makeText(this, "播放结束了~", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
        handler.removeCallbacks(runnable);
        unbindService(serviceConnection);
    }

    private void initView() {
        /*初始化控件的函数*/
        //页面控件
        nextIv = findViewById(R.id.bottom_iv_next);
        playIv = findViewById(R.id.bottom_iv_play);
        lastIv = findViewById(R.id.bottom_iv_last);
        albumIv = findViewById(R.id.bottom_iv_icon);
        singerTv = findViewById(R.id.bottom_tv_singer);
        songTv = findViewById(R.id.bottom_tv_song);
        musicRv = findViewById(R.id.music_rv);
        seekBar = findViewById(R.id.music_seekBar);
        musicSearch = findViewById(R.id.music_search);

        //侧滑栏控件
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        menuIv = findViewById(R.id.menu_icon);
        //获取状态栏高度
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        Notification_height = getResources().getDimensionPixelSize(resourceId);

        nextIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
        playIv.setOnClickListener(this);

        seekBar.setProgress(0);
    }
}
