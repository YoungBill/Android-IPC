package com.example.chentao.aidldemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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

        Intent intent = new Intent(getApplicationContext(), MyAidlService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    public void OnClick(View view) {
        try {
            mAidl.addPerson(new Person("Tom", mMinAge++));
            String toastString = "There are " + mAidl.getPersonList().size() + " people at present, and the last one is " + mAidl.getPersonList().get(mAidl.getPersonList().size() - 1).getAge() + " .";
            Toast.makeText(MainActivity.this, toastString, Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
