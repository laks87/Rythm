/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;


import java.io.File;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

enum Status{
    PLAYING, // Odtwarzanie muzyki
    PAUSED, // Odtwarzanie spauzowane, ponowne odtworzenie od ostatniego miejsca w utworze
    STOPPED,// Odtwarzanie zatrzymane, ponowne odtworzenie od początku utworu
    ERROR,	// Błąd odtwarzania
    CREATED,// Obiekt klasy MediaPlayerEngine stworzony pomyślnie (Inicjalizacja wymaganego interfejsu)
    LOADED // Utwór załadowany, gotowy do odtworzenia
}


public class MediaPlayerEngine {
    
    private Playlist playlist; //Obiekt klasy Playlist przechowuje wszystkie pliki muzyczne (klasy Media), wraz z metadata (utwór, autor itp.) @see initSong(), @see Playlist.java
    private Media media;//Zmienna reprezentuje jeden plik muzyczny, w praktyce służy to wstępnej inicjalizacji obiektu MediaPlayer @see MediaPlayerEngine() @see initSong()
    private MediaPlayer mediaPlayer; //Klasa MediaPlayer służy do odtwarzania zawartości obiektów klasy Media
    
    private final IntegerProperty currentFile;//Obiekt reprezentuje numer obecnie odtwarzanego pliku. Wykorzystanie klasy IntegerProperty pozwala na zastosowanie mechanizmu słuchaczy, przez co zmiana tej wartości od razu skutkuje zmianą odtwarzanego utworu. @see createChangeListenerForCurrentFile()
    private Status mediaPlayerEngineStatus;//Reprezentuje status obiektu klasy MediaPlayerEngine @see enum Status;

    private DoubleProperty progress; //Reprezentuje obecny postęp w odtwarzaniu @see setProgressBar()
    private DoubleProperty volume; //Reprezentuje głośność odtwarzania @see setVolumeBar()
    private ProgressBar progressBar; //Reprezentuje element GUI, umożliwia sterowanie obecnie odtwarzanym czasem w utworze @see setProgressBar()
    private ProgressBar volumeBar; //Reprezentuje element GUI, sterowanie głośnością utworu @see setVolumeBar()
    
    private ImageView imageView;//Wyświetla okładkę albumu utworu, jeśli istnieje w metadata @see initSong()
    private Label artist;//Wyświetla autora utworu, jeśli istnieje w metadata @see initSong()
    private Label title;//Wyświetla tytuł utworu, jeśli istnieje w metadata @see initSong()
    private Label album;//Wyświetla tytuł albumu utworu, jeśli istnieje w metadata @see initSong() 
    private Label year;//Wyświetla rok wydania albumu utworu, jeśli istnieje w metadata @see initSong()
    
    private Boolean autoplayInternalFlag;//Flaga wykorzystywana przy metodach next i previous, @see createChangeListenerForCurrentFile()
    private Boolean autoplayExternalFlag;//Flaga wykorzystywana w momencie kiedy skończy się odtwarzany utwor  @see createChangeListenerForProgress()
    private Boolean firstMusicLoadedFlag;//Ustawienie tej flagi na false, blokuje pewne elementy interfejsu, które są niepotrzebne, dopóki nie załaduje się jakikolwiek plik z utworem
    
    
    

