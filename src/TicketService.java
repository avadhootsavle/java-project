import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;

// ticket details, PDF ticket, email and random codes (PNR / payment reference)
public class TicketService
{
    // booking details as text (used in TextArea, PDF and email), one row of the query = one passenger
    public static String getDetails(String pnr) throws Exception
    {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT b.*, s.seat_no, c.name, c.email, f.* FROM bookings b "
                + "JOIN seats s ON b.seat_id = s.seat_id JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN flights f ON b.flight_id = f.flight_id WHERE b.pnr = ? ORDER BY s.seat_id");
        ps.setString(1, pnr);
        ResultSet rs = ps.executeQuery();

        String info = "", list = "";
        int n = 0;
        double total = 0;
        while (rs.next())
        {
            // flight details are same for all passengers, so they are taken from the first row
            if (n == 0)
                info = "PNR            : " + pnr + "\nBooked By      : " + rs.getString("name") + " (" + rs.getString("email") + ")"
                        + "\nFlight         : " + rs.getString("flight_no") + " (" + rs.getString("airline") + ")"
                        + "\nRoute          : " + rs.getString("source") + " -> " + rs.getString("destination")
                        + "\nDeparture      : " + rs.getString("departure_time").substring(0, 16)
                        + "\nArrival        : " + rs.getString("arrival_time").substring(0, 16)
                        + "\nBooking Date   : " + rs.getString("booking_date").substring(0, 16) + "\n";

            list = list + String.format("%-20s %-12s %-6s %-10s %.2f\n", rs.getString("passenger_name"),
                    rs.getInt("passenger_age") + " / " + rs.getString("passenger_gender"), rs.getString("seat_no"),
                    rs.getString("status"), rs.getDouble("ticket_price"));
            if (rs.getString("status").equals("CONFIRMED"))
                total = total + rs.getDouble("ticket_price"); // cancelled tickets are not counted
            n++;
        }
        con.close();

        if (n == 0)
            throw new Exception("Booking not found");
        return info + "\nPASSENGERS (" + n + ")\n" + String.format("%-20s %-12s %-6s %-10s %s\n", "Name", "Age/Gender", "Seat", "Status", "Price")
                + list + String.format("\nTotal Fare     : Rs. %.2f", total);
    }

    // makes tickets/ticket_PNR.pdf with the booking details and returns the file path
    public static String makePdf(String pnr) throws Exception
    {
        String[] lines = getDetails(pnr).split("\n");
        new File("tickets").mkdirs();
        String path = "tickets/ticket_" + pnr + ".pdf";

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);

        // heading
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
        cs.newLineAtOffset(50, 730);
        cs.showText("SkyWay Airline Reservation System");
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
        cs.newLineAtOffset(0, -25);
        cs.showText("Electronic Ticket / Boarding Pass");
        cs.endText();

        // booking details line by line (Courier font keeps the columns straight)
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 11);
        cs.setLeading(16);
        cs.newLineAtOffset(60, 650);
        for (int i = 0; i < lines.length; i++)
        {
            cs.showText(lines[i]);
            cs.newLine();
        }
        cs.showText("");
        cs.newLine();
        cs.showText("Please reach the airport 2 hours before departure with a valid photo ID.");
        cs.endText();

        // box around the details
        cs.addRect(40, 650 - lines.length * 16, 530, lines.length * 16 + 30);
        cs.stroke();
        cs.close();
        doc.save(path);
        doc.close();
        return path;
    }

    // sends the PDF ticket to the customer's email using Gmail
    public static void sendEmail(String pnr, String pdfPath) throws Exception
    {
        // reading gmail id and app password from config.properties
        Properties config = new Properties();
        FileInputStream fis = new FileInputStream("config.properties");
        config.load(fis);
        fis.close();
        String from = config.getProperty("mail.username", "").trim();
        String pass = config.getProperty("mail.password", "").trim();
        if (from.isEmpty() || pass.isEmpty())
            throw new Exception("Email not set. Add your Gmail in config.properties");

        // finding the customer email
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT c.name, c.email FROM bookings b JOIN customers c ON b.customer_id = c.customer_id WHERE b.pnr = ?");
        ps.setString(1, pnr);
        ResultSet rs = ps.executeQuery();
        rs.next();
        String name = rs.getString("name");
        String to = rs.getString("email");
        con.close();

        // gmail smtp settings
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props, new Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(from, pass);
            }
        });

        // mail with text and pdf attachment
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject("Your E-Ticket - PNR " + pnr);
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Dear " + name + ",\n\nThank you for booking with us. Your ticket is attached.\n\n" + getDetails(pnr) + "\n\nHappy journey!\nSkyWay Airlines");
        MimeBodyPart filePart = new MimeBodyPart();
        filePart.attachFile(new File(pdfPath));
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(textPart);
        mp.addBodyPart(filePart);
        msg.setContent(mp);
        Transport.send(msg);
    }

    // random code like K7MX2P (used for PNR and payment reference)
    public static String randomCode(int length)
    {
        String chars = "ACDEFHJKLMNPQRTUVWXY23456789"; // no B G I O S Z, they look like 8 6 1 0 5 2
        Random r = new Random();
        String code = "";
        for (int i = 0; i < length; i++)
            code = code + chars.charAt(r.nextInt(chars.length()));
        return code;
    }
}
