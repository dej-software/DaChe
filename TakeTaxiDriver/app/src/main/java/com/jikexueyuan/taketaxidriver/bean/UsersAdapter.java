package com.jikexueyuan.taketaxidriver.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jikexueyuan.taketaxidriver.R;

import java.util.List;

/**
 * Created by dej on 2016/12/20.
 */

public class UsersAdapter extends BaseAdapter {

    private Context context;
    private List<TakeTaxiUser> userList;

    public UsersAdapter(Context context, List<TakeTaxiUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.user_cell, null);

            holder.tvUserInfo = (TextView) view.findViewById(R.id.tv_user_info);
            holder.tvLocationInfo = (TextView) view.findViewById(R.id.tv_location_info);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        TakeTaxiUser user = (TakeTaxiUser) getItem(i);

        holder.tvUserInfo.setText(user.getUserName() + " 距离" + user.getDistance() + "米");
        holder.tvLocationInfo.setText(user.getBeginAddr() + " 到 " + user.getDestAddr());

        return view;
    }

    private static class ViewHolder {
        TextView tvUserInfo, tvLocationInfo;
    }
}
