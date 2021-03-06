package com.comsince.phonebook.activity;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.comsince.phonebook.PhoneBookApplication;
import com.comsince.phonebook.R;
import com.comsince.phonebook.constant.Constant;
import com.comsince.phonebook.entity.Loginfo;
import com.comsince.phonebook.util.BaiduCloudSaveUtil;
import com.comsince.phonebook.util.DataUtil;
import com.comsince.phonebook.util.FileUtil;
import com.comsince.phonebook.util.HttpTool;
import com.comsince.phonebook.util.SimpleXmlReaderUtil;

public class LoadingActivity extends Activity {
    private String userName;
    private String userPassWord;
    private SimpleXmlReaderUtil simpleXmlReaderUtil;
    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_Fail = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		userName = getIntent().getStringExtra("username");
		userPassWord = getIntent().getStringExtra("password");
		simpleXmlReaderUtil = new SimpleXmlReaderUtil();
		new loginThread().start();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return false;
		}else{
			return super.onKeyDown(keyCode, event);
		}
		
	}
	
	class loginThread extends Thread {

		@Override
		public void run() {
			//从服务器中LoginInfo中读取其注册文件
			String md5password = DataUtil.md5(userPassWord);
			String urlStr = BaiduCloudSaveUtil.generateUrlForGet(Constant.PHONE_BOOK_PATH, "/"+Constant.DIR_LOGIN_INFO+"/"+userName+"_"+md5password+".xml");
			try {
				//InputStream logIn = HttpTool.getStream(urlStr, null, null, HttpTool.GET);
				InputStream logIn = BaiduCloudSaveUtil.getObject(urlStr);
				if(logIn == null ){
					loginHandler.sendEmptyMessage(LOGIN_Fail);
				}else{
					Loginfo loginfo = simpleXmlReaderUtil.readXmlFromInputStream(logIn, Loginfo.class);
					if(loginfo != null){
						if(loginfo.getPassword().equals(userPassWord)){
							//将该文件写入手机中
							//FileUtil.write2SDFromInput(Constant.PHONE_BOOK_PATH+"/"+Constant.DIR_LOGIN_INFO, userName+"_"+md5password+".xml", logIn);
							loginHandler.sendEmptyMessage(LOGIN_SUCCESS);
						}else{
							loginHandler.sendEmptyMessage(LOGIN_Fail);
						}
					}else{
						loginHandler.sendEmptyMessage(LOGIN_Fail);
					}
				}
				
				
			} catch (IOException e) {
				loginHandler.sendEmptyMessage(LOGIN_Fail);
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	Handler loginHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOGIN_SUCCESS:
				setResult(LOGIN_SUCCESS);
				finish();
				break;
			case LOGIN_Fail:
				setResult(LOGIN_Fail);
				finish();
				break;
			default:
				break;
			}
		}
		
	};
	

}
