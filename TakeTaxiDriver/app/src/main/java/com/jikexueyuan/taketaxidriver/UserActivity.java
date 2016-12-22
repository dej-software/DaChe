package com.jikexueyuan.taketaxidriver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jikexueyuan.taketaxidriver.bean.TakeTaxiUser;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";

    private TextView userName, userDistance, goWhere, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (TextView) findViewById(R.id.user_name);
        userDistance = (TextView) findViewById(R.id.user_distance);
        userPhone = (TextView) findViewById(R.id.user_phone);
        goWhere = (TextView) findViewById(R.id.go_where);

        TakeTaxiUser user = getIntent().getParcelableExtra("user");
        if (user != null) {
            userName.setText(user.getUserName());
            userPhone.setText(user.getUserPhone());
            userDistance.setText("距离" + user.getDistance() + "米");
            goWhere.setText(user.getBeginAddr() + " 到 " + user.getDestAddr());
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

    /**
     * 点击拨打电话
     *
     * @param view
     */
    public void onCallPhone(View view) {
        Toast.makeText(this, "拨打电话", Toast.LENGTH_SHORT).show();
    }
}
