package application.log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class Report {
	private Document document = new Document();
	private String name;
	
	
	private final Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,Font.BOLD);
	private final Font subTitleFont = new Font(Font.FontFamily.TIMES_ROMAN, 14);
	private final Font sentenceFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,Font.ITALIC);

	private final Font textFont = new Font(Font.FontFamily.TIMES_ROMAN, 10);

	public Report(String name){
		this.name=name;
	} 
	
	public void open(){
        try {
            PdfWriter.getInstance(document,new FileOutputStream(name));
            document.open();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}
	
	public void close(){
		document.close();
	}
	
	public void addTitle(String title){
		Paragraph paragraph=new Paragraph("Tele Assistance System",titleFont);
		paragraph.setAlignment(Element.ALIGN_CENTER);

        try {
			document.add(paragraph);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public void addSubTitle(String subTitle){
		Paragraph paragraph=new Paragraph("Diagnosis Report",subTitleFont);
		paragraph.setAlignment(Element.ALIGN_CENTER);

        try {
			document.add(paragraph);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public void addSentence(String sentence){
		Paragraph paragraph=new Paragraph(sentence,sentenceFont);
		paragraph.setIndentationLeft(50);
		//paragraph.setAlignment(Element.ALIGN_CENTER);

        try {
			document.add(paragraph);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	} 
	
	public void addTable(String[] columns, float[] columnWidths, List<String> values){
		int columnNum=columns.length;
	    PdfPTable table = new PdfPTable(columnNum);
	    
	    try {
			table.setWidths(columnWidths);
		} catch (DocumentException e1) {
			e1.printStackTrace();
		}
	    
	    for(String column:columns){
	        PdfPCell cell = new PdfPCell(new Phrase(column,textFont));
	        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        table.addCell(cell);
	    }
	    
	    for(String value:values){
	        PdfPCell cell = new PdfPCell(new Phrase(value,textFont));
	        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        table.addCell(cell);
	    }
	    
	    try {
			document.add(table);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	 
    public void addEmptyLine(int number) {
        try {
        	for (int i = 0; i < number; i++) {
    			document.add(new Paragraph(" "));
        	}
		} catch (DocumentException e) {
			e.printStackTrace();
        }
    }
	
	
	

}
