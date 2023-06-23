package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Converter {
    public String getTxtFromFile(File file){
        if (file == null) return null;
        String ret;
        try {
            Charset charset = detectEncoding(file);
            if (file.getName().endsWith(".docx")) {
                ret = getTextFromDocx(file);
            } else if (file.getName().endsWith(".pdf")) {
                ret = getTextFromPdf(file);

            } else if (file.getName().endsWith(".doc")) {
                ret = getTextFromDoc(file);
            }
            else if (file.getName().endsWith(".rtf")) {
                ret = getTextFromRtf(file,charset);
            }
            else {
                ret = getText(file);
            }
            ret = checkEnc(ret);
            String delete = ApplicationConfiguration.getInstance().getProperty("dev.delete_mosgorsud_docs");
            if (delete == null || delete.equals("true") || delete.equals("")) {
                Files.delete(file.toPath());
            }
            return ret;
        } catch (Exception e) {
            SimpleLogger.log(LoggingLevel.DEBUG, "During parsing occurred exception: " + e);
        }
        return null;
    }

    private String checkEnc(String ret) {
        if (ret == null) return null;
        if (ret.contains("Å") && ret.contains("È")) {
            try {
                return new String(ret.getBytes("Windows-1252"), "Windows-1251");
            } catch (UnsupportedEncodingException ignored) {}
        }
        return ret;
    }

    private String getTextFromRtf(File file, Charset charset) {
        try {
            FileInputStream stream = new FileInputStream(file);
            RTFEditorKit kit = new RTFEditorKit();
            Document doc = kit.createDefaultDocument();
            Reader reader = new InputStreamReader(stream, charset);
            kit.read(reader, doc, 0);

            return doc.getText(0, doc.getLength());
        } catch (IOException | BadLocationException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.CONVERSION_FAILED +" .rtf");
            return null;
        }
    }

    public Charset detectEncoding(File file) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(bis);
            CharsetMatch charset = detector.detect();
            return Charset.forName(charset.getName());
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.ENCODING_NOT_PARSED + " "+file.getName());
        }
        return StandardCharsets.UTF_8;
    }

    private String getTextFromDoc(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(document);
            String text = extractor.getText();
            extractor.close();
            document.close();
            return text;
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.CONVERSION_FAILED +" .doc");
            return null;
        }
    }

    private String getTextFromPdf(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper textStripper = new PDFTextStripper();

            textStripper.setStartPage(1);
            textStripper.setEndPage(document.getNumberOfPages());

            return textStripper.getText(document);
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.CONVERSION_FAILED +" .pdf");
            return null;
        }
    }

    private String getText(File file) throws IOException {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();
            try (InputStream stream = new FileInputStream(file)) {
                parser.parse(stream, handler, metadata, context);
                String text = handler.toString();
                if (text.length() == 0) throw new TikaException("Text is not parsed");
            } catch (IOException | TikaException | SAXException | IllegalArgumentException e) {
                String[] splits = file.getName().split("\\.");
                SimpleLogger.log(LoggingLevel.WARNING, Message.CONVERSION_FAILED + " " + splits[splits.length-1]);
                return null;
            }
            return handler.toString();
    }

    private String getTextFromDocx(File file) {
        try(FileInputStream fis=new FileInputStream(file)){
            XWPFDocument xwpfDocument = new XWPFDocument(OPCPackage.open(fis));
            XWPFWordExtractor ext=new XWPFWordExtractor(xwpfDocument);
            return ext.getText();
        } catch (IOException | InvalidFormatException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.CONVERSION_FAILED + " .docx");
            return null;
        }
    }
}
