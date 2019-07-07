package com.mid.exporter;

import com.mid.exporter.data.common.Theme;
import com.mid.exporter.ui.error.ErrorLog;
import com.mid.exporter.util.MIDUtil;
import com.mid.exporter.util.WindowContainer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PDFExporter extends Application {
    
    private static List<String> args;
    private static Stage mainStage;
    
    @Override
    public void start(Stage primaryStage) {
	mainStage = primaryStage;
	MIDUtil.setStylesheet(Theme.BLUE.getLocation());
	WindowContainer container = MIDUtil.loadFXML(getClass().getResource("exporter-view.fxml"));
	Parent parent = container.getParent();
	parent.getStylesheets().add("/com/mid/exporter/styles.css");
	primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/pdf.png")));
	primaryStage.setTitle("Експорт в PDF");
	primaryStage.setScene(new Scene(parent));
	primaryStage.show();
	ExporterView exporterView = container.getController();
	try {
	    exporterView.load(args);
	}
	catch (SQLException ex) {
	    ErrorLog.getErrorLog().show(ex);
	}
    }

    public static Stage getMainStage() {
	return mainStage;
    }

    public static void runn() {
        
        Stage primaryStage = new Stage();
        mainStage = primaryStage;
        MIDUtil.setStylesheet(Theme.BLUE.getLocation());
        WindowContainer container = MIDUtil.loadFXML(PDFExporter.class.getResource("exporter-view.fxml"));
        Parent parent = container.getParent();
        parent.getStylesheets().add("/com/mid/exporter/styles.css");
        primaryStage.getIcons().add(new Image(PDFExporter.class.getResourceAsStream("/resources/pdf.png")));
        primaryStage.setTitle("Експорт в PDF");
        primaryStage.setScene(new Scene(parent));
        primaryStage.show();
        ExporterView exporterView = container.getController();
        try {
            exporterView.load(args);
        } catch (SQLException ex) {
            ErrorLog.getErrorLog().show(ex);
        }
    }
    
    public static void main(String[] args) {
//	args = new String[] {"-from_mid", "7", "8", "12", "13"};
	if (args.length > 0) {
	    if (args[0].equals("-from_mid")) {
		setArgs(args);
                launch(args);
	    }
	    else {
		System.exit(0);
	    }
	}
	else {
	    System.exit(0);
	}
    }
    
    private static void setArgs(String[] args) {
	String[] arr = new String[args.length-1];
	System.arraycopy(args, 1, arr, 0, arr.length);
	PDFExporter.args = Arrays.asList(arr);
    }
}