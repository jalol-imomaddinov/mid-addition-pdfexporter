package com.mid.exporter;

import com.mid.exporter.data.database.DataAccessHelper;
import com.mid.exporter.data.database.ContractDAO;
import com.mid.exporter.data.model.FullContractModel;
import com.mid.exporter.preview.PreviewController;

import com.jfoenix.controls.JFXProgressBar;
import com.mid.exporter.pdf.PDFBuilder;
import com.mid.exporter.ui.error.ErrorLog;
import java.io.File;
import java.io.IOException;

import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.SnapshotParameters;
import javafx.event.ActionEvent;
import javafx.fxml.*;

import java.util.*;
import java.net.URL;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.stage.FileChooser;

public class ExporterView implements Initializable {

    private final ArrayList<Image> images = new ArrayList<>();

    @FXML
    private AnchorPane rootPane;
    @FXML
    private ScrollPane container;
    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private ImageView imageViewer;
    @FXML
    private Label pageIndicator;
    @FXML
    private StackPane progressPane;
    
    private PreviewController preview;
    
    private int currentPage = 0;
    private int pageCount = 0;
    
    private double persentage = 1;
    
    private final FileChooser chooser = new FileChooser();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
	preview = PreviewController.newPreview();
	imageViewer.imageProperty().addListener((observable, oldValue, newValue) -> {
	    persentage = 1;
	    changeSizes();
	});

	container.widthProperty().addListener((observable, oldValue, newValue) -> {
	    changeSizes();
	});

	container.heightProperty().addListener((observable, oldValue, newValue) -> {
	    changeSizes();
	});
	
	chooser.setTitle("Export PDF");
	chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
    }
    
    public void load(List<String> indexes) throws SQLException {
	showProgress();
	ContractDAO contractDAO = DataAccessHelper.getDataAccessHelper().getContractDAO();
	double d = 1.0 / (double)indexes.size();
	pageCount = indexes.size();
	progressBar.setProgress(0);
	new Thread(() -> {
	    try {
		for (String index : indexes) {
		    FullContractModel model = new FullContractModel();
		    contractDAO.readFullContract(Integer.parseInt(index), model);
		    Platform.runLater(()-> {
			try {
			    if (preview.loadModel(model)) {
                		images.add(preview.getPrintable().snapshot(new SnapshotParameters(), new WritableImage(595, 842)));
                            }
			    updateProgress(d);
			}
			catch (SQLException ex) {
			    ErrorLog.getErrorLog().show(ex);
			}
		    });
		}
		Platform.runLater(()-> {
		    updatePageIndicator();
		    if (images.size() > 0) {
			setImage(images.get(0));
		    }
		    hideProgress();
		});
	    }
	    catch (SQLException ex) {
		ErrorLog.getErrorLog().show(ex);
	    }
	}).start();
    }

    private void updateProgress(double v) {
	double p = progressBar.getProgress() + v;
	if(p >= 1.00) {
	    p = 1;
	}
	progressBar.setProgress(p);
    }
    
    private void updatePageIndicator() {
	String str;
	if (pageCount == 0) {
	    str = "(пусто)";
	}
	else {
	    str = (currentPage + 1) + "/" + pageCount;
	}
	pageIndicator.setText(str);
    }
    
    private void showProgress() {
	rootPane.setOpacity(0.25);
	progressPane.setVisible(true);
    }
    
    private void hideProgress() {
	rootPane.setOpacity(1);
	progressPane.setVisible(false);
    }
    
    @FXML
    private void zoomIn(ActionEvent event) {
	persentage += 0.1;
	changeSizes();
    }

    @FXML
    private void zoomOut(ActionEvent event) {
	persentage -= 0.1;
	if (persentage <= 0.1) {
	    persentage = 0.1;
	}
	changeSizes();
    }

    @FXML
    private void prevPage(ActionEvent event) {
	if (currentPage-1 < 0) {
	    return;
	}
	currentPage--;
	setImage(images.get(currentPage));
	updatePageIndicator();
    }

    @FXML
    private void nextPage(ActionEvent event) {
	if (currentPage + 1 >= images.size()) {
	    return;
	}
	currentPage++;
	setImage(images.get(currentPage));
	updatePageIndicator();
    }

    private void setImage(Image img) {
	persentage = 1;
	imageViewer.setImage(img);
    }
    
    @FXML
    private void savePDF(ActionEvent event) {
	try {
	    PDFBuilder builder = new PDFBuilder();
	    for (Image image : images) {
		builder.addPage(image);
	    }
	    File file = chooser.showSaveDialog(PDFExporter.getMainStage());
	    if (file.exists()) file.delete();
	    builder.savePDF(file.getAbsolutePath());
	}
	catch (IOException ex) {
	    ErrorLog.getErrorLog().show(ex);
	}
    }

    private double getWidth() {
	return container.getWidth();
    }

    private double getHeight() {
	return container.getHeight();
    }
    
    private void changeSizes() {
	imageViewer.setFitWidth(getWidth() * persentage);
	imageViewer.setFitHeight(getHeight() * persentage);
	container.setVvalue(0.5);
	container.setHvalue(0.5);
    }
}