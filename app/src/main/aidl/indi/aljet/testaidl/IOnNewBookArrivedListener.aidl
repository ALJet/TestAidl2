// IOnNewBookArrivedListener.aidl
package indi.aljet.testaidl;
import indi.aljet.testaidl.Book;

// Declare any non-default types here with import statements

interface IOnNewBookArrivedListener {

    void onNewBookArrived(in Book newBook);
}
