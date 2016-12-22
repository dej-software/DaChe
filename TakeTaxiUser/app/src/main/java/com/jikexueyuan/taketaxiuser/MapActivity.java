package com.jikexueyuan.taketaxiuser;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.jikexueyuan.taketaxiuser.bean.Location;
import com.jikexueyuan.taketaxiuser.util.ClientUtil;
import com.jikexueyuan.taketaxiuser.util.DataUtils;
import com.jikexueyuan.taketaxiuser.util.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements ServiceConnection, View.OnClickListener {

    private static final String TAG = "MapActivity";

    // 是否绑定了服务
    private boolean isBind = false;
    private SocketService.SocketBinder binder = null;

    // 出发、终点、约车 按钮
    private Button btnBegin, btnDest, btnTakeTaxi;
    private String beginAddr = "", destAddr = "";

    private TextureMapView mapView;
    private BaiduMap baiduMap;

    // 定位相关
    private LatLng curLatLng;
    private LocationClient mLocationClient;
    boolean isFirstLoc = true; // 是否首次定位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MyApplication.addActivity(this);
        initView();
    }

    private void initView() {

        btnBegin = (Button) findViewById(R.id.departure);
        btnDest = (Button) findViewById(R.id.destination);
        btnTakeTaxi = (Button) findViewById(R.id.take_taxi);
        btnBegin.setOnClickListener(this);
        btnDest.setOnClickListener(this);
        btnTakeTaxi.setOnClickListener(this);

        // 更新定位
        findViewById(R.id.update_location).setOnClickListener(this);

        // 地图初始化
        mapView = (TextureMapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15));
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);

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
        Intent intentService = new Intent(MapActivity.this, SocketService.class);
        isBind = bindService(intentService, this, Context.BIND_AUTO_CREATE);
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

