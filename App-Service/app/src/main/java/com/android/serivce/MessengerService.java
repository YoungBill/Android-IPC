package com.android.serivce;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by baina on 18-2-1.
 * 以Messenger的形式进程跨进程通信
 */

public class MessengerService extends Service {

    private static final String TAG = MessengerService.class.getSimpleName();

    private Messenger mMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String clientMessage = null;
            if (bundle != null) {
                clientMessage = bundle.getString("content");
            }
            //回传
            Message serviceMessage = Message.obtain();
            Bundle serviceBundle = new Bundle();
            serviceBundle.putString("content", "收到客户端发来的消息:" + clientMessage + "\n这是服务端回传的消息．当前线程:" + Thread.currentThread().getName());
            serviceBundle.putParcelable("person", new Person("小王子", 18));
            serviceMessage.setData(serviceBundle);
            try {
                msg.replyTo.send(serviceMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "绑定成功!");
        return mMessenger.getBinder();
    }
}
