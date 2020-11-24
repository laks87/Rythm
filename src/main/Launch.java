/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author kewlg
//// */
public class Launch extends Application {
    public static Stage stage;
    private double xoffset=0;
    private double yoffset=0;
    @Override
    public void start(Stage  stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Ui.fxml"));
        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        
        
       
        
        root.setOnMousePressed(new EventHandler<MouseEvent>()
        {
             @Override
             public void handle(MouseEvent event)
             {
                 xoffset=event.getSceneX();
                 yoffset=event.getSceneY();   
             }
        });
        
         root.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
             @Override
             public void handle(MouseEvent event)
             {
                 stage.setX(event.getScreenX() - xoffset);
                 stage.setY(event.getScreenY() - yoffset);
             }
        });
         
         
         Image image=new Image("/main/logo.jpg");
        stage.getIcons().add(image);
        stage.setTitle("RYTHM");
         
          stage.setScene(scene);
        Launch.stage=stage;
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
