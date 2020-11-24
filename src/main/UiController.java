/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;


import animatefx.animation.*;
import com.jfoenix.controls.JFXProgressBar;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;



/**
 *
 * @author kewlg
 */
public class UiController implements Initializable { 
    
   
    
    @FXML
    private Pane sidebar;
    @FXML
    private ProgressBar volumeBar;
    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private ImageView imageView;
    @FXML
    private Label artist;
    @FXML
    private Label title;
    @FXML
    private Label album;
    @FXML
    private Label year;
    
    static public MediaPlayerEngine media;
    @FXML
    private FontAwesomeIcon add_song;
    @FXML
    private TableView<Song> tableView;
    @FXML
    
    private Button minimizebutton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        sidebar.setVisible(false);
        
        System.out.println("Initialize 1");
       
    	media = new MediaPlayerEngine(progressBar,volumeBar,imageView); 
    	System.out.println("Initialize 2");
       
        media.setMetaDataLabels(artist, title, album, year);
        System.out.println("Initialize 3");
        
        ObservableList<Song> data = tableView.getItems();
    	tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    	data.clear();
    	tableView.setItems(media.getPlaylist().getData());
    	
    	
    	tableView.getSelectionModel().selectedItemProperty().addListener((newSelection) -> {
    	    if (newSelection != null) {
    	        //tableView.getSelectionModel().clearSelection();
    	        try{
    	        	System.out.println("Selected item: " + tableView.getSelectionModel().getSelectedIndex());
    	        	//System.out.println(""+tableView.getSelectionModel().getFocusedIndex());
    	        	System.out.println(""+tableView.getSelectionModel().getSelectedItem().getTitle());
    	        	media.play(tableView.getSelectionModel().getSelectedIndex());
    	    	}
    	        catch(Exception e)
    	        {
    	        	
    	        }
    	      }
    	});
        
        
        Image minimizeImg;
        try{
            minimizeImg=new Image(new FileInputStream("src\\main\\minimize2.png"));
            minimizebutton.setGraphic(new ImageView(minimizeImg));
        }
        catch(FileNotFoundException ex){
            System.out.println("Image not found");
        }
        
       
    }    
    
    @FXML
    private void close_app(MouseEvent event) {
        System.exit(0);
    }

    @FXML
    private void open_or_close_sidebar(MouseEvent event) {
        if(!sidebar.isVisible()){
            sidebar.setVisible(true);
            new FadeIn(sidebar).play();
        }
        else
        {
            new FadeOut(sidebar).play();
            sidebar.setVisible(false);
        }
    }

    @FXML
    private void play(MouseEvent event) {
        media.playPause();
    }

    @FXML
    private void prev(MouseEvent event) {
        media.previous();
    }

    @FXML
    private void next(MouseEvent event) {
        media.next();
    }

    @FXML
    private void close_sidebar(MouseEvent event) {
        if(sidebar.isVisible())
        {
            new FadeOut(sidebar).play();
            sidebar.setVisible(false);
        }
        else
        {
            sidebar.setVisible(true);
             new FadeIn(sidebar).play();
        }
    }

    @FXML
    private void repeat(MouseEvent event) {
           media.play(); 

    }

    @FXML
    private void shuffle(MouseEvent event) {
        
    }


    @FXML
    private void add_songs(MouseEvent event) {
        media.addSongs(Launch.stage);
    }

    @FXML
    private void handlebuttonaction(javafx.event.ActionEvent event) {
        
        Stage stage=(Stage)((Button)event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }
    
}
