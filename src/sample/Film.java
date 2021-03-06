package sample;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Film {
    private int ID;
    private int indexNumber;
    private String title;
    private String director;
    private Date year;
    private byte rating;

    public Film(int ID, int indexNumber, String title, String director, Date year, byte rating) {
        this.ID = ID;
        this.indexNumber = indexNumber;
        this.title = title;
        this.director = director;
        this.year = year;
        this.rating = rating;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(int indexNumber) {
        this.indexNumber = indexNumber;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getYear() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        return df.format(year);
    }

    public void setYear(Date year) {
        this.year = year;
    }

    public byte getRating() {
        return rating;
    }

    public void setRating(byte rating) {
        this.rating = rating;
    }
}
