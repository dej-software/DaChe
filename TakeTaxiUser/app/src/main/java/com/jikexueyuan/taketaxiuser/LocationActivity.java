package com.jikexueyuan.taketaxiuser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.jikexueyuan.taketaxiuser.bean.Location;
import com.jikexueyuan.taketaxiuser.util.PreferencesUtils;

import java.util.List;

public class LocationActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "LocationActivity";

    private EditText inputLocation;
    private PoiSearch poiSearch;

    private TextView toolbarTitle;
    private ListView lvLocation;
    private ArrayAdapter<Location> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        inputLocation = (EditText) findViewById(R.id.input_location);

        // 百度地图 POI搜索
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(poiListener);

        lvLocation = (ListView) findViewById(R.id.location_list);
        lvLocation.setOnItemClickListener(this);
        adapter = new ArrayAdapter<Location>(this, android.R.layout.simple_list_item_1);
        lvLocation.setAdapter(adapter);

        switch (getIntent().getStringExtra("type")) {
            case "begin":
                for (Location location : PreferencesUtils.curLocationList) {
                    adapter.add(location);
                    adapter.notifyDataSetChanged();
                    lvLocation.invalidateViews();
                }
                break;
            case "dest":
                toolbarTitle.setText(R.string.dest_location);
                for (Location location : PreferencesUtils.cityLocationList) {
                    adapter.add(location);
                    adapter.notifyDataSetChanged();
                    lvLocation.invalidateViews();
                }
                break;
        }
    }

    // POI检索监听
    private OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            List<PoiInfo> allPoi = poiResult.getAllPoi();
            if (allPoi != null && !allPoi.isEmpty()) {
                adapter.clear();
                if (allPoi.size() < 10) {
                    for (PoiInfo poiInfo : allPoi) {
                        System.out.println(poiInfo.type + " " + poiInfo.name);
                        adapter.add(new Location(poiInfo.name));
                    }
                } else {
                    for (int i = 0; i < 10; i++) {
                        System.out.println(allPoi.get(i).type + " " + allPoi.get(i).name);
                        adapter.add(new Location(allPoi.get(i).name));
                    }
                }
                adapter.notifyDataSetChanged();
                lvLocation.invalidateViews();
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.i(TAG, "返回");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 点击搜索
     *
     * @param view
     */
    public void onSearch(View view) {
        Log.i(TAG, "onSearch");

        String searchStr = inputLocation.getText().toString().trim();
        if (searchStr.equals("")) {
            Toast.makeText(this, "请输入要搜索的地点", Toast.LENGTH_SHORT).show();
            return;
        }

        poiSearch.searchInCity((new PoiCitySearchOption())
                .city(PreferencesUtils.curCity)
                .keyword(searchStr)
                .pageNum(0)
                .pageCapacity(20));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Location location = adapter.getItem(i);
        System.out.println(location.getName());
        Intent intent = new Intent();
        intent.putExtra("address_name", location.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
