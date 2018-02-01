package com.android.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.serivce.IMyAidlInterface;
import com.android.serivce.Person;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private IMyAidlInterface mAidl;
    private Messenger mMessenger;
    private ServiceConnection mAIDLConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //连接后拿到 Binder，转换成 AIDL，在不同进程会返回个代理
            mAidl = IMyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAidl = null;
        }
    };
    private ServiceConnection mMessengerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private Messenger mReplyMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            /***************************important***********************/
            bundle.setClassLoader(getClass().getClassLoader());
            /***************************important***********************/
            String clientMessage = null;
            String name = null;
            if (bundle != null) {
                clientMessage = bundle.getString("content");
                Person person = bundle.getParcelable("person");
                name = person.getName();
            }
            Toast.makeText(MainActivity.this, "收到服务端返回：\n" + clientMessage + "\nPersonName：" + name, Toast.LENGTH_SHORT).show();
        }
    });

    private int mMinAge = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /***************************AIDL***********************/
        Intent aidlIntent = new Intent();
        aidlIntent.setClassName("com.android.serivce", "com.android.serivce.MyAidlService");
        bindService(aidlIntent, mAIDLConnection, BIND_AUTO_CREATE);
        /***************************AIDL***********************/

        /***************************Messenger***********************/
        Intent mMessengerIntent = new Intent();
        mMessengerIntent.setClassName("com.android.serivce", "com.android.serivce.MessengerService");
        bindService(mMessengerIntent, mMessengerServiceConnection, BIND_AUTO_CREATE);
        /***************************Messenger***********************/
    }

    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.addPersonByAIDLBt:
                try {
                    if (mAidl == null) {
                        Log.e(TAG, "bindService failed");
                        return;
                    }
                    mAidl.addPerson(new Person("hh", mMinAge++));
                    String toastString = "There are " + mAidl.getPersonList().size() + " people at present, and the last one is " + mAidl.getPersonList().get(mAidl.getPersonList().size() - 1).getAge() + " .";
                    Toast.makeText(MainActivity.this, toastString, Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.sendMessageByMessengerBt:
                if (mMessenger == null) {
                    Log.e(TAG, "bindService failed");
                    return;
                }
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("content", "哈哈，我是客户端!");
                message.setData(bundle);
                message.replyTo = mReplyMessenger;
                try {
                    mMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }

    }
}
