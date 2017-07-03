package indi.aljet.testaidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PC-LJL on 2017/7/3.
 */

public class Book implements Parcelable {

    public int bookId;

    public String bookName;

    public Book() {
    }

    public Book(int bookId, String bookName) {
        this.bookId = bookId;
        this.bookName = bookName;
    }

    public static Creator<Book> CREATOR = new
            Creator<Book>() {
                @Override
                public Book createFromParcel(Parcel source) {
                    return new Book(source);
                }

                @Override
                public Book[] newArray(int size) {
                    return new Book[size];
                }
            };


    public Book(Parcel source){
        this.bookId = source.readInt();
        this.bookName = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookId);
        dest.writeString(bookName);
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    @Override
    public String toString() {
        return String.format("[bookId:%s,bookName:%s]",
                bookId,bookName);
    }
}
