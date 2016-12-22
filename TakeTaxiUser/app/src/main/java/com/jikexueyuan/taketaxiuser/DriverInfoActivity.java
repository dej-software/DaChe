package com.jikexueyuan.taketaxiuser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jikexueyuan.taketaxiuser.util.ClientUtil;
import com.jikexueyuan.taketaxiuser.util.DataUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class DriverInfoActivity extends AppCompatActivity implements ServiceConnection, View.OnClickListener {

    private static final String TAG = "DriverInfoActivity";

    private LinearLayout waitLayout, driverInfoLayout;

    private Button cancelTakeTaxi, callPhone;
    private TextView driverInfo, driverPhone;

    // 是否绑定了服务
    private boolean isBind = false;
    private SocketService.SocketBinder binder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_info);

        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        waitLayout = (LinearLayout) findViewById(R.id.wait_layout);
        driverInfoLayout = (LinearLayout) findViewById(R.id.taxi_layout);

        cancelTakeTaxi = (Button) findViewById(R.id.cancel_take_taxi);
        cancelTakeTaxi.setOnClickListener(this);
        callPhone = (Button) findViewById(R.id.call);
        callPhone.setOnClickListener(this);

        driverInfo = (TextView) findViewById(R.id.driver_info);
        driverPhone = (TextView) findViewById(R.id.driver_phone);

        // 绑定服务
        Intent intentService = new Intent(this, SocketService.class);
        isBind = bindService(intentService, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_take_taxi:
                setResult(DataUtils.CANCEL_TAKE_TAXI);
                finish();
                break;
            case R.id.call:
                Toast.makeText(this, "拨打电话", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.i(TAG, "返回");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (isBind) {
            unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        binder = (SocketService.SocketBinder) iBinder;
        binder.getService().setTakeTaxiCallBack(new SocketService.SocketTakeTaxiCallBack() {
            @Override
            public void onSocketMsgCallback(String msg) {
                Message message = new Message();
                Bundle b = new Bundle();
                b.putString("msg", msg);
                message.setData(b);
                handler.sendMessage(message);
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            // 格式：
            JSONObject root = null;
            try {
                // 显示接单的司机信息
                root = new JSONObject(msg.getData().getString("msg"));
                driverInfo.setText("司机 " + root.getString(ClientUtil.JSON_NAME) + " 已接单");
                driverPhone.setText(root.getString(ClientUtil.JSON_PHONE));
                waitLayout.setVisibility(View.GONE);
                driverInfoLayout.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    };
}
