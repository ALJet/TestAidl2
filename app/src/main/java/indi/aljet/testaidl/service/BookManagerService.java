package indi.aljet.testaidl.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import indi.aljet.testaidl.Book;
import indi.aljet.testaidl.IBookManager;
import indi.aljet.testaidl.IOnNewBookArrivedListener;

/**
 * Created by PC-LJL on 2017/7/3.
 */

public class BookManagerService extends Service {

    private static final String TAG = "BMS";

    private AtomicBoolean mIsServiceDestoryed =
            new AtomicBoolean(false);

    private CopyOnWriteArrayList<Book> mBookList =
            new CopyOnWriteArrayList<Book>();

    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList =
            new RemoteCallbackList<IOnNewBookArrivedListener>();


    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            SystemClock.sleep(5000);
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
            final int N = mListenerList
                    .beginBroadcast();
            mListenerList.finishBroadcast();
            Log.d(TAG,"registerListener,current size:"+N);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            boolean success = mListenerList
                    .unregister(listener);
            if(success){
                Log.d(TAG,"unregister success.");
            }else{
                Log.d(TAG,"not found,can not unregister.");
            }
            final int N = mListenerList
                    .beginBroadcast();
            mListenerList.finishBroadcast();
            Log.d(TAG,"unregisterListener,current size:"+ N);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1,"Android"));
        mBookList.add(new Book(2,"Ios"));
        new Thread(new ServiceWorker()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission("indi.aljet.testaidl.permission.ACCESS_BOOK_SERVICE");
        Log.d(TAG, "onbind check=" + check);
        if (check == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return mBinder;
    }


    @Override
    public void onDestroy() {
        mIsServiceDestoryed.set(true);
        super.onDestroy();
    }

    private void onNewBookArrived(Book book)throws RemoteException{
        mBookList.add(book);
        final int N = mListenerList.beginBroadcast();
        for(int i = 0;i < N;i++){
            IOnNewBookArrivedListener l =
                    mListenerList.getBroadcastItem(i);
            if(l != null){
                try{
                    l.onNewBookArrived(book);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }




    private class ServiceWorker extends Thread{
        @Override
        public void run() {
            while (!mIsServiceDestoryed.get()){
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                int bookId = mBookList
                        .size() + 1;
                Book newBook = new Book(bookId,
                        "new Book#"+bookId);
                try{
                    onNewBookArrived(newBook);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
