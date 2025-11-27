package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.SymptomType;
import com.example.telemedicine.exceptions.PdfGeneratorException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.PageSize;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

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

    private void addTitle(Document doc) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Measurement Session Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        doc.add(title);
    }

    private void addPatientInfo(Document doc, Patient patient) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        doc.add(new Paragraph("Patient Information:", headerFont));
        doc.add(new Paragraph("Name: " + patient.getName() + " " + patient.getSurname(), normalFont));
        doc.add(new Paragraph("Gender: " + patient.getGender(), normalFont));
        doc.add(new Paragraph("Birthdate: " + (patient.getBirthDate() != null ? patient.getBirthDate().toString() : "N/A"), normalFont));
        doc.add(new Paragraph("Height: " + patient.getHeight() + " cm", normalFont));
        doc.add(new Paragraph("Weight: " + patient.getWeight() + " kg", normalFont));
        doc.add(Chunk.NEWLINE);
    }

    private void addSessionInfo(Document doc, MeasurementSession session) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        doc.add(new Paragraph("Session Information:", headerFont));
        String formattedDate = session.getTimeStamp() != null ? session.getTimeStamp().format(DATE_FORMATTER) : "N/A";
        doc.add(new Paragraph("Date: " + formattedDate, normalFont));
        doc.add(Chunk.NEWLINE);
    }

    private void addSymptoms(Document doc, Set<SymptomType> symptoms) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        doc.add(new Paragraph("Symptoms:", headerFont));

        if (symptoms == null || symptoms.isEmpty()) {
            doc.add(new Paragraph("No symptoms recorded.", normalFont));
        } else {
            for (SymptomType symptom : symptoms) {
                doc.add(new Paragraph("- " + symptom.name(), normalFont));
            }
        }
        doc.add(Chunk.NEWLINE);
    }

    private void addSignals(Document doc, List<Signal> signals) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        doc.add(new Paragraph("Signals:", headerFont));

        if (signals == null || signals.isEmpty()) {
            doc.add(new Paragraph("No signals recorded.", normalFont));
        } else {
            for (Signal signal : signals) {
                doc.add(new Paragraph("Signal Type: " + signal.getSignalType().name(), normalFont));

                double[] data = signal.getSignalDataAsDoubleArray();
                if (data != null && data.length > 0) {
                    String dataPreview = data.length > 5 ?
                            Arrays.toString(Arrays.copyOf(data, 5)) + "..." :
                            Arrays.toString(data);

                    doc.add(new Paragraph("Data Preview: " + dataPreview, normalFont));
                } else {
                    doc.add(new Paragraph("No signal data available.", normalFont));
                }
                doc.add(Chunk.NEWLINE);
            }
        }
    }
}
