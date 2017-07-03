package indi.aljet.testaidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.MissingResourceException;

import indi.aljet.testaidl.service.BookManagerService;

public class MainActivity extends AppCompatActivity {

    private Button btn;

    private static final String TAG = "MainActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private IBookManager mRemoteBookManager;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.d(TAG,"receive new book :"+msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
//            super.handleMessage(msg);
        }
    };


    private IBinder.DeathRecipient mDeathReathRecipient = new
            IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Log.d(TAG,"binder died.tanme:"+
                    Thread.currentThread().getName());
                    if(mRemoteBookManager == null){
                        return;
                    }
                    mRemoteBookManager.asBinder()
                            .unlinkToDeath(mDeathReathRecipient,0);
                    mRemoteBookManager = null;
                }
            };


    private ServiceConnection mConnection = new
            ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IBookManager bookManager = IBookManager
                            .Stub.asInterface(service);
                    mRemoteBookManager = bookManager;
                    try{
                        mRemoteBookManager.asBinder().linkToDeath
                                (mDeathReathRecipient,0);
                        List<Book> list = bookManager
                                .getBookList();
                        Log.i(TAG,"query book list,list type:"+list
                        .getClass().getCanonicalName());
                        Book newBook = new Book(3,"Android进阶");
                        bookManager.addBook(newBook);
                        Log.i(TAG,"add Book :"+newBook);
                        List<Book> newList = bookManager
                                .getBookList();
                        Log.i(TAG,"query book list:"+
                        newList.toString());
                        bookManager.registerListener(
                                mOnNewBookArrivedListener);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG,"onServiceDisconnected.name:"
                    + Thread.currentThread()
                    .getName());
                }
            };


    private IOnNewBookArrivedListener mOnNewBookArrivedListener
            = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED,
                    newBook).sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button)this.findViewById(R.id.btn_click);
        Intent intent = new Intent(this,
                BookManagerService.class);
        bindService(intent,mConnection, Context
        .BIND_AUTO_CREATE);
    }

    public void onButton1Click(View view){
        Toast.makeText(this, "click button1", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mRemoteBookManager != null){
                    try{
                        List<Book> newList =
                                mRemoteBookManager.
                                        getBookList();
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if(mRemoteBookManager != null
                && mRemoteBookManager.asBinder()
                .isBinderAlive()) {
            try {
                Log.i(TAG, "unregister listener:"
                        + mOnNewBookArrivedListener);
                mRemoteBookManager.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
            unbindService(mConnection);

        super.onDestroy();
    }
}
