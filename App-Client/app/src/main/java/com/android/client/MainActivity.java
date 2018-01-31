package com.android.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
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
    private ServiceConnection mConnection = new ServiceConnection() {
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

    private int mMinAge = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //本项目其他进程
//        Intent intent = new Intent(getApplicationContext(), MyAidlService.class);
//        bindService(intent, mConnection, BIND_AUTO_CREATE);
        Intent intent = new Intent();
        intent.setClassName("com.android.serivce", "com.android.serivce.MyAidlService");
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    public void OnClick(View view) {
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
    }
}
