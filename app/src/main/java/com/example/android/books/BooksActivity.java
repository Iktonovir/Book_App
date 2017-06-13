package com.example.android.books;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.books.QueryUtils.LOG_TAG;

public class BooksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Books>> {

    // Declare base url as a constant
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    // Adapter for the list of books
    private BooksAdapter Adapter;

    // User's search input
    private String searchQuery;

    // TextView that is displayed when the list is empty
    private TextView emptyStateTextView;

    // Progress spinner which is displayed when data is being downloaded from the internet
    private View loadingProgressSpinner;

    /**
     * Constant value for the book loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int BOOK_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "TEST: Books Activity onCreate() called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.books_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView booksListView = (ListView) findViewById(R.id.bookList);

        emptyStateTextView = (TextView) findViewById(R.id.empty_view);
        booksListView.setEmptyView(emptyStateTextView);

        loadingProgressSpinner = findViewById(R.id.loading_spinner);
        loadingProgressSpinner.setVisibility(View.GONE);

        emptyStateTextView.setText(R.string.enter_search_query);

        // Create a new adapter that takes an empty list of books as input
        Adapter = new BooksAdapter(this, new ArrayList<Books>());

        /*
          Set the adapter on the {@link ListView} so the list can be populated in the UI
         */
        booksListView.setAdapter(Adapter);

        final EditText searchField = (EditText) findViewById(R.id.search);

        final Button button = (Button) findViewById(R.id.submit);

        // Get a reference to the LoaderManager to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        /*
         * Initialise the loader, then pass in the int ID constant defined above and pass in null
         * for the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
         * because this activity implements the LoaderCallbacks interface.
         */

        loaderManager.initLoader(BOOK_LOADER_ID, null, BooksActivity.this);

        button.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View view) {

                String searchString = searchField.getText().toString();

                searchQuery = searchString.trim();

                emptyStateTextView.setText("");
                loadingProgressSpinner.setVisibility(View.VISIBLE);

                // Get a reference to the ConnectivityManager to check state of network connectivity
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                // Get details on the current active default data network
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                // If there is a network connection, fetch the data
                if (networkInfo != null && networkInfo.isConnected()) {

                    // Restart the loader
                    getLoaderManager().restartLoader(0, null, BooksActivity.this);
                } else {
                    // display error. First, hide loading indicator so error message will be visible
                    loadingProgressSpinner.setVisibility(View.GONE);

                    // clear the adapter so user can tell if connectivity is lost between searches
                    Adapter.clear();

                    // Update empty state with no connection error message
                    emptyStateTextView.setText(R.string.no_internet_connection);
                }
            }
        });

        /*
         * Register to receive messages. We are registering an observer (MessageReceiver) to
         * receive Intents with actions named "parsing-error".
         */

        LocalBroadcastManager.getInstance(this).registerReceiver(MessageReceiver,
                new IntentFilter("parsing-error"));

    }

    @Override
    protected void onDestroy(){
        // Unregister since the activity is about to be closed
        LocalBroadcastManager.getInstance(this).unregisterReceiver(MessageReceiver);
        super.onDestroy();
    }

    @Override
    public Loader<List<Books>> onCreateLoader(int id, Bundle args) {

        return new BooksLoader(this, BASE_URL, searchQuery);
    }

    @Override
    public void onLoadFinished(Loader<List<Books>> loader, List<Books> listOfBooks) {

        // Hide progress spinner because the data has been loaded
        loadingProgressSpinner.setVisibility(View.GONE);

        // clear the adapter of previous books data
        Adapter.clear();

        /*
          If there is a valid list of {@link listOfBooks}, then add them to the adapter's data set.
          This will update the ListView.
         */
        if (listOfBooks != null && !listOfBooks.isEmpty()) {
            Adapter.addAll(listOfBooks);
        } else if (searchQuery != null)
            emptyStateTextView.setText(R.string.no_matches_found);
    }

    @Override
    public void onLoaderReset(Loader<List<Books>> loader) {
        // Loader reset, so we can clear out our existing data
        Adapter.clear();
    }

    /*
     * Our handler for received Intents. This will be called whenever an Intent with an action
     * named "parsing-error" is broadcast.
     */

    private BroadcastReceiver MessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    };
}