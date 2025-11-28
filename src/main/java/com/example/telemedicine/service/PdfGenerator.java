package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.SymptomType;
import com.example.telemedicine.exceptions.PdfGeneratorException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private static final Font HEADER_FONT =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(50, 50, 50));
    private static final Font NORMAL_FONT =
            FontFactory.getFont(FontFactory.HELVETICA, 12, new BaseColor(60, 60, 60));
    private static final Font SMALL_GRAY =
            FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, new BaseColor(120, 120, 120));

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
    public byte[] generateSessionPDF(Patient patient, MeasurementSession session, Set<SymptomType> symptoms, List<Signal> signals) throws PdfGeneratorException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addTitle(doc);
            addPatientInfo(doc, patient);
            addSessionInfo(doc, session);
            addSymptoms(doc, symptoms);
            addSignals(doc, signals);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new PdfGeneratorException("Error while generating PDF", e);
        }
    }

    /**
     * Adds the PDF title for the document.
     *
     * @param doc PDF document
     * @throws DocumentException If an error occurs while adding the title.
     */
    private void addTitle(Document doc) throws DocumentException {
        // --- Load logo safely ---
        Image logo = null;
        try {
            logo = Image.getInstance(getClass().getResource("/static/icons/logo.png"));
        } catch (Exception e) {
            throw new DocumentException("Error loading logo.png", e);
        }

        logo.scaleToFit(45, 45);
        logo.setAlignment(Image.ALIGN_CENTER);
        logo.setSpacingAfter(10f);

        doc.add(logo);

        Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                20,
                new BaseColor(60, 66, 82)
        );

        Paragraph title = new Paragraph("Measurement Session Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6f);
        doc.add(title);

        Font subtitleFont = FontFactory.getFont(
                FontFactory.TIMES_BOLD,
                16,
                new BaseColor(80, 140, 200)
        );

        Paragraph subheading = new Paragraph("MotivSMA Platform", subtitleFont);
        subheading.setAlignment(Element.ALIGN_CENTER);
        subheading.setSpacingAfter(18f);
        doc.add(subheading);

        LineSeparator separator = new LineSeparator();
        separator.setLineColor(new BaseColor(200, 200, 200));
        separator.setPercentage(80);
        doc.add(new Chunk(separator));
        doc.add(Chunk.NEWLINE);
    }

    /**
     * Adds patient information to the PDF document.
     *
     * @param doc     PDF document.
     * @param patient
     * @throws DocumentException Erros launched while adding patient info.
     */
    private void addPatientInfo(Document doc, Patient patient) throws DocumentException {
        Paragraph h = new Paragraph("Patient Information", HEADER_FONT);
        h.setSpacingBefore(10f);
        h.setSpacingAfter(4f);
        doc.add(h);

        doc.add(new Paragraph("Name: " + patient.getName() + " " + patient.getSurname(), SMALL_GRAY));
        doc.add(new Paragraph("Gender: " + patient.getGender(), SMALL_GRAY));
        doc.add(new Paragraph("Birthdate: " + (patient.getBirthDate() != null ? patient.getBirthDate().toString() : "N/A"), SMALL_GRAY));
        doc.add(new Paragraph("Height: " + patient.getHeight() + " cm", SMALL_GRAY));
        doc.add(new Paragraph("Weight: " + patient.getWeight() + " kg", SMALL_GRAY));
        doc.add(Chunk.NEWLINE);
    }

    /**
     * Adds measurement session details to the PDF document.
     *
     * @param doc
     * @param session Measurement session
     * @throws DocumentException
     */
    private void addSessionInfo(Document doc, MeasurementSession session) throws DocumentException {
        Paragraph h = new Paragraph("Session Information", HEADER_FONT);
        h.setSpacingBefore(10f);
        h.setSpacingAfter(4f);
        doc.add(h);

        String formattedDate = session.getTimeStamp() != null ? session.getTimeStamp().format(DATE_FORMATTER) : "N/A";
        doc.add(new Paragraph("Date: " + formattedDate, SMALL_GRAY));
        doc.add(Chunk.NEWLINE);
    }

    /**
     * Adds rselected symptoms to the PDF document.
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
            for (SymptomType symptom : symptoms) {
                doc.add(new Paragraph("- " + symptom.name(), SMALL_GRAY));
            }
        }
        doc.add(Chunk.NEWLINE);
    }

    /**
     * Adds recorded signals to the PDF document.
     *
     * @param doc
     * @param signals List of registered biosignals.
     * @throws DocumentException
     */
    private void addSignals(Document doc, List<Signal> signals) throws DocumentException {
        Paragraph h = new Paragraph("Signals: ", HEADER_FONT);
        h.setSpacingBefore(10f);
        h.setSpacingAfter(4f);
        doc.add(h);

        if (signals == null || signals.isEmpty()) {
            doc.add(new Paragraph("No signals recorded.", SMALL_GRAY));
        } else {
            for (Signal signal : signals) {
                doc.add(new Paragraph("Signal Type: " + signal.getSignalType().name(), SMALL_GRAY));

                double[] data = signal.getSignalDataAsDoubleArray();
                if (data != null && data.length > 0) {
                    String dataPreview = data.length > 5 ?
                            Arrays.toString(Arrays.copyOf(data, 5)) + "..." :
                            Arrays.toString(data);

                    doc.add(new Paragraph("Data Preview: " + dataPreview, SMALL_GRAY));
                } else {
                    doc.add(new Paragraph("No signal data available.", SMALL_GRAY));
                }
                doc.add(Chunk.NEWLINE);
            }
        }
    }
}
