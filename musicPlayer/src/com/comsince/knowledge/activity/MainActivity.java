package com.comsince.knowledge.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.comsince.knowledge.MyApplication;
import com.comsince.knowledge.R;
import com.comsince.knowledge.adapter.MyPagerAdapter;
import com.comsince.knowledge.constant.Constant;
import com.comsince.knowledge.entity.Music;
import com.comsince.knowledge.layout.FavoriteLayout;
import com.comsince.knowledge.layout.LocalLayout;
import com.comsince.knowledge.layout.NetLayout;
import com.comsince.knowledge.service.MusicPlayerService;
import com.comsince.knowledge.utils.BitmapTool;
import com.comsince.knowledge.utils.StrTime;

public class MainActivity extends Activity implements OnClickListener{
	// 基本控件
	ViewPager viewPager;
	ImageButton localmusic, favoritemusic, netmusic;
	ImageView listShowAlbum;
	TextView musicName, musicTime;
	ImageButton preBtn, playBtn, nextBtn;
	ProgressBar MusicprogressBar;
	Context context;
	// 装载滑动页面
	List<View> pageViews;
	LayoutInflater inflater;
	// 滑动布局
	FavoriteLayout favorLayout;
	LocalLayout localLayout;
	NetLayout netLayout;
	MyPagerAdapter pagerAdapter;
	/**
	 * 记录当前音乐的播放状态
	 */
	boolean isPlaying = false; 
	/**
	 * musicReceiver
	 * */
	MusicReceiver musicReceiver;
	/**
	 * music thread 更新播放进度的线程
	 * */
	MusicProgressThread musicProgressThread;

