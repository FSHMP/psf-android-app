package org.psfcerd.blog.database;

/**
 * Created by pras on 7/3/15.
 */
public class PostEntry {
    String _title;
    String _url;
    String _description;
    String _published_date;
    String _html_file_location;

    public PostEntry() {}

    public PostEntry (String title, String url, String desc, String pub_date, String html_file_loc) {
        this._title = title;
        this._url = url;
        this._description = desc;
        this._published_date = pub_date;
        this._html_file_location = html_file_loc;
    }

    // Getter Setter methods for TITLE
    public String getTitle(){
        return this._title;
    }

    public void setTitle(String title){
        this._title = title;
    }

    // Getter Setter methods for URL
    public String getUrl(){
        return this._url;
    }

    public void setUrl(String url){
        this._url = url;
    }

    // Getter Setter methods for DESCRIPTION
    public String getDescription(){
        return this._description;
    }

    public void setDescription(String desc){
        this._description = desc;
    }

    // Getter Setter methods for PUBLISHED DATE
    public String getPublishedDate(){
        return this._published_date;
    }

    public void setPublishedDate(String pub_date){
        this._published_date = pub_date;
    }

    // Getter Setter methods for html_file_location
    public String getHtmlFileLocation(){
        return this._html_file_location;
    }

    public void setHtmlFileLocation(String file_location){
        this._html_file_location = file_location;
    }
}