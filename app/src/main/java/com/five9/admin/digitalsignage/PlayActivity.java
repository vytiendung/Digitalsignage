package com.five9.admin.digitalsignage;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.five9.admin.digitalsignage.Common.ApiConnection;
import com.five9.admin.digitalsignage.Common.ListSchedulesManager;
import com.five9.admin.digitalsignage.Object.Schedule;


import java.io.IOException;



public class PlayActivity extends AppCompatActivity {

    private static final String TAG = "PlayActivity";
    public static final int TIME_SHOW_IMAGE = 10000;
    public VideoView videoView;
    public ImageView imvSplash;
    public ImageView imvShowImage;
    public ListSchedulesManager listSchedulesManager;
    private String currentPath;
    private MediaPlayer mediaPlayer;
    private int STATE = 0;
    private final int STATE_SHOW_DEFAULT = 0;
    private final int STATE_PLAY_VIDEO = 1;
    private final int STATE_PLAY_AUDIO = 2;
    private final int STATE_SHOW_IMAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        ApiConnection.getDataOnStart();
        listSchedulesManager = ListSchedulesManager.getInstance();
//        listSchedulesManager.initData();
        showDefaultView();
    }

    private void initView(){
        videoView = findViewById(R.id.video_view);
        imvSplash = findViewById(R.id.imv_splash);
        imvShowImage = findViewById(R.id.imv_image);
    }

    public void showDefaultView(){
        Log.d(TAG, "showDefaultView: ");
        if (videoView.isPlaying())
            videoView.stopPlayback();
        videoView.setVisibility(View.INVISIBLE);
        imvShowImage.setVisibility(View.GONE);
        imvSplash.setVisibility(View.VISIBLE);
        final Thread checkPlayVideoThread = new Thread(){
            @Override
            public void run() {
                while (true){
                    try {
                        if (canPlaySchedule()){
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "playSchedules 1");
                                    playSchedules();
                                }
                            }, 0);
                            break;
                        }
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        checkPlayVideoThread.start();
        STATE = STATE_SHOW_DEFAULT;
    }

    public boolean canPlaySchedule(){
        boolean res = listSchedulesManager.canPlay();
        Log.d(TAG, "canPlaySchedule: " + res);
        return res;
    }

    public void playSchedules(){
        Log.d(TAG, "playSchedules:");
        Schedule schedule = listSchedulesManager.getNextSchedule();
        if (schedule == null){
            showDefaultView();
        } else {
            Log.d(TAG, "playSchedules: " + schedule.getPathOnDevice());
            currentPath = schedule.getPathOnDevice();
            if (schedule.type.equals(Schedule.TYPE_VIDEO)) {
                playVideo(schedule.getPathOnDevice());
            } else if (schedule.type.equals(Schedule.TYPE_AUDIO)) {
                playAudio(schedule.getPathOnDevice());
            } else if (schedule.type.equals(Schedule.TYPE_IMAGE)){
                showImage(schedule.getPathOnDevice());
            } else {
                Log.d(TAG, "playSchedules 2");
                playSchedules();
            }
        }
    }

    private void playVideo(String path) {
        Log.d(TAG, "playVideo: ");
        STATE = STATE_PLAY_VIDEO;
        videoView.setVisibility(View.VISIBLE);
        imvSplash.setVisibility(View.GONE);
        imvShowImage.setVisibility(View.GONE);
        videoView.setVideoPath(path);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "playVideo onError: ");
                listSchedulesManager.onPlayErrFile(currentPath);
                Log.d(TAG, "playSchedules 3");
                playSchedules();
                return true;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "onCompletion: ");
                Log.d(TAG, "playSchedules 4");
                playSchedules();
            }
        });
    }

    private void playAudio(String path){
        Log.d(TAG, "playAudio: ");
        STATE = STATE_PLAY_AUDIO;
        videoView.setVisibility(View.INVISIBLE);
        imvSplash.setVisibility(View.VISIBLE);
        imvShowImage.setVisibility(View.GONE);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            listSchedulesManager.onPlayErrFile(currentPath);
            Log.d(TAG, "playAudio onError: ");
            e.printStackTrace();
        }
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "playAudio onError: ");
                listSchedulesManager.onPlayErrFile(currentPath);
                Log.d(TAG, "playSchedules 5");
                playSchedules();
                return true;

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "playSchedules 6");
                playSchedules();
            }
        });

    }

    private void showImage(String pathOnDevice) {
        Log.d(TAG, "showImage: ");
        try {
            STATE = STATE_SHOW_IMAGE;
            videoView.setVisibility(View.INVISIBLE);
            imvSplash.setVisibility(View.GONE);
            imvShowImage.setVisibility(View.VISIBLE);
            Drawable drawable = Drawable.createFromPath(pathOnDevice);
            imvShowImage.setImageDrawable(drawable);
            RelativeLayout.LayoutParams par = (RelativeLayout.LayoutParams) imvShowImage.getLayoutParams();
//            par.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            par.height = ViewGroup.LayoutParams.MATCH_PARENT;
//            imvShowImage.setLayoutParams(par);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "playSchedules 7");
                    playSchedules();
                }
            }, TIME_SHOW_IMAGE);
        } catch (Exception ex){
            ex.printStackTrace();
            listSchedulesManager.onPlayErrFile(currentPath);
            Log.d(TAG, "playSchedules 8");
            playSchedules();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
       
    }

    @Override
    protected void onPause() {
        try {
            if (mediaPlayer != null)
                mediaPlayer.pause();
        } catch (Exception ex){}
        super.onPause();
    }

    @Override
    protected void onResume() {
        try {
            if (STATE == STATE_PLAY_VIDEO)
                videoView.start();
            else if (STATE == STATE_PLAY_AUDIO) {
                mediaPlayer.start();
            }
        } catch (Exception e){}

        super.onResume();
    }
}
