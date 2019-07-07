package com.mid.exporter.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class PDFBuilder {

    PDDocument document;
    BufferedImage bufferedImage = new BufferedImage(595, 842, BufferedImage.TYPE_INT_RGB);
    
    public PDFBuilder() {
	document = new PDDocument();
    }
    
    public void addPage(Image image) throws IOException {
	PDPage page = new PDPage(PDRectangle.A4);
	document.addPage(page);
	page.setMediaBox(new PDRectangle(0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
	PDImageXObject pdImage = LosslessFactory.createFromImage(document, SwingFXUtils.fromFXImage(image, bufferedImage));
	try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
	    stream.drawImage(pdImage, 0, 0);
	}
    }
    
    public void savePDF(String path) throws IOException {
	document.save(path);
	document.close();
    }
}