	private static String TAG = "Aisa";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.playermain);
		context = this;
		inflater = LayoutInflater.from(this);
		initView();
		// 实例化musicReceiver
		musicReceiver = new MusicReceiver();
		// 启动service
		startService(new Intent(context, MusicPlayerService.class));
	}

	@Override
	protected void onResume() {
		super.onResume();
		initpageData();
		// 给musicReceiver加intent过滤器
		IntentFilter musicFilter = new IntentFilter();
		musicFilter.addAction(Constant.ACTION_UPDATE);
		// 注册监听器
		registerReceiver(musicReceiver, musicFilter);
		//启动更新音乐播放进度的线程
		musicProgressThread = new MusicProgressThread();
		musicProgressThread.start();
	}

	/**
	 * 
	 * 初始化界面
	 **/
	public void initView() {
		// 滑动控件
		viewPager = (ViewPager) findViewById(R.id.center_body_view_pager);
		pageViews = new ArrayList<View>();

		localLayout = new LocalLayout(context);
		favorLayout = new FavoriteLayout(context);
		netLayout = new NetLayout(context);
		// 加入时注意顺序
		pageViews.add(localLayout);
		pageViews.add(favorLayout);
		pageViews.add(netLayout);

		// 顶部三个导航按钮
		localmusic = (ImageButton) findViewById(R.id.localmusic);
		favoritemusic = (ImageButton) findViewById(R.id.favoritemusic);
		netmusic = (ImageButton) findViewById(R.id.netmusic);
		// 播放进度条
		MusicprogressBar = (ProgressBar) findViewById(R.id.musicProgressBar);
		// 底部播放按钮
		listShowAlbum = (ImageView) findViewById(R.id.list_show_album);
		musicName = (TextView) findViewById(R.id.musicName);
		musicTime = (TextView) findViewById(R.id.musicTime);
		preBtn = (ImageButton) findViewById(R.id.prebtn);
		nextBtn = (ImageButton) findViewById(R.id.nextbtn);
		playBtn = (ImageButton) findViewById(R.id.playbtn);
		// 设置监听器
		setupListener();
	}

	/**
	 * 设置监听器
	 * */
	public void setupListener() {
		localmusic.setOnClickListener(new MyMusicListener());
		favoritemusic.setOnClickListener(new LoveMusicListener());
		netmusic.setOnClickListener(new NetMusicListener());
		preBtn.setOnClickListener(this);
		nextBtn.setOnClickListener(this);
		playBtn.setOnClickListener(this);
	}

	public void initpageData() {
		if (viewPager.getChildCount() <= 0) {
			pagerAdapter = new MyPagerAdapter(pageViews);
		}
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				localmusic.setSelected(false);
				favoritemusic.setSelected(false);
				netmusic.setSelected(false);
				switch (position) {
				case 0:
					localmusic.setSelected(true);
					break;
				case 1:
					favoritemusic.setSelected(true);
					break;
				case 2:
					netmusic.setSelected(true);
					break;
				default:
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
		viewPager.setCurrentItem(0);
		localmusic.setSelected(true);
	}

	// 我的音乐
	class MyMusicListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			localmusic.setSelected(true);
			favoritemusic.setSelected(false);
			netmusic.setSelected(false);
			viewPager.setCurrentItem(0);
		}

	}

	// 最喜欢的歌曲列表
	class LoveMusicListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			localmusic.setSelected(false);
			favoritemusic.setSelected(true);
			netmusic.setSelected(false);
			viewPager.setCurrentItem(1);
		}

	}

	// 网络歌曲列表
	class NetMusicListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			localmusic.setSelected(false);
			favoritemusic.setSelected(false);
			netmusic.setSelected(true);
			viewPager.setCurrentItem(2);
		}

	}

	/**
	 * 定义更新底部播放控件的状态，播放,暂停 播放的图片，音乐名称，音乐的时间
	 * */
	/**
	 * 音乐的总时间
	 * */
	int totalms = 1;
	/**
	 * 播放状态
	 * */
	int status;
	public Bitmap nowBitMap;
	public Music music;

	private class MusicReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.ACTION_UPDATE)) {
				// 实现从intent获取entity,该类必须实现Serializable接口
				music = (Music) intent.getSerializableExtra("music");
				totalms = intent.getIntExtra("totalms", 28888);
				status = intent.getIntExtra("status", -1);
				String musicname = music.getMusicName();
				musicName.setText(musicname);
				String musictime = music.getTime();
				//totalms = Integer.parseInt(musictime);
				musicTime.setText("00:00/" + StrTime.getTime(musictime));
				// 设置音乐图片
				String albumkey = music.getAlbumkey();
				if (!TextUtils.isEmpty(albumkey)) {
					nowBitMap = BitmapTool.getbitBmBykey(context, albumkey);
				}
				if (nowBitMap != null && !nowBitMap.isRecycled()) {
					listShowAlbum.setImageBitmap(nowBitMap);
				} else {
					listShowAlbum.setImageResource(R.drawable.default_bg_s);
				}
				if (status == 3) {
					playBtn.setImageResource(R.drawable.desktop_pausebt);
					isPlaying = true;
				}
			}
		}

	}

	/**
	 * 监听musicPalyer,进而更新播放进度
	 * 
	 * */
	boolean isrunable = true;
	/**
	 * 当前播放进度：ms
	 * */
	int curms;

	private class MusicProgressThread extends Thread {

		@Override
		public void run() {
			while (isrunable) {
				if (MyApplication.mediaPlayer != null && MyApplication.mediaPlayer.isPlaying()) {
					curms = MyApplication.mediaPlayer.getCurrentPosition();
					musicHandler.sendEmptyMessage(1);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * musicPlayer handler 更新播放进度
	 * */
	Handler musicHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				int musicProgress = curms * 100 / totalms;
				MusicprogressBar.setProgress(musicProgress);
				musicTime.setText(StrTime.gettim(curms) + "/" + StrTime.gettim(totalms));
				break;

			default:
				break;
			}
		}

	};
	
	/**
	 * 播放intent
	 */
	Intent playIntent;
   
	@Override
	public void onClick(View v) {
		playIntent = new Intent();
		switch (v.getId()) {
		case R.id.playbtn:
			if(isPlaying){
				playIntent.setAction(Constant.ACTION_PAUSE);
				sendBroadcast(playIntent);
				isPlaying = false;
				playBtn.setImageResource(R.drawable.play_play_btn);
			}else{
				playBtn.setImageResource(R.drawable.desktop_pausebt);
				playIntent.setAction(Constant.ACTION_PLAY);
				sendBroadcast(playIntent);
				isPlaying = true;
			}
			break;
		case R.id.nextbtn:
			isPlaying = true;
			playIntent.setAction(Constant.ACTION_NEXT);
			sendBroadcast(playIntent);
			break;
		case R.id.prebtn:
			isPlaying = true;
			playIntent.setAction(Constant.ACTION_PREVIOUS);
			sendBroadcast(playIntent);
			break;
		default:
			break;
		}
	}

}