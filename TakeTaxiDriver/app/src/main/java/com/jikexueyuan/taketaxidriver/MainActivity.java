package com.jikexueyuan.taketaxidriver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jikexueyuan.taketaxidriver.util.ClientUtil;
import com.jikexueyuan.taketaxidriver.util.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "MainActivity";

    // 服务相关
    private boolean isBind = false;
    private SocketService.SocketBinder mBinder = null;

    private EditText userPhone, userName;
    private Button getUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyApplication.addActivity(this);
        initView();
    }

    private void initView() {
        userPhone = (EditText) findViewById(R.id.user_phone);
        userName = (EditText) findViewById(R.id.user_name);

        getUser = (Button) findViewById(R.id.get_user);
        getUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginGetUser();
            }
        });

        PreferencesUtils.init(this);
        if (!"".equals(PreferencesUtils.getUser())) {
            userName.setText(PreferencesUtils.getUser());
        }
        if (!"".equals(PreferencesUtils.getPhone())) {
            userPhone.setText(PreferencesUtils.getPhone());
        }

        // 绑定服务
        Intent intentService = new Intent(this, SocketService.class);
        isBind = bindService(intentService, this, BIND_AUTO_CREATE);
    }

    /**
     * 开始接单
     */
    private void beginGetUser() {
        String phone = userPhone.getText().toString().trim();
        String name = userName.getText().toString().trim();

        // 检查是否为11位手机号码
        if (phone.equals("") || phone.length() != 11 || !isNumber(phone)) {
            Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.equals("")) {
            Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存用户信息到本地
        if (!name.equals(PreferencesUtils.getUser())) {
            PreferencesUtils.setUser(name);
        }

        if (!phone.equals(PreferencesUtils.getPhone())) {
            PreferencesUtils.setPhone(phone);
        }

        PreferencesUtils.commit();

        // 连接服务器
        mBinder.connect(ClientUtil.SERVER_IP, ClientUtil.SERVER_PORT);
    }

    /**
     * 判断一个字符串是否是纯数字（正数）
     *
     * @param str
     * @return
     */
    private boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        if (isBind) {
            unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected");
        mBinder = (SocketService.SocketBinder) iBinder;
        mBinder.getService().setConnectCallBack(new SocketService.SocketConnectCallBack() {
            @Override
            public void onSocketMsgCallback(String msg) {
                Message message = new Message();
                Bundle b = new Bundle();
                b.putString("msg", msg);
                message.setData(b);
                mHandler.sendMessage(message);
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    // 消息处理
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 格式：{"flag":0,"message":"@success"}
            JSONObject root = null;
            String server_msg = null;
            try {
                root = new JSONObject(msg.getData().getString("msg"));
                server_msg = root.getString(ClientUtil.JSON_MSG);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 连接处理
            if (server_msg.equals("@error")) {
                Toast.makeText(MainActivity.this, "无法建立连接，请检查网络", Toast.LENGTH_SHORT).show();
            } else if (server_msg.equals("@success")) {
                JSONObject userInfo = new JSONObject();
                try {
                    userInfo.put(ClientUtil.JSON_FLAG, ClientUtil.DATA_FLAG_REGISTER);
                    userInfo.put(ClientUtil.JSON_USER_TYPE, ClientUtil.TAXI_DRIVER);
                    userInfo.put(ClientUtil.JSON_NAME, userName.getText().toString().trim());
                    userInfo.put(ClientUtil.JSON_PHONE, userPhone.getText().toString().trim());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // 发送用户信息给服务器
                mBinder.send(userInfo.toString());
            }

            // 注册处理（用户信息）
            if (server_msg.equals("@user_error")) {
                Toast.makeText(MainActivity.this, "用户名已存在", Toast.LENGTH_SHORT).show();
            } else if (server_msg.equals("@user_success")) {
                try {
                    PreferencesUtils.setId(root.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(MainActivity.this, UserListActivity.class));
            }
        }
    };
}