//            System.out.println("Convert: latitude -- " + destLatLng.latitude + " longitude -- " + destLatLng.longitude);

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())   //设置定位数据的精度，单位米
                    .direction(0)                     //设定定位数据的方向
                    .latitude(destLatLng.latitude) //设定定位数据的纬度
                    .longitude(destLatLng.longitude)//设定定位数据的经度
                    .build();   //构建地址数据

            // 设置
            baiduMap.setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;

                // 初始化一些当前信息
                curLatLng = destLatLng;
                PreferencesUtils.curCity = bdLocation.getCity();
                beginAddr = bdLocation.getLocationDescribe();
                btnBegin.setText(beginAddr);
                initCityLocationList();
                List<Poi> poiList = bdLocation.getPoiList();// POI数据
                if (!poiList.isEmpty()) {
                    if (!PreferencesUtils.curLocationList.isEmpty()) {
                        PreferencesUtils.curLocationList.clear();
                    }
                    for (Poi poi : poiList) {
                        PreferencesUtils.curLocationList.add(new Location(poi.getName()));
                    }
                }

                //描述地图状态将要发生的变化，newLatLngZoom设置地图新中心点
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(destLatLng, 16.0f);
                //以动画方式更新地图状态
                baiduMap.animateMapStatus(mapStatusUpdate);
            }
        }
    };

    /**
     * 打印获得的定位信息
     *
     * @param bdLocation
     */
    public void printLocation(BDLocation bdLocation) {
        //Receive Location
        StringBuffer sb = new StringBuffer(256);
        sb.append("time : ");
        sb.append(bdLocation.getTime());
        sb.append("\nerror code : ");
        sb.append(bdLocation.getLocType());
        sb.append("\nlatitude : ");
        sb.append(bdLocation.getLatitude());
        sb.append("\nlontitude : ");
        sb.append(bdLocation.getLongitude());
        sb.append("\nradius : ");
        sb.append(bdLocation.getRadius());
        if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
            sb.append("\nspeed : ");
            sb.append(bdLocation.getSpeed());// 单位：公里每小时
            sb.append("\nsatellite : ");
            sb.append(bdLocation.getSatelliteNumber());
            sb.append("\nheight : ");
            sb.append(bdLocation.getAltitude());// 单位：米
            sb.append("\ndirection : ");
            sb.append(bdLocation.getDirection());// 单位度
            sb.append("\naddr : ");
            sb.append(bdLocation.getAddrStr());
            sb.append("\ndescribe : ");
            sb.append("gps定位成功");

        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
            sb.append("\naddr : ");
            sb.append(bdLocation.getAddrStr());
            //运营商信息
            sb.append("\noperationers : ");
            sb.append(bdLocation.getOperators());
            sb.append("\ndescribe : ");
            sb.append("网络定位成功");
        } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");
        } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");
        } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        sb.append("\nlocationdescribe : ");
        sb.append(bdLocation.getCity() + "---");
        sb.append(bdLocation.getLocationDescribe());// 位置语义化信息
        List<Poi> list = bdLocation.getPoiList();// POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
            }
        }
        Log.i("BaiduLocationApiDem", sb.toString());
    }

    // 存放地图覆盖物
    private Map<MarkerOptions, Overlay> overlays = new HashMap<>();

    /**
     * 在地图上标记出周围司机
     *
     * @param id
     * @param la
     * @param lo
     */
    public void addDriverOverlay(String id, double la, double lo) {
        Log.d(TAG, "addUserOverlay");
        LatLng ll = new LatLng(la, lo);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.user_maker, null);

        // 自定义覆盖物
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);
        MarkerOptions options = null;
        Overlay overlay = null;

        // 同一个覆盖物  删掉旧的 添加新的
        Iterator iterator = overlays.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            options = (MarkerOptions) entry.getKey();
            overlay = (Overlay) entry.getValue();
            if (id.equals(options.getTitle())) {
                overlay.remove();

                // 如果司机退出司机端 删掉此司机覆盖物
                if ((la == -1) && (lo == -1)) {
                    Log.d(TAG, "deleteOverlay");
                    iterator.remove();
                    return;
                }

                Log.d(TAG, "updateOverlay");
                options.position(ll);
                overlay = baiduMap.addOverlay(options);
                entry.setValue(overlay);
                return;
            }
        }

        // 新的覆盖物
        options = new MarkerOptions()
                .title(id)
                .position(ll)
                .icon(bitmap);
        overlay = baiduMap.addOverlay(options);
        overlays.put(options, overlay);
        Log.d(TAG, "addNewOverlay");
    }

    /**
     * 制作位置信息
     *
     * @return
     */
    private JSONObject makeLocationMsg(double la, double lo) {
        JSONObject root = new JSONObject();
        try {
            root.put(ClientUtil.JSON_FLAG, ClientUtil.DATA_FLAG_LOCATION);
            root.put(ClientUtil.JSON_USER_TYPE, ClientUtil.TAKE_TAXI_USER);
            root.put(ClientUtil.JSON_USER_ID, PreferencesUtils.getId());
            root.put(ClientUtil.JSON_NAME, PreferencesUtils.getUser());
            root.put(ClientUtil.JSON_PHONE, PreferencesUtils.getPhone());
            root.put(ClientUtil.JSON_LATITUDE, la);
            root.put(ClientUtil.JSON_LONGITUDE, lo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

    /**
     * 制作路线信息（约车、取消）
     *
     * @return
     */
    private JSONObject makeRouteMsg(double la, double lo) {
        JSONObject root = new JSONObject();
        try {
            root.put(ClientUtil.JSON_FLAG, ClientUtil.DATA_FLAG_USER);
            root.put(ClientUtil.JSON_USER_TYPE, ClientUtil.TAKE_TAXI_USER);
            root.put(ClientUtil.JSON_USER_ID, PreferencesUtils.getId());
            root.put(ClientUtil.JSON_NAME, PreferencesUtils.getUser());
            root.put(ClientUtil.JSON_PHONE, PreferencesUtils.getPhone());
            root.put(ClientUtil.JSON_BEGIN, btnBegin.getText().toString());
            root.put(ClientUtil.JSON_DEST, btnDest.getText().toString());
            root.put(ClientUtil.JSON_LATITUDE, la);
            root.put(ClientUtil.JSON_LONGITUDE, lo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.update_location:
                isFirstLoc = true;
                break;
            case R.id.departure:
                Intent iBegin = new Intent(this, LocationActivity.class);
                iBegin.putExtra("type", "begin");
                startActivityForResult(iBegin, DataUtils.BEGIN_LOCATION);
                break;
            case R.id.destination:
                Intent iDest = new Intent(this, LocationActivity.class);
                iDest.putExtra("type", "dest");
                startActivityForResult(iDest, DataUtils.DEST_LOCATION);
                break;
            case R.id.take_taxi:
                if ("".equals(beginAddr) || "".equals(destAddr)) {
                    Toast.makeText(this, "请选择起始位置", Toast.LENGTH_SHORT).show();
                    return;
                }
                binder.send(makeRouteMsg(curLatLng.latitude, curLatLng.longitude).toString());
                startActivityForResult(new Intent(this, DriverInfoActivity.class), DataUtils.TAKE_TAXI);
                break;
        }
    }

    /**
     * 初始化当前定位到的城市的常用位置（机场、火车站、景点）
     */
    private void initCityLocationList() {

        if (!PreferencesUtils.cityLocationList.isEmpty()) {
            PreferencesUtils.cityLocationList.clear();
        }

        final PoiSearch poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                List<PoiInfo> allPoi = poiResult.getAllPoi();
                for (PoiInfo poiInfo : allPoi) {
                    System.out.println(poiInfo.name);
                }
                if (allPoi != null && !allPoi.isEmpty()) {
                    if (allPoi.size() < 4) {
                        for (PoiInfo poiInfo : allPoi) {
                            PreferencesUtils.cityLocationList.add(new Location(poiInfo.name));
                        }
                    } else {
                        for (int i = 0; i < 4; i++) {
                            PreferencesUtils.cityLocationList.add(new Location(allPoi.get(i).name));
                        }
                    }
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });

        // 在线程中搜索
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 搜索三次
                poiSearch.searchInCity((new PoiCitySearchOption())
                        .city(PreferencesUtils.curCity)
                        .keyword("机场")
                        .pageNum(0)
                        .pageCapacity(4));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                poiSearch.searchInCity((new PoiCitySearchOption())
                        .city(PreferencesUtils.curCity)
                        .keyword("火车站")
                        .pageNum(0)
                        .pageCapacity(4));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                poiSearch.searchInCity((new PoiCitySearchOption())
                        .city(PreferencesUtils.curCity)
                        .keyword("景点")
                        .pageNum(0)
                        .pageCapacity(4));
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mLocationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        if (isBind) {
            unbindService(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == DataUtils.CANCEL_TAKE_TAXI) {
            // 发送位置为-1 表示取消订单
            binder.send(makeRouteMsg(-1, -1).toString());
        }

        if (resultCode == RESULT_OK && data != null) {
            String addr = data.getStringExtra("address_name");
            if (addr != null) {
                if (requestCode == DataUtils.BEGIN_LOCATION) {
                    btnBegin.setText(addr);
                    beginAddr = addr;
                } else if (requestCode == DataUtils.DEST_LOCATION) {
                    btnDest.setText(addr);
                    destAddr = addr;
                }
            }
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
    public void onServiceDisconnected(ComponentName name) {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            // 格式：{"flag":4,"type":2,"id":"20161220111045","name":"233","latitude":22.3,"longitude":113.506417}
            JSONObject root = null;
            String id = null;
            double la = 0;
            double lo = 0;
            try {
                root = new JSONObject(msg.getData().getString("msg"));
                id = root.getString(ClientUtil.JSON_USER_ID);
                la = root.getDouble(ClientUtil.JSON_LATITUDE);
                lo = root.getDouble(ClientUtil.JSON_LONGITUDE);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

//            la = la + (0.01 * Math.random());
//            System.out.println("la:" + la);

//            System.out.println(DistanceUtil.getDistance(curLatLng, new LatLng(la, lo)));

            // 只显示两公里范围内
            if (((la == -1) && (lo == -1)) || DistanceUtil.getDistance(curLatLng, new LatLng(la, lo)) < 2000.0) {
                addDriverOverlay(id, la, lo);
            }
        }
    };
}
