package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.SymptomType;
import com.example.telemedicine.exceptions.PdfGeneratorException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for generating PDF files containing patient measurement sessions
 * information.
 * The PDF includes: Patient information, Measurement session details, recorded Signals
 * ECG/EMG, selected Symptoms.
 * Uses the iText library to create PDFs and returns the result as a byte array suitable
 * for storing the information in a database.
 */
@Service
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.TIMES_BOLD, 14, new BaseColor(50, 50, 50));
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, new BaseColor(60, 60, 60));
    private static final Font SMALL_GRAY = FontFactory.getFont(FontFactory.TIMES, 10, new BaseColor(120, 120, 120));

    /**
     * Generates a PDF document for a patient measurement session.
     *
     * @param patient  Patient to whom the session belongs.
     * @param session  Measurement session desired to document.
     * @param symptoms Set of symptoms selected during the session.
     * @param signals  List of signals recording during the session.
     * @return A byte array representing the generated PDF file.
     * @throws PdfGeneratorException If errors occur during the PDF creation.
     */
    public byte[] generateSessionPDF(Patient patient, MeasurementSession session, Set<SymptomType> symptoms,
                                     List<Signal> signals, String doctorComment) throws PdfGeneratorException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            float marginLeft = 100f;
            float marginRight = 100f;
            float marginTop = 70f;
            float marginBottom = 70f;

            Document doc = new Document(PageSize.A4, marginLeft, marginRight, marginTop, marginBottom);

            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            doc.open();

            addTitle(doc);
            addPatientAndSessionTable(doc, patient, session);
            addSymptoms(doc, symptoms);
            addSignals(doc, writer, signals);

            if (doctorComment != null && !doctorComment.isBlank()) {
                addDoctorComments(doc, doctorComment);
            }

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new PdfGeneratorException("Error while generating PDF", e);
        }
    }

    private void addDoctorComments(Document doc, String comments) throws DocumentException {
        Paragraph header = new Paragraph("Doctor's Notes:", HEADER_FONT);
        header.setSpacingBefore(10f);
        header.setSpacingAfter(4f);
        doc.add(header);

        Paragraph commentParagraph = new Paragraph(comments, NORMAL_FONT);
        commentParagraph.setSpacingAfter(10f);
        doc.add(commentParagraph);
    }

    /**
     * Adds the PDF title for the document.
     *
     * @param doc PDF document
     * @throws DocumentException If an error occurs while adding the title.
     */
    private void addTitle(Document doc) throws DocumentException {
        try {
            URL url = getClass().getClassLoader().getResource("logo.png");
            if (url == null) {
                throw new RuntimeException("Logo not found in classpath: logo.png");
            }

            Image logo = Image.getInstance(url);
            logo.scaleToFit(40, 40);

            logo.setAlignment(Image.ALIGN_CENTER);

            doc.add(logo);

        } catch (Exception e) {
            throw new RuntimeException("Error loading logo image", e);
        }

        Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 20, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Measurement Session Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5f);

        doc.add(title);

        Font subheadingFont = FontFactory.getFont(FontFactory.TIMES, 16, BaseColor.DARK_GRAY);
        Paragraph subheading = new Paragraph("Spinal Muscular Atrophy Patient Analysis", subheadingFont);
        subheading.setAlignment(Element.ALIGN_CENTER);
        subheading.setSpacingAfter(20f);

        doc.add(subheading);
    }

    /**
     * Adds selected symptoms to the PDF document.
     *
     * @param doc
     * @param symptoms
     * @throws DocumentException
     */
    private void addSymptoms(Document doc, Set<SymptomType> symptoms) throws DocumentException {
        Paragraph h = new Paragraph("Symptoms: ", HEADER_FONT);
        h.setSpacingBefore(10f);
        h.setSpacingAfter(4f);
        doc.add(h);

        if (symptoms == null || symptoms.isEmpty()) {
            doc.add(new Paragraph("No symptoms recorded.", SMALL_GRAY));
        } else {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setSpacingBefore(5f);
            table.setSpacingAfter(10f);

            int count = 0;
            for (SymptomType symptom : symptoms) {
                PdfPCell cell = new PdfPCell(new Phrase("\u2022 " + symptom.name(), SMALL_GRAY));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);
                count++;
            }

            if (count % 2 != 0) {
                PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                emptyCell.setBorder(Rectangle.NO_BORDER);
                table.addCell(emptyCell);
            }

            doc.add(table);
        }

        LineSeparator line = new LineSeparator();
        line.setLineWidth(0.5f);
        line.setLineColor(BaseColor.GRAY);
        doc.add(new Chunk(line));
    }

    /**
     * Adds recorded signals to the PDF document.
     *
     * @param doc
     * @param writer
     * @param signals List of registered biosignals.
     * @throws DocumentException
     */
    private void addSignals(Document doc, PdfWriter writer, List<Signal> signals) throws DocumentException {
        if (signals == null || signals.isEmpty()) {
            doc.add(new Paragraph("No signals recorded.", SMALL_GRAY));
        } else {
            for (Signal signal : signals) {
                addSignalChart(doc, writer, signal);
            }
        }

        LineSeparator line = new LineSeparator();
        line.setLineWidth(0.5f);
        line.setLineColor(BaseColor.GRAY);
        doc.add(new Chunk(line));
    }

    /**
     * Adds patient information and session information in a 2-column table.
     *
     * @param doc     PDF document
     * @param patient the patient
     * @param session the measurement session
     * @throws DocumentException
     */
    private void addPatientAndSessionTable(Document doc, Patient patient, MeasurementSession session) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(5f);
        table.setWidths(new int[]{1, 1});

        PdfPCell patientCell = new PdfPCell();
        patientCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        patientCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        patientCell.setBorder(Rectangle.NO_BORDER);

        patientCell.addElement(new Paragraph("Patient Information", HEADER_FONT));
        patientCell.addElement(new Paragraph("Name: " + patient.getName() + " " + patient.getSurname(), SMALL_GRAY));
        patientCell.addElement(new Paragraph("Gender: " + patient.getGender(), SMALL_GRAY));
        patientCell.addElement(new Paragraph("Birthdate: " + (patient.getBirthDate() != null ? patient.getBirthDate().toString() : "N/A"), SMALL_GRAY));
        patientCell.addElement(new Paragraph("Height: " + patient.getHeight() + " cm", SMALL_GRAY));
        patientCell.addElement(new Paragraph("Weight: " + patient.getWeight() + " kg", SMALL_GRAY));

        table.addCell(patientCell);

        PdfPCell sessionCell = new PdfPCell();
        sessionCell.setBorder(Rectangle.NO_BORDER);
        sessionCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        sessionCell.addElement(new Paragraph("Session Information", HEADER_FONT));
        String formattedDate = session.getTimeStamp() != null ? session.getTimeStamp().format(DATE_FORMATTER) : "N/A";
        sessionCell.addElement(new Paragraph("Date: " + formattedDate, SMALL_GRAY));

        table.addCell(sessionCell);

        doc.add(table);

        LineSeparator line = new LineSeparator();
        line.setLineWidth(0.5f);
        line.setLineColor(BaseColor.GRAY);
        doc.add(new Chunk(line));
    }

    /**
     * Draws a signal (ECG/EMG) as a line chart in the PDF.
     *
     * @param doc    PDF document
     * @param signal the Signal object containing data
     * @throws DocumentException
     */
    private void addSignalChart(Document doc, PdfWriter writer, Signal signal) throws DocumentException {
        double[] data = signal.getSignalDataAsDoubleArray();
        if (data == null || data.length == 0) {
            doc.add(new Paragraph("No data for signal: " + signal.getSignalType(), SMALL_GRAY));
            return;
        }

        Paragraph title = new Paragraph(signal.getSignalType().name() + " Signal:", HEADER_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10f);
        doc.add(title);

        float chartWidth = 400f;
        float chartHeight = 150f;
        float margin = 30f;

        PdfContentByte canvas = writer.getDirectContent();
        PdfTemplate template = canvas.createTemplate(chartWidth + margin * 2, chartHeight + margin * 2);

        double min = Arrays.stream(data).min().orElse(0);
        double max = Arrays.stream(data).max().orElse(1);
        double range = max - min;
        if (range == 0) range = 1;

        float xStep = chartWidth / (data.length - 1);

        int horizontalLines = 5;
        int verticalLines = 10;
        template.setLineWidth(0.25f);
        template.setGrayStroke(0.8f);

        for (int i = 0; i <= horizontalLines; i++) {
            float y = margin + i * (chartHeight / horizontalLines);
            template.moveTo(margin, y);
            template.lineTo(chartWidth + margin, y);
            template.stroke();
        }

        for (int i = 0; i <= verticalLines; i++) {
            float x = margin + i * (chartWidth / verticalLines);
            template.moveTo(x, margin);
            template.lineTo(x, chartHeight + margin);
            template.stroke();
        }

        template.moveTo(margin, margin);
        template.lineTo(margin, chartHeight + margin);
        template.stroke();

        template.moveTo(margin, margin);
        template.lineTo(chartWidth + margin, margin);
        template.stroke();

        template.setLineWidth(1.2f);
        template.setRGBColorStroke(240, 84, 84);

        for (int i = 0; i < data.length - 1; i++) {
            float x1 = margin + i * xStep;
            float y1 = margin + (float) ((data[i] - min) / range * chartHeight);
            float x2 = margin + (i + 1) * xStep;
            float y2 = margin + (float) ((data[i + 1] - min) / range * chartHeight);

            template.moveTo(x1, y1);
            template.lineTo(x2, y2);
            template.stroke();
        }

        Font labelFont = FontFactory.getFont(FontFactory.TIMES, 8, BaseColor.DARK_GRAY);
        ColumnText.showTextAligned(template, Element.ALIGN_RIGHT, new Phrase(String.format("%.2f", max), labelFont), margin - 2, margin + chartHeight, 0);
        ColumnText.showTextAligned(template, Element.ALIGN_RIGHT, new Phrase(String.format("%.2f", min), labelFont), margin - 2, margin, 0);

        Image chartImage = Image.getInstance(template);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        doc.add(chartImage);
        doc.add(Chunk.NEWLINE);
    }
}
