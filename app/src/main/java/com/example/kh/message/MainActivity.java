package com.example.kh.message;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private int randomNumber;
    private boolean mIsBound;
    private Intent intentService;
    private static final int GET_COUNT=0;
    private static final String TAG = MainActivity.class.getSimpleName();
    Messenger randomNumberRequestMessenger, randomNumberReceiveMessenger;
    class ReceiveRandomnumber extends Handler{

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            randomNumber = 0;
            switch (msg.what){
                case GET_COUNT:
                    randomNumber  =msg.arg1;
                    txtRandomnumber.setText(""+randomNumber);
                    break;
                default:break;

            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           randomNumberRequestMessenger  =new Messenger(service);
            randomNumberReceiveMessenger = new Messenger(new ReceiveRandomnumber());
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
            randomNumberReceiveMessenger=null;
            randomNumberRequestMessenger= null;
        }
    };
    @BindView(R.id.txtMessage)
    TextView txtRandomnumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        intentService = new Intent();
        intentService.setComponent(new ComponentName("com.example.kh.myapplication","com.example.kh.myapplication.Service.MyService"));
    }
    @OnClick(R.id.btnBind)
    public void btnBind(){
        Log.i(TAG, "btnBind: ");
        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    @OnClick(R.id.btnUnBind)
    public void btnUnBind(){
        Log.i(TAG, "btnUnBind:");
        if(mIsBound) {
            unbindService(serviceConnection);
            mIsBound=false;
        }
    }
    @OnClick(R.id.btngetRandomNumber)
    public void btngetRandomNumber(){
        Log.i(TAG, "btngetRandomNumber: ");
        if(mIsBound){
            Message requestMessage = Message.obtain(null, GET_COUNT);
            requestMessage.replyTo  =randomNumberReceiveMessenger;
            try {
                randomNumberRequestMessenger.send(requestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Service is unbound, can't get the random number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        serviceConnection=null;
    }
}
