package com.jikexueyuan.taketaxidriver;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.jikexueyuan.taketaxidriver.bean.TakeTaxiUser;
import com.jikexueyuan.taketaxidriver.bean.UsersAdapter;
import com.jikexueyuan.taketaxidriver.util.ClientUtil;
import com.jikexueyuan.taketaxidriver.util.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements ServiceConnection, AdapterView.OnItemClickListener {

    private static final String TAG = "UserListActivity";

    // 连接服务
    private boolean isBind = false;
    private SocketService.SocketBinder binder = null;

    // 定位
    private LatLng curLatLng;
    private LocationClient mLocationClient;
    boolean isFirstLoc = true; // 是否首次定位

    private ListView lvUser;
    List<TakeTaxiUser> userList;
    private UsersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        MyApplication.addActivity(this);
        initView();
    }

    private void initView() {

        // 打车的用户显示
        lvUser = (ListView) findViewById(R.id.user_list);
        lvUser.setOnItemClickListener(this);
        userList = new ArrayList<>();
        adapter = new UsersAdapter(this, userList);
        lvUser.setAdapter(adapter);

        // 定位初始化
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        // 设置定位方式（选项）
        LocationClientOption option = new LocationClientOption();
        // 设置是否使用GPS
        option.setOpenGps(true);
        // 定位模式 - 默认高精度
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 坐标类型
        option.setCoorType("bd0911");
        // 发起定位请求的时间间隔3s
        option.setScanSpan(3000);
        //设置需要地址信息
        option.setIsNeedAddress(true);
        //可选，默认false，设置是否需要位置语义化结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果
        option.setIsNeedLocationPoiList(true);

        mLocationClient.setLocOption(option);
        mLocationClient.start();

        // 绑定服务
        Intent intentService = new Intent(this, SocketService.class);
        isBind = bindService(intentService, this, Context.BIND_AUTO_CREATE);
    }

    /**
     * 更新列表
     *
     * @param user
     */
    private void updateListView(TakeTaxiUser user) {
        Log.i(TAG, "updateListView");
        TakeTaxiUser tmpUser = getUserFromList(user.getId(), userList);
        if (tmpUser != null) {
            if (user.getDistance() == -1) {
                userList.remove(tmpUser);
            }
        } else {
            if (user.getDistance() != -1) {
                userList.add(user);
            }
        }

        // 更新显示
        adapter.notifyDataSetChanged();
        lvUser.invalidateViews();
    }

    /**
     * 从列表获取一个成员（根据ID）
     *
     * @param id
     * @param userList
     * @return
     */
    public static TakeTaxiUser getUserFromList(String id, List<TakeTaxiUser> userList) {

        for (TakeTaxiUser u : userList) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    //定位监听器
    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            // 打印
//            printLocation(bdLocation);

            // 进行坐标转换 适应百度地图 得到准确的位置
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.COMMON);
            converter.coord(new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude()));
            LatLng destLatLng = converter.convert();
            curLatLng = destLatLng;

//            System.out.println("Convert: latitude -- " + destLatLng.latitude + " longitude -- " + destLatLng.longitude);

            // 发送坐标到服务器
            binder.send(makeLocationMsg(bdLocation.getLatitude(), bdLocation.getLongitude()).toString());
            // 测试时可写死坐标 虚拟机定位不了
//            binder.send(makeLocationMsg(22.30000, 113.506417).toString());
        }
    };

    /**
     * 制作位置信息
     *
     * @param la
     * @param lo
     * @return
     */
    private JSONObject makeLocationMsg(double la, double lo) {
        JSONObject root = new JSONObject();
        try {
            root.put(ClientUtil.JSON_FLAG, ClientUtil.DATA_FLAG_LOCATION);
            root.put(ClientUtil.JSON_USER_TYPE, ClientUtil.TAXI_DRIVER);
            root.put(ClientUtil.JSON_USER_ID, PreferencesUtils.getId());
            root.put(ClientUtil.JSON_NAME, PreferencesUtils.getUser());
            root.put(ClientUtil.JSON_LATITUDE, la);
            root.put(ClientUtil.JSON_LONGITUDE, lo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mLocationClient.stop();
        if (isBind) {
            unbindService(this);
        }
    }

    /**
     * 制作接单信息
     *
     * @return
     */
    private JSONObject makeGetUserMsg(String destId) {
        JSONObject root = new JSONObject();
        try {
            root.put(ClientUtil.JSON_FLAG, ClientUtil.DATA_FLAG_DRIVER);
            root.put(ClientUtil.JSON_USER_TYPE, ClientUtil.TAXI_DRIVER);
            root.put(ClientUtil.JSON_USER_ID, PreferencesUtils.getId());
            root.put(ClientUtil.JSON_DEST_ID, destId);
            root.put(ClientUtil.JSON_NAME, PreferencesUtils.getUser());
            root.put(ClientUtil.JSON_PHONE, PreferencesUtils.getPhone());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TakeTaxiUser user = (TakeTaxiUser) adapter.getItem(i);
        System.out.println(user.getUserName());

        if (user != null) {
            binder.send(makeGetUserMsg(user.getId()).toString());

            Intent intent = new Intent(this, UserActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    }

    /**
     * 在这地图界面返回时 是否退出应用
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.d(TAG, "onBackPressed");
        new AlertDialog.Builder(this)
                .setTitle("确定退出？")
                .setMessage("您要退出此应用吗？")
                .setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyApplication.exitApp();
            }
        }).show();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        binder = (SocketService.SocketBinder) iBinder;
        binder.getService().setMapCallBack(new SocketService.SocketMapCallBack() {
            @Override
            public void onMapMsgCallback(String msg) {
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
            Log.i(TAG, "handleMessage");
            super.handleMessage(msg);

            // 格式：{"flag":2,"type":1,"id":"20161220154859","name":"小鸟🐦","phone":"15000000000",
            // "begin":"出发地","dest":"目的地","latitude":-1,"longitude":-1}
            JSONObject root = null;
            double la = 0;
            double lo = 0;
            try {
                root = new JSONObject(msg.getData().getString("msg"));
                la = root.getDouble(ClientUtil.JSON_LATITUDE);
                lo = root.getDouble(ClientUtil.JSON_LONGITUDE);

                int dist = 0;
                if (la == -1 && lo == -1) {
                    dist = -1;
                } else {
//                    dist = (int) DistanceUtil.getDistance(curLatLng, new LatLng(la, lo));
                    dist = (int) DistanceUtil.getDistance(new LatLng(22.30000, 113.506417), new LatLng(la, lo));
                }

                System.out.println("dist: " + dist);
                // 超出范围的不显示
                if (dist > 2000) {
                    return;
                }

                TakeTaxiUser user = new TakeTaxiUser(
                        root.getString(ClientUtil.JSON_USER_ID),
                        root.getString(ClientUtil.JSON_NAME),
                        root.getString(ClientUtil.JSON_PHONE),
                        root.getString(ClientUtil.JSON_BEGIN),
                        root.getString(ClientUtil.JSON_DEST),
                        dist
                );

                updateListView(user);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    };
}
