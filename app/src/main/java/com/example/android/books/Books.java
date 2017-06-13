package com.example.android.books;

/**
 * An {@link Books} object contains information related to a single book.
 */
public class Books {

    // Title of the book
    private String Title;

    // Author of the book
    private String Author;

    /**
     * Constructs a new {@link Books} object.
     *
     * @param title is the title of the book.
     * @param author is the person/people who wrote the book.
     */

    public Books(String title, String author) {
        Title = title;
        Author = author;
    }

    /**
     * Returns the title of the book.
     */

    public String getTitle() {
        return Title;
    }

    /**
     * Returns the author of the book.
     */

    public String getAuthor() {
        return Author;
    }
}
