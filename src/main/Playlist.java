/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


/**
 * 
 * @author ErnestChechelski
 */


//niby implementuje Serializable, ale jeszcze nie dokończone
public class Playlist implements Serializable{
  
    private static final long serialVersionUID = 1L;
    private static final ArrayList<Media> mediaList = new ArrayList<Media>();//Lista obiektów typu media, jeden obiekt, jeden utwór
    private static final ArrayList<Song> metadataList = new ArrayList<Song>();//Lista obiektów typu song, jeden obiekt, jeden utwór, ale zawiera też dane metadata
    private static int contentCounter;//Każdy następny utwór ma swoje ID, ta zmienna zlicza ile utwórów jest w pojedyńczej playliście
    private static final ObservableList<Song> data = FXCollections.observableArrayList();
    private static boolean ready = false;
    
    
    Playlist()
    {
        contentCounter = 0;
    }
    
    public void mutate()
    {
        java.util.Collections.shuffle(data);
    }
   
    public ArrayList<Song> getMetaDataList()
    {
        return Playlist.metadataList;
    }
    
    public ObservableList<Song> getData()
    {
        System.out.println(Playlist.data);
        return Playlist.data;
    }
    
    public ArrayList<Media> getMediaList()
    {
        return Playlist.mediaList;
    }
    
    
    
   
    
    public Media getMedia(int index)
    {
        return this.getData().get(index).getMedia();
    }
    
   
    
    public int size()
    {
        return contentCounter;
    }
    
    public void add(List<File> fileList)
    {
        for (File fileList1 : fileList) {
            URI filepath = fileList1.toURI();
            add(filepath);
        }
    }
    
    
    //W tej metodzie następuje odczytanie metadaty 
    public void add(URI filepath)
    {
        Media media = new Media(filepath.toString());
        MediaPlayer mediaplayer = new MediaPlayer(media);
        
        
        mediaplayer.setOnReady(new Runnable(){

			@Override
			public void run() {
                        String album;
		        String artist;
		        String year;
		        String title;
		        Image image;
		        try
		           {
		              album = media.getMetadata().get("album").toString();
		           }
		           catch(NullPointerException e)
		           {
		               album = "-";
		           }
		           try
		           {
		               artist = media.getMetadata().get("artist").toString();
		           }
		           catch(NullPointerException e)
		           {
		               artist = "-";
		           }
		           
		           try
		           {
		               title = media.getMetadata().get("title").toString();
		           }
		           catch(NullPointerException e)
		           {
		               title = "-"; 
		           }
		           
		           try
		           {
		              year = media.getMetadata().get("year").toString();
		           }
		           catch(NullPointerException e)
		           {
		               year = "-";
		           }
		           try
		           {
		              Object object = media.getMetadata().get("image");
		              System.out.println(media.getMetadata());
		              if(object instanceof Image)
		              {
		            	  image = (Image) object;
		            	  System.out.println("Song image loaded from metadata");
		              }
		              else
		              {
		            	  System.out.println("Info: " + object.getClass().toString());
		            	  System.out.println("Song image loaded but corrupted");
		            	  
		            	  System.out.println("Gówno lol");
		            	  throw new NullPointerException();
		              }
		              
		             
		           }
		           catch(NullPointerException e)
		           {
		              try {
						image = new Image(Playlist.class.getResource("lecteur-de-musique-92113-1.png").toURI().toString());
						System.out.println("Song image loaded from default");
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						System.out.println("Null song image");
						image = null;
					}
		           }
		        
		        System.out.println("Raport!: "+ title + artist + year + album);
		        final Song song = new Song(media,title,album,artist,year,image);
		        data.add(song);
		        System.out.println("Playlist 'Playlist.add().onReadyListener': data.add(song)");
		        System.out.println("Playlist 'Playlist.add().onReadyListener': media added to data");
		    }
			
        });
        
        mediaList.add(media);
        System.out.println("Playlist 'Playlist.add()': media added to medialist");
        contentCounter++;
    }   
    
    public synchronized static void invertReady()
    {
    	ready = !ready;
    }
    
    public void add(String filepath) throws URISyntaxException
    {
        add(new URI(filepath));
    }   
    
    public void remove(int index) throws ArrayIndexOutOfBoundsException
    {
        mediaList.remove(index);
        contentCounter--;
    }
     
}
