package com.jikexueyuan.taketaxidriver;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jikexueyuan.taketaxidriver.util.ClientUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Created by dej on 2016/11/7.
 */
public class SocketService extends Service {

    private static final String TAG = "SocketService";
    private static Socket socket = null;
    private static BufferedWriter writer = null;
    private static BufferedReader reader = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return new SocketBinder();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        // 没有任何绑定时 服务销毁 断开连接
        close();
    }

    /**
     * 退出程序时关掉socke
     */
    public static void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*-------------Binder-------------*/

    public class SocketBinder extends Binder {

        public SocketService getService() {
            return SocketService.this;
        }

        public void connect(final String ipStr, final int port) {
            Log.d(TAG, "connect");
            AsyncTask<Void, String, Void> read = new AsyncTask<Void, String, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Log.d(TAG, "doInBackground");
                    try {
                        socket = new Socket();
                        SocketAddress address = new InetSocketAddress(ipStr, port);
                        socket.connect(address, 3000);
                        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (UnknownHostException e) {
                        publishProgress("{\"flag\":0,\"message\":\"@error\"}");
                        e.printStackTrace();
                        return null;
                    } catch (IOException e) {
                        publishProgress("{\"flag\":0,\"message\":\"@error\"}");
                        e.printStackTrace();
                        return null;
                    }

                    try {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            publishProgress(line);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, String.valueOf(e));
//                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(String... values) {
                    Log.d(TAG, "onProgressUpdate:" + values[0]);

                    JSONObject root = null;
                    int flag = -1;
                    try {
                        root = new JSONObject(values[0]);
                        flag = root.getInt(ClientUtil.JSON_FLAG);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // 服务器发回连接信息
                    if (flag == ClientUtil.DATA_FLAG_CONNECT || flag == ClientUtil.DATA_FLAG_REGISTER) {
                        if (connectCallBack != null) {
                            connectCallBack.onSocketMsgCallback(values[0]);
                        }
                    }

                    // 服务器发来其他客户端共享的位置信息
                    if (flag == ClientUtil.DATA_FLAG_USER) {
                        if (mapCallBack != null) {
                            mapCallBack.onMapMsgCallback(values[0]);
                        }
                    }

                    super.onProgressUpdate(values);
                }
            };
            read.execute();
        }

        /**
         * 发送消息给服务器
         *
         * @param message
         */
        public void send(String message) {
            if (writer != null) {
                try {
                    writer.write(message + "\n");
                    writer.flush();
                } catch (IOException e) {
                    Log.e(TAG, String.valueOf(e));
                }
            }
        }
    }

    /*-------------回调相关-------------*/

    /**
     * 处理连接服务器的回调
     */
    private SocketConnectCallBack connectCallBack = null;

    public SocketConnectCallBack getConnectCallBack() {
        return connectCallBack;
    }

    public void setConnectCallBack(SocketConnectCallBack connectCallBack) {
        this.connectCallBack = connectCallBack;
    }

    public interface SocketConnectCallBack {
        void onSocketMsgCallback(String msg);
    }

    /**
     * 处理定位数据的回调
     */
    private SocketMapCallBack mapCallBack = null;

    public SocketMapCallBack getMapCallBack() {
        return mapCallBack;
    }

    public void setMapCallBack(SocketMapCallBack mapCallBack) {
        this.mapCallBack = mapCallBack;
    }

    public interface SocketMapCallBack {
        void onMapMsgCallback(String msg);
    }
}