    public int calibrationProgressBarMulti = 200;//Stała te służy do sterowania progressBar @see setProgressBar()
    public int calibrationProgressBarBound = 150;//Stała te służy do sterowania progressBar @see setProgressBar()
   
    
    public MediaPlayerEngine(ProgressBar pb, ProgressBar vb, ImageView iv)
    {
    	//############## Inicjalizacja zmiennych wewnętrznych wymaganych do odtworzenia pojedyńczego pliku
        playlist = new Playlist();
        this.progressBar = pb;
        this.volumeBar = vb;
        setImageView(iv);
        progress = new SimpleDoubleProperty(0.0);
        progress.addListener(createChangeListenerForProgress());
        setProgressBar(pb);
        volume = new SimpleDoubleProperty(1.0);
        volume.addListener(createChangeListenerForVolume());
        setVolumeBar(vb);
        try
        {
        	 media =  new Media(MediaPlayerEngine.class.getResource("null.mp3").toURI().toString()); //Tutaj wrzucić względną ścieżkę pliku null.mp3
        }
        catch(Exception e)
        {
        	System.out.println("No file!");
        	e.printStackTrace();
        }
        initSong(media);
        currentFile = new SimpleIntegerProperty(0);
        // Do tego momentu obiekt mediaPlayer jest już gotowy. Więc klasa zareaguje na metody play(), pause(), stop()
        
        
        autoplayInternalFlag = false;//To czemuś służyło XD, przestaw by wpaść na buga przy odtwarzaniu następnych utworów
        currentFile.addListener(createChangeListenerForCurrentFile());//Nazwa metody wszystko tłumaczy
        autoplayExternalFlag = true; //Definiuje automatyczne odtwarzanie następnego utworu przy ukończeniu odtwarzania obecnego
        mediaPlayer.setOnError(createErrorHandlerForMediaPlayer());//Listener do obsłgi błędów mediaPlayer
        mediaPlayerEngineStatus = Status.CREATED;//Zmiana statusu MediaPlayerEngine
        firstMusicLoadedFlag = false;//Odblokowanie interfejsu
        //Bieżący zapis danych do wyświetlania wizualizacji
        
    }
    
    
    /*
     * @param Label artist, title, album, year
     * Setter może być wykorzystany do wymuszonego ustawienia kontrolek UI
     * */
    public void setMetaDataLabels(Label artist,Label title,Label album,Label year)
    {
    	this.artist = artist;
    	this.title = title;
    	this.album = album;
    	this.year = year;
    }
    
    /*
     * @return Status
     * Getter zwraca status obiektu klasy
     * */
    public Status getMediaPlayerEngineStatus() {
        return mediaPlayerEngineStatus;
    }
    
