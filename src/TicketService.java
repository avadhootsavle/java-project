import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;

public class TicketService
{
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
            if (n == 0)
                info = "PNR            : " + pnr + "\nBooked By      : " + rs.getString("name") + " (" + rs.getString("email") + ")"
                        + "\nFlight         : " + rs.getString("flight_no") + " (" + rs.getString("airline") + ")"
                        + "\nRoute          : " + rs.getString("source") + " -> " + rs.getString("destination")
                        + "\nDeparture      : " + DateTimeFormat.format(rs.getTimestamp("departure_time"))
                        + "\nArrival        : " + DateTimeFormat.format(rs.getTimestamp("arrival_time"))
                        + "\nBooking Date   : " + DateTimeFormat.format(rs.getTimestamp("booking_date")) + "\n";

            list = list + String.format("%-20s %-12s %-6s %-10s %.2f\n", rs.getString("passenger_name"),
                    rs.getInt("passenger_age") + " / " + rs.getString("passenger_gender"), rs.getString("seat_no"),
                    rs.getString("status"), rs.getDouble("ticket_price"));
            if (rs.getString("status").equals("CONFIRMED"))
                total = total + rs.getDouble("ticket_price");
            n++;
        }
        con.close();

        if (n == 0)
            throw new Exception("Booking not found");
        return info + "\nPASSENGERS (" + n + ")\n" + String.format("%-20s %-12s %-6s %-10s %s\n", "Name", "Age/Gender", "Seat", "Status", "Price")
                + list + String.format("\nTotal Fare     : Rs. %.2f", total);
    }

    public static String makePdf(String pnr) throws Exception
    {
        String[] lines = getDetails(pnr).split("\n");
        new File("tickets").mkdirs();
        String path = "tickets/ticket_" + pnr + ".pdf";

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);

        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
        cs.newLineAtOffset(50, 730);
        cs.showText("Gowardhan Airlines");
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
        cs.newLineAtOffset(0, -25);
        cs.showText("E-Ticket");
        cs.endText();
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
        cs.showText("Thank you for booking with us. Please arrive on time.");
        cs.endText();

        cs.close();
        doc.save(path);
        doc.close();
        return path;
    }

    public static void sendEmail(String pnr, String pdfPath) throws Exception
    {
        Properties config = new Properties();
        FileInputStream fis = new FileInputStream("config.properties");
        config.load(fis);
        fis.close();
        String from = config.getProperty("mail.username", "").trim();
        String pass = config.getProperty("mail.password", "").trim();
        if (from.isEmpty() || pass.isEmpty())
            throw new Exception("Email not set. Add your Gmail in config.properties");

        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT c.name, c.email FROM bookings b JOIN customers c ON b.customer_id = c.customer_id WHERE b.pnr = ?");
        ps.setString(1, pnr);
        ResultSet rs = ps.executeQuery();
        rs.next();
        String to = rs.getString("email");
        con.close();

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

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject("Your E-Ticket - PNR " + pnr);
        MimeBodyPart textPart = new MimeBodyPart();
        String smileE = "\uD83D\uDE42";
        textPart.setText("Thank you for booking with us" + smileE, "UTF-8");
        MimeBodyPart filePart = new MimeBodyPart();
        filePart.attachFile(new File(pdfPath));
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(textPart);
        mp.addBodyPart(filePart);
        msg.setContent(mp);
        Transport.send(msg);
    }

    public static String randomCode(int length)
    {
        String chars = "ACDEFHJKLMNPQRTUVWXY23456789";
        Random r = new Random();
        String code = "";
        for (int i = 0; i < length; i++)
            code = code + chars.charAt(r.nextInt(chars.length()));
        return code;
    }
}
