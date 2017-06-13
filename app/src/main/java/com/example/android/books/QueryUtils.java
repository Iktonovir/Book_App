package com.example.android.books;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


// Utility class with methods to help perform the HTTP request and parse the response
public final class QueryUtils {

    // Tag for the log messages
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private static Context context;

    // Constant keys
    private static final String KEY_ITEMS = "items";
    private static final String KEY_VOLUME_INFO = "volumeInfo";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHORS = "authors";

    // Returns new URL object from the given string URL
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    // Make an HTTP request to the given URL and return a String as the response.
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /*milliseconds*/);
            urlConnection.setConnectTimeout(15000 /*milliseconds*/);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            /*
             * If the request was successful (response code 200), then read the input stream and
             * parse the response.
             */
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null)
                inputStream.close();
        }

        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the whole JSON response from the
     * server.
     *
     * @param inputStream is the JSON response
     * @return output.toString()
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Books} objects that have been built up from parsing a JSON response.
     */
    private static List<Books> extractFeatureFromJson(String bookJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON))
            return null;

        //Create an empty ArrayList that we can start adding books to.
        List<Books> books = new ArrayList<>();

        /*
         * Try to parse the JSON response. If there's a problem with the way the JSON is formatted,
         * a JSONException exception object will be thrown. Catch the exception so the app doesn't
         * crash, and print the error message to the logs.
         */

        try {
            // Create a JSONObject from the JSON response string.
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            /*
             * Extract the JSONArray associated with the key called "items", which represents
             * a list of features (or books)
             */
            JSONArray bookArray = baseJsonResponse.getJSONArray(KEY_ITEMS);

            /*
              for each book in the bookArray, create an {@link Books} object.
             */
            for (int i = 0; i < bookArray.length(); i++) {

                // Get book JSONObject at position i
                JSONObject currentBook = bookArray.getJSONObject(i);

                /*
                 * For a given book, extract the JSONObjects associated with the key
                 * "volumeInfo", which represents a list of information for that book
                 */
                JSONObject volumeInfo = currentBook.getJSONObject(KEY_VOLUME_INFO);

                // Extract "title" under "volumeInfo" for title
                String title = "";
                if (volumeInfo.has(KEY_TITLE)) {
                    title = volumeInfo.getString(KEY_TITLE);
                }else
                    sendTitleMessage();

                // Extract "authors" under "volumeInfo" for author
                String authors = "";
                if (volumeInfo.has(KEY_AUTHORS)) {
                    JSONArray authorsArray = volumeInfo.getJSONArray(KEY_AUTHORS);
                    authors = authorsArray.toString().replace("[", "").replace("]", "");
                }else
                    sendAuthorsMessage();

                /*
                 * Create a new {@link Books} object with the title and author from the JSON response.
                 */
                Books book = new Books(title, authors);

                // Add book to list of books
                books.add(book);

            }

        } catch (JSONException e) {
            /*
             * If an error is thrown when executing any of the above statements in the "try" block,
             * catch the exception here, so the app doesn't crash. Print a log message with the
             * message from the exception.
             */
            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        }

        // Return the list of books
        return books;
    }

    /**
     * Query the Google dataset and return a list of {@link Books} objects.
     *
     * @param requestUrl is the URL we are requesting data from
     * @return books
     */
    public static List<Books> fetchBookData(String requestUrl) {

        Log.i(LOG_TAG, "TEST: fetchBookData() called...");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create URL object
        URL url = createUrl(requestUrl);

        //Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        /*
         *Extract relevant fields from the JSON response and create a list of {@link Books}.
         *Return the list of {@link Books}.
         */
        return extractFeatureFromJson(jsonResponse);
    }

    public static void init(Context context){
        QueryUtils.context = context;
    }

    /*
     * Send an Intent with an action named "parsing-error". The intent should be received by
     * the Receiver Activity (BooksActivity)
     */
    private static void sendTitleMessage(){

        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("parsing-error");
        intent.putExtra("message", "Could not display book title. Possible that field does not exist");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private static void sendAuthorsMessage(){

        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("parsing-error");
        intent.putExtra("message", "Could not display book authors. Possible that field does not exist");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }
}