    /*
     * @return Playlist
     * Getter zwraca status obiektu klasy Playlist.
     * */
    public Playlist getPlaylist() {
       System.out.println("Playlist 'MediaPlayerEngine.getPlaylist': Playlist get");
       return playlist;
    }
    /*
     * @param Status
     * Setter ustawia playlistę. Można takową przygotować w zewnętrznej klasie. Jak pobrać metadatę patrz w initSong() 
     * */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }
   
    /*
     * @return Boolean
     * Getter zwraca, czy następne utwory są automatycznie odtwarzane przy zakończeniu odtwarzania obecnego
     * */
    public Boolean getAutoplayExternalFlag() {
        return autoplayExternalFlag;
    }
    
    /*
     * @param Boolean
     * Setter ustawia czy następne utwory są automatycznie odtwarzane przy zakończeniu odtwarzania obecnego
     * */
    public void setAutoplayExternalFlag(Boolean autoplayExternalFlag) {
        this.autoplayExternalFlag = autoplayExternalFlag;
    }
    
    //Mechanizm zmiany automatycznej odtwarzania obecnego utworu
    private ChangeListener<Number> createChangeListenerForProgress() 
    {
       return new ChangeListener<Number>()
       {
          @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            
                if(newValue.doubleValue()>=0.99)//Zmiana parametru 0.99 na mniejszą skutkuje wcześniejszą zmianą odtwarzania utworu na następny, wartość obliczana w procentach długości trwania utworu
                {
                    if(autoplayExternalFlag)
                    {
                       MediaPlayerEngine.this.next();
                    }
                    else
                    {
                        MediaPlayerEngine.this.stop();
                    }
                }
            
            }
       };      
    }
    //Mechanizm ogranicza zakres głośności od 0 do 1, i od razu wprowadza zmiany w głośności odtwarzania
    private ChangeListener<Number> createChangeListenerForVolume() 
    {
       return new ChangeListener<Number>()
       {
          @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            
                if(newValue.doubleValue()>1)
                {
                   volume.set(1.0);
                }
                if(newValue.doubleValue()<0)
                {
                   volume.set(0.0);
                }
                mediaPlayer.setVolume(newValue.doubleValue());
            }
       };      
    }
    
    /*
     * @param ProgressBar ustawia obecny ProgressBar do reprezentacji momentu odtwarznia obecnego utworu
     * */
    public void setProgressBar(final ProgressBar progressBar)
    {
    	//Obsługa kliknięcia w progressBar
        progressBar.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY){
                    double mouseX = event.getX();
                    progressBar.setProgress(mouseX/progressBar.getBoundsInLocal().getWidth());
                    progress.set(progressBar.getProgress());
                    if(mediaPlayer != null)
                    {
                        if(mediaPlayer.getStatus()==MediaPlayer.Status.PLAYING)
                        {
                            mediaPlayer.stop();
                            mediaPlayer.setStartTime(new Duration(mediaPlayer.getMedia().getDuration().toMillis()*progressBar.getProgress()));
                            mediaPlayer.play();
                            mediaPlayer.setStartTime(new Duration(0));
                        }
                        if(mediaPlayer.getStatus()==MediaPlayer.Status.PAUSED ||mediaPlayer.getStatus()==MediaPlayer.Status.STOPPED)
                        {
                            mediaPlayer.stop();
                            mediaPlayer.setStartTime(new Duration(mediaPlayer.getMedia().getDuration().toMillis()*progressBar.getProgress()));
                        }
                        if(mediaPlayer.getStatus()==MediaPlayer.Status.READY)
                        {
                            mediaPlayer.setStartTime(new Duration(mediaPlayer.getMedia().getDuration().toMillis()*progressBar.getProgress()));
                        }
                    } 
                   
            }
        }
        });
        System.out.println("ProgressBar Created!");
    }
    
    /*
     * @param ImageView ustawia obecny ImageView do reprezentacji okładki albumu obecnego utworu
     * 	Domyślnie pobiera plik pusheen.png
     * */
    public void setImageView(final ImageView imageView)
    {
    	this.imageView = imageView;
    	//imageView.setImage(new Image(MediaPlayerEngine.class.getResourceAsStream("pusheen.png")));
    	System.out.println("ImageView Created!");
    }
    
    /*
     * @param BarChart ustawia obecny BarChart do wyświetlania wizualizacji struktury dzwięku
     * */
   
    
    /*
     * @param ProgressBar ustawia obecny ProgressBar do reprezentacji głośności utworu
     * */
    public void setVolumeBar(final ProgressBar volumeBar)
    {
        volumeBar.setProgress(volume.getValue());
       
        volumeBar.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY)
                {
                    double mouseX = event.getX();
                    volumeBar.setProgress(mouseX/volumeBar.getBoundsInLocal().getWidth());
                    volume.set(volumeBar.getProgress());
                }
        }
        });
        System.out.println("VolumeBar Created!");
    }
    
    //Tworzy najprostszy mechanizm do wyświeltania błędów. Wyświetla w konsoli każdy błąd mediaPlayer
    private Runnable createErrorHandlerForMediaPlayer()
    {
       return new Runnable() {
       @Override
            public void run() 
            {
                final String errorMessage = media.getError().getMessage();
                // Handle errors during playback
                System.out.println("MediaPlayer Error: " + errorMessage);
            }
       };
    }
    
    
    
    private ChangeListener<Number> createChangeListenerForCurrentFile() 
    {
       return new ChangeListener<Number>()
       {
          @Override
          public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
        	 //if(currentFile.get() <= playlist.size())
        	 if(newValue.intValue()>-1)
        	 {
        		 System.out.println("autoplayInternalFlag: " + autoplayInternalFlag);
                 progressBar.setProgress(0.0);
                 progress.set(progressBar.getProgress());
                 initSong(playlist.getMedia(newValue.intValue()));
                 System.out.println("Playlist 'MediaPlayerEngine.ChangeListenerForCurrentFile': Song init, playlist.getMedia()");
                 
                 if(autoplayInternalFlag)
                 {
                     mediaPlayer.play();
                     autoplayInternalFlag = false;
                 }
             }
          }// value changed 
       };      
    }
    
    
    
    /*
     * @param Media inicjalizuje utwór czyli, wrzuca do playlisty, gdzie pobierana jest metadata, w razie potrzeby od razu odtwarza utwór
     * */
    private void initSong(Media song) 
    {
        try
        {
            try {
                if(mediaPlayer != null) {
                	mediaPlayer.stop();
                	mediaPlayer.dispose();
                }
            }
            catch(Exception e){}
            mediaPlayer = new MediaPlayer(song);
        
            mediaPlayer.setAudioSpectrumInterval(0.1);
            mediaPlayer.setOnReady(new Runnable(){
            	@Override
				public void run() {
					System.out.println("Odtwarzacz gotowy.");
				};
            
            });
           
            mediaPlayer.statusProperty().addListener(createInvalidationListenerForMediaPlayer());
            mediaPlayer.currentTimeProperty().addListener(createCurrentTimeListenerForMediaPlayer());
            mediaPlayer.setVolume(volume.doubleValue());
            try{
            	artist.setText(getCurrentSongArtist());
                title.setText(getCurrentSongTitle());
                album.setText(getCurrentSongAlbum());
                year.setText(getCurrentSongYear());
                try
                {
                	 imageView.setImage(this.getCurrentSongImage());
                }
                catch(NullPointerException e)
                {
                	//imageView.setImage(new Image(MediaPlayerEngine.class.getResourceAsStream("pusheen.png")));
                	//imageView.setImage(new Image());
                }
                
               
            }
            catch(NullPointerException e)
            {
            	artist.setText("....");
                title.setText("...");
                album.setText("..");
                year.setText(".");
            }
            
            
            mediaPlayerEngineStatus = Status.LOADED;
            System.out.println("Playlist 'MediaPlayerEngine.initSong': Song init");
            
        }
        catch(NullPointerException e)
        {
        	System.out.println("Playlist 'MediaPlayerEngine.initSong': Exception!");	
        }
    }
    
    /*
     * @param Stage, tej metodzie trzeba zapewnić obiekt klasy Stage, by mogła ona otworzyć okno do dodawania utworów
     * */
    public void addSongs(Stage stage) 
    {
        
    	try{
    		System.out.println("Current file = "+currentFile.get());
    		if(firstMusicLoadedFlag)
    		{
    			 addMusicFilesByFileChooser(stage);
    		}
    		else
    		{
    			addNextMusicFilesByFileChooser(stage);
    		}
    	}
    	catch(Exception e)
    	{
    		System.out.println("Playlist 'MediaPlayerEngine.addSongs': addMusicFilesByFileChooser run");
    	}
        System.out.println("Playlist 'MediaPlayerEngine.addSongs': addMusicFilesByFileChooser run");
        
    }
	//Dodawanie pierwszych utworów
    private void addMusicFilesByFileChooser(Stage stage) throws Exception
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP3 File","*.mp3"));//Widzi tylko .mp3
        List<File> fileList  = fileChooser.showOpenMultipleDialog(stage);
        System.out.println("Playlist 'MediaPlayerEngine.addMusicFilesByFileChooser': "+fileList.size()+ " To ilosc wszystkich plików");
        currentFile.set(0);
        System.out.println("Playlist 'MediaPlayerEngine.addMusicFilesByFileChooser': CurrentFile set to 0");
        playlist.add(fileList);
        System.out.println("Playlist 'MediaPlayerEngine.addMusicFilesByFileChooser': fileList added to playlist");
        initSong(playlist.getMedia(currentFile.get()));
        System.out.println("Playlist 'MediaPlayerEngine.addMusicFilesByFileChooser': Current song initialized from playlist getMedia");
        mediaPlayerEngineStatus = Status.LOADED;
        firstMusicLoadedFlag = true;
        System.out.println("Playlist 'MediaPlayerEngine.addMusicFilesByFileChooser': mediaPlayer status changed to LOADED");
    }
    //Dodawanie następnych utworów
    private void addNextMusicFilesByFileChooser(Stage stage) throws Exception
    {
    	
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP3 File","*.mp3"));//Widzi tylko .mp3
        List<File> fileList  = fileChooser.showOpenMultipleDialog(stage);
        System.out.println("Playlist 'MediaPlayerEngine.addNextMusicFilesByFileChooser': "+fileList.size()+ " To ilosc wszystkich plików");
        
        playlist.add(fileList);
    }
    
    
    
    
    //Kontrola statusu
    private InvalidationListener createInvalidationListenerForMediaPlayer()
    {
        return new InvalidationListener() 
	    {
                @Override
                public void invalidated(javafx.beans.Observable observable) {
                    updateStatus();
                }
	    };
        
    }
    
    //Kontrola wyświetlania progressBar
    private InvalidationListener createCurrentTimeListenerForMediaPlayer()
    {
        return new InvalidationListener() 
	    {
                @Override
                public void invalidated(javafx.beans.Observable observable) {
                    
                    if(mediaPlayer.getStatus()==MediaPlayer.Status.PLAYING || mediaPlayer.getStatus()==MediaPlayer.Status.STOPPED || mediaPlayer.getStatus()==MediaPlayer.Status.PAUSED)
                    {
                        progressBar.setProgress((mediaPlayer.getCurrentTime().toSeconds()/mediaPlayer.getMedia().getDuration().toSeconds()));
                        progress.set(progressBar.getProgress());
                    }
                }
	    };
        
    }
    
    //Tutaj są ustawiane różne statusy, podstawowa obsługa
    public void updateStatus()
    {
   	MediaPlayer.Status status = mediaPlayer.getStatus();
    	if(status == MediaPlayer.Status.PLAYING)
    	{
             mediaPlayerEngineStatus = Status.PLAYING;
        }
    	else if(status == MediaPlayer.Status.PAUSED)
    	{ 
             mediaPlayerEngineStatus = Status.PAUSED;
    	}
        else if(status == MediaPlayer.Status.STALLED)
    	{ 
             mediaPlayerEngineStatus = Status.ERROR;
             progressBar.setProgress(-1);
    	}
        else if(status == MediaPlayer.Status.UNKNOWN)
    	{ 
             mediaPlayerEngineStatus = Status.ERROR;
             progressBar.setProgress(-1);
    	}
        else if(status == MediaPlayer.Status.HALTED)
    	{ 
             mediaPlayerEngineStatus = Status.ERROR;
             progressBar.setProgress(-1);
    	}
    }
    
    //Odtwarzanie konkretnego utworu z dostępnych
    public void play(int index)
    {
    	if(index <= this.getPlaylist().size()-1)
    	this.autoplayInternalFlag = true;
    	this.currentFile.set(index);
    }
    //Odtwarzanie obecnego utworu @see this.currentFile
    public void play()
    {
        
       System.out.println("Play: MediaPlayerStatus" + mediaPlayer.getStatus().toString());
       if(mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) 
       {
           mediaPlayer.setStartTime(mediaPlayer.getCurrentTime());
           mediaPlayer.play();
       }
       else{
    	   try
    	   {
    		   initSong(playlist.getMedia(currentFile.get()));
    		   mediaPlayer.play();  
    	   }
    	   catch(IndexOutOfBoundsException e)
    	   {
    		   System.out.println("Error with play()");
    	   }
    	   
       }
       
    }
    
    //po prostu pauza :D
    public void pause()
    {
        mediaPlayer.pause();
    }
    
    //play/pause, w zależności co jest obecnie, zmiana między tymi statusami
    public void playPause()
    {
       try
       {
            if(mediaPlayer.getStatus()==MediaPlayer.Status.PLAYING)
            {
                mediaPlayer.pause();
            }
            else
            {
                this.play();
            }
       }
        catch(MediaException e1)
        {
             mediaPlayerEngineStatus = Status.ERROR;    	 
        }
    }
    
    //Po prostu stop :D
    public void stop()
    {
        mediaPlayer.setStartTime(new Duration(0));
        mediaPlayer.stop();
    }
    
    
    //Odtwarza następny plik, jeśli dostępny
    public void next()
    {
    	if(currentFile.get() < (playlist.size()-1))
    	{
    		if(mediaPlayer.getStatus()==MediaPlayer.Status.PLAYING)
    		{
    			System.out.println("autoplayInternalFlag = true");
    			autoplayInternalFlag = true;
    		}
    		mediaPlayer.setStartTime(new Duration(0));
    		currentFile.set(currentFile.get() + 1);
    	}
    }
    
    //Odtwarza poprzedni plik, jeśli dostępny
    public void previous()
    {
    	if(currentFile.get() > 0)
    	{
    		if(mediaPlayer.getStatus()==MediaPlayer.Status.PLAYING)
            {
                autoplayInternalFlag = true;
            }
            mediaPlayer.setStartTime(new Duration(0));
            currentFile.set(currentFile.get() - 1);
    	}
    }
    
  //####################
    //Gettery i settery, działanie maksymalnie oczywiste
    public String getCurrentSongTitle()
    {
        return playlist.getData().get(this.currentFile.get()).getTitle();
    }       
    public String getCurrentSongAlbum()
    {
        return playlist.getData().get(this.currentFile.get()).getAlbum();
    }       
    public String getCurrentSongArtist()
    {
        return playlist.getData().get(this.currentFile.get()).getArtist();
    }  
    public String getCurrentSongYear()
    {
        return playlist.getData().get(this.currentFile.get()).getYear();
    }  
    public Image getCurrentSongImage()
    {
        return playlist.getData().get(this.currentFile.get()).getImage();
    }  
    
}
