/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

/**
 *
 * @author ErnestChechelski
 */
//Pojedyńczy obiekt tej klasy reprezentuje jeden utwór i dane dodatkowe, artyste, album itp itd
public class Song extends Object{
    

    private final SimpleStringProperty title  = new SimpleStringProperty("");
    private final SimpleStringProperty album = new SimpleStringProperty("");
    private final SimpleStringProperty artist = new SimpleStringProperty("");
    private final SimpleStringProperty year = new SimpleStringProperty("");
    private final SimpleIntegerProperty ID = new SimpleIntegerProperty();
    private Image image = new Image(Playlist.class.getResourceAsStream("lecteur-de-musique-92113-1.png"));
    private final Media mediaFile;
    static int songsIDCounter = 0;
    
    public Media getMedia()
    {
        return this.mediaFile;
    }
    
    public Song(Media media, String fTitle, String fAlbum, String fArtist, String fYear,Image image) {
        this.setTitle(fTitle);
        this.setAlbum(fAlbum);
        this.setArtist(fArtist);
        this.setYear(fYear);
        this.mediaFile = media;
        this.ID.set(songsIDCounter);
        this.image = image;
        System.out.println("ImageHeight" + image.getHeight());
        System.out.println("Playlist 'Song Constructor': Song object created with data: " + fTitle + fAlbum + fArtist + fYear + "ID:"+songsIDCounter);
        songsIDCounter++;
    }
    
    
    public Song(Media media, String fTitle, String fAlbum, String fArtist, String fYear) {
        this.setTitle(fTitle);
        this.setAlbum(fAlbum);
        this.setArtist(fArtist);
        this.setYear(fYear);
        this.mediaFile = media;
        this.ID.set(songsIDCounter);
        System.out.println("Playlist 'Song Constructor': Song object created with data: " + fTitle + fAlbum + fArtist + fYear + "ID:"+songsIDCounter);
        songsIDCounter++;
    }
    public static void resetID()
    {
        songsIDCounter = 0;
    }
    
    public int getID() {
        return ID.get();
    }
    
    public Song copySong()
    {
       return new Song(this.getMedia(),this.getTitle(),this.getAlbum(),this.getArtist(),this.getYear());
    }
   
    public void setTitle(String fTitle) {
        title.set(fTitle);
    }
    
    public String getTitle()
    {
        return this.title.get();
    }
    
    public String getAlbum() {
        return album.get();
    }
    
    public void setAlbum(String fAlbum) {
        album.set(fAlbum);
    }
    
    public String getArtist()
    {
        return artist.get();
    }
    public void setArtist(String fArtist)
    {
        this.artist.set(fArtist);
    }
    
    public void setYear(String fYear)
    {
        this.year.set(fYear);
    }
    
    public String getYear()
    {
        return this.year.get();
    }
    
    public Image getImage() 
    {
	return image;
    }
    
    public void setImage(Image image) 
    {
	this.image = image;
    }
}
