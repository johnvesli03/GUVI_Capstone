package com.BusBooking.Bus.util;

import com.BusBooking.Bus.model.Booking;
import com.BusBooking.Bus.model.Bus;
import com.BusBooking.Bus.model.Passenger;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;

public class TicketPDFGenerator {

    public static byte[] generateTicketPDF(Booking booking, Bus bus) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            addTitle(document);
            addBookingInformation(document, booking);
            addTripAndBusInformation(document, booking, bus);
            addPassengerInformation(document, booking);
            addBoardingInstructions(document);
            addQRCodeImage(document, booking, bus);
            addFooter(document);

            return outputStream.toByteArray();
        }
    }

    private static void addTitle(Document document) {
        Paragraph title = new Paragraph("🚌 Online Bus Ticket")
                .setBold()
                .setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER);
        Paragraph separator = new Paragraph("------------------------------------------------------------");
        document.add(title);
        document.add(separator);
    }

    private static void addBookingInformation(Document document, Booking booking) {
        document.add(new Paragraph("Booking ID: " + booking.getId()));
        document.add(new Paragraph("Booking Date: " + booking.getBookingDate()));
        document.add(new Paragraph("Email: " + booking.getEmail()));
    }

    private static void addTripAndBusInformation(Document document, Booking booking, Bus bus) {
        document.add(new Paragraph("\n🔹 Trip Details").setBold());
        document.add(new Paragraph("Bus Operator: " + bus.getOperatorName()));
        document.add(new Paragraph("Route: " + bus.getFrom() + " → " + bus.getTo()));
        document.add(new Paragraph("Departure Time: " + bus.getDepartureTime()));
        document.add(new Paragraph("Arrival Time: " + bus.getArrivalTime()));
        document.add(new Paragraph("Travel Date: " + booking.getTravelDate()));
        document.add(new Paragraph("Total Paid: ₹" + booking.getTotalAmount()));
        document.add(new Paragraph("Total Seats Booked: " + booking.getPassengers().size()));
        document.add(new Paragraph(" "));
    }

    private static void addPassengerInformation(Document document, Booking booking) {
        document.add(new Paragraph("👥 Passenger Details").setBold());
        for (Passenger passenger : booking.getPassengers()) {
            String passengerInfo = String.format(" - %s | Age: %d | Gender: %s | Seat: %d (%s)",
                    passenger.getName(),
                    passenger.getAge(),
                    passenger.getGender(),
                    passenger.getSeatNumber(),
                    passenger.getSeatType());
            document.add(new Paragraph(passengerInfo));
        }
    }

    private static void addBoardingInstructions(Document document) {
        document.add(new Paragraph("\n📍 Boarding Instructions").setBold());
        document.add(new Paragraph("Boarding Time: Please report 30 minutes before departure."));
        document.add(new Paragraph("Boarding Point: Main Bus Terminal (Gate 3)"));
        document.add(new Paragraph("Helpline: +91-98765-43210"));
    }

    private static void addQRCodeImage(Document document, Booking booking, Bus bus) throws Exception {
        String qrContent = String.format(
                "Bus: %s | From: %s | To: %s | Date: %s | Paid: ₹%.2f | Booking ID: %d",
                bus.getOperatorName(),
                bus.getFrom(),
                bus.getTo(),
                booking.getTravelDate(),
                booking.getTotalAmount(),
                booking.getId()
        );

        ByteArrayOutputStream qrOutputStream = new ByteArrayOutputStream();
        var qrMatrix = new QRCodeWriter().encode(qrContent, BarcodeFormat.QR_CODE, 150, 150);
        MatrixToImageWriter.writeToStream(qrMatrix, "PNG", qrOutputStream);

        Image qrImage = new Image(ImageDataFactory.create(qrOutputStream.toByteArray()))
                .setAutoScale(true)
                .setTextAlignment(TextAlignment.CENTER);

        document.add(new Paragraph("\nScan this QR code for quick ticket info").setTextAlignment(TextAlignment.CENTER));
        document.add(qrImage);
    }

    private static void addFooter(Document document) {
        document.add(new Paragraph("\nThank you for booking with us. Have a safe journey!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setItalic());
    }
}
