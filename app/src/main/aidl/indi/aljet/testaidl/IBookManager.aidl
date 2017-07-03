// IBookManager.aidl
package indi.aljet.testaidl;
import indi.aljet.testaidl.Book;
import indi.aljet.testaidl.IOnNewBookArrivedListener;

// Declare any non-default types here with import statements

interface IBookManager {


    List<Book> getBookList();

    void addBook(in Book book);

    void registerListener(IOnNewBookArrivedListener listener);

    void unregisterListener (IOnNewBookArrivedListener listener);
}
