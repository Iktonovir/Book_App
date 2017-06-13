package com.example.android.books;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class BooksAdapter extends ArrayAdapter<Books> {

    /**
     * constructs a new {@link BooksAdapter}
     *
     * @param context of the app
     * @param books   is the list of books, which is the data source of the adapter
     */
    public BooksAdapter(Context context, List<Books> books) {
        super(context, 0, books);
    }

    // view lookup cache
    private static class ViewHolder {
        public ImageView book_picture;
        public TextView title;
        public TextView author;
    }

    /*
     * Returns a list item view that displays information about the book at the given position
     * in the list of books.
     */

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        // Get the data item for this position.
        Books currentBook = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view.
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.books_list_item, parent, false);

            // Find and display the book cover
            viewHolder.book_picture = (ImageView) convertView.findViewById(R.id.book_picture);

            // Find and display the title
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);

            // Find and display the author
            viewHolder.author = (TextView) convertView.findViewById(R.id.author);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        // Populate the data into the template view using the data object.
        if (currentBook != null) {
            viewHolder.title.setText(currentBook.getTitle());
        }
        if (currentBook != null) {
            viewHolder.author.setText(currentBook.getAuthor());
        }

        // Return the completed view to render on screen.
        return convertView;
    }
}