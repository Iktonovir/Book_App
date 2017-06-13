package com.example.android.books;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Loads a list of books by using an AsyncTask to perform the network request to the given URL.
 */
public class BooksLoader extends AsyncTaskLoader<List<Books>> {

    /** Tag for log messages */
    private static final String LOG_TAG = BooksLoader.class.getName();

    /** Query URL */
    private String Url;

    /** Search Query String */
    private String SearchQuery;

    /**
     * Constructs a new {@link BooksLoader}
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public BooksLoader(Context context, String url, String searchQuery) {
        super(context);
        Url = url;
        SearchQuery = searchQuery;
    }

    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "TEST: onStartLoading() called...");
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Books> loadInBackground() {
        Log.i(LOG_TAG, "TEST: loadInBackground() called...");

        if (Url == null || SearchQuery == null)
            return null;

        // Perform the network request, parse the response, and extract a list of books.
        return QueryUtils.fetchBookData(Url + SearchQuery);
    }
}
