import java.io.*;
import java.net.*;
import java.sql.*;
import com.sun.net.httpserver.*;

// small HTTP backend (port 8085) between the demo payment website and this Java app
// it uses HttpServer which comes with Java, no extra jar is needed
//   GET  /api/payment/details?ref=PAYXXXXXX  -> payment and flight details
//   POST /api/payment/success?ref=...        -> "Payment Done" button
//   POST /api/payment/fail?ref=...           -> "Payment Failed" button
public class PaymentServer
{
    static PaymentFrame current = null; // payment window that is open on the laptop (only one at a time)

    public static void start()
    {
        try
        {
            HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);
            server.createContext("/api/payment", new HttpHandler()
            {
                public void handle(HttpExchange ex) throws IOException
                {
                    handleRequest(ex);
                }
            });
            server.start();
            System.out.println("Payment server started: http://" + getLaptopIp() + ":8085");
        }
        catch (Exception e)
        {
            System.out.println("Payment server could not start: " + e);
        }
    }

    static void handleRequest(HttpExchange ex) throws IOException
    {
        String path = ex.getRequestURI().getPath();   // eg. /api/payment/success
        String query = ex.getRequestURI().getQuery(); // eg. ref=PAY4K7M2X
        String ref = "";
        if (query != null && query.startsWith("ref="))
            ref = query.substring(4).toUpperCase().replaceAll("[^A-Z0-9]", "");

        // the payment window of this ref (null if it is closed)
        PaymentFrame pf = null;
        if (current != null && current.ref.equals(ref))
            pf = current;

        String reply = "{}";
        if (path.endsWith("/details"))
            reply = getDetails(ref);
        else if (path.endsWith("/success"))
            reply = paymentDone(ref, pf);
        else if (path.endsWith("/fail") && update("UPDATE payments SET status = 'FAILED' WHERE payment_ref = ? AND status = 'PENDING'", ref) == 1)
        {
            if (pf != null)
                pf.paymentFailed();
            reply = message(true, "Payment failed. No seat was booked.");
        }
        else if (path.endsWith("/fail"))
            reply = message(false, "This payment is already completed or failed.");

        // sending the reply to the phone
        byte[] data = reply.getBytes("UTF-8");
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // website runs on another port
        ex.sendResponseHeaders(200, data.length);
        OutputStream os = ex.getResponseBody();
        os.write(data);
        os.close();

        // making the PDF and email takes a few seconds, so it is done after replying to the phone
        if (path.endsWith("/success") && pf != null && pf.done)
            pf.makeTicket();
    }

    // payment, flight and customer details for the website (as JSON text)
    static String getDetails(String ref)
    {
        try
        {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT p.*, f.flight_no, f.airline, f.source, f.destination, f.departure_time, c.name "
                    + "FROM payments p JOIN flights f ON p.flight_id = f.flight_id JOIN customers c ON p.customer_id = c.customer_id WHERE p.payment_ref = ?");
            ps.setString(1, ref);
            ResultSet rs = ps.executeQuery();
            String json = message(false, "Payment not found. Please scan the QR code again.");
            if (rs.next())
                json = "{\"ok\":true,\"ref\":\"" + ref + "\",\"status\":\"" + rs.getString("status") + "\",\"amount\":" + rs.getDouble("amount")
                        + ",\"seats\":\"" + rs.getString("seats") + "\",\"passengers\":" + rs.getInt("passengers")
                        + ",\"flightNo\":\"" + esc(rs.getString("flight_no")) + "\",\"airline\":\"" + esc(rs.getString("airline"))
                        + "\",\"from\":\"" + esc(rs.getString("source")) + "\",\"to\":\"" + esc(rs.getString("destination"))
                        + "\",\"departure\":\"" + rs.getString("departure_time").substring(0, 16) + "\",\"customer\":\"" + esc(rs.getString("name"))
                        + "\",\"pnr\":\"" + esc(rs.getString("pnr")) + "\"}";
            con.close();
            return json;
        }
        catch (Exception e)
        {
            System.out.println(e);
            return message(false, "Server error: " + e.getMessage());
        }
    }

    // "Payment Done" clicked on phone
    static String paymentDone(String ref, PaymentFrame pf)
    {
        // payment window is closed on the laptop
        if (pf == null)
        {
            update("UPDATE payments SET status = 'FAILED' WHERE payment_ref = ? AND status = 'PENDING'", ref);
            return message(false, "This payment is closed or already completed. Please book again.");
        }

        // status changes only if it is still PENDING, so clicking two times cannot book two times
        if (update("UPDATE payments SET status = 'SUCCESS' WHERE payment_ref = ? AND status = 'PENDING'", ref) != 1)
            return message(false, "This payment is already completed or failed.");

        try
        {
            return "{\"ok\":true,\"message\":\"Payment successful. Booking confirmed!\",\"pnr\":\"" + pf.paymentSuccess() + "\"}";
        }
        catch (Exception e)
        {
            return message(false, "Booking failed: " + e.getMessage() + ". No money was taken (demo).");
        }
    }

    // runs an update query for one payment and returns number of rows changed
    static int update(String sql, String ref)
    {
        try
        {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ref);
            int rows = ps.executeUpdate();
            con.close();
            return rows;
        }
        catch (Exception e)
        {
            System.out.println(e);
            return 0;
        }
    }

    // finds the Wi-Fi IP of the laptop (eg. 192.168.1.104) so the phone can open the website
    static String getLaptopIp()
    {
        try
        {
            // connecting a UDP socket does not send anything, it only tells which IP the laptop uses on the network
            DatagramSocket ds = new DatagramSocket();
            ds.connect(InetAddress.getByName("8.8.8.8"), 80);
            String ip = ds.getLocalAddress().getHostAddress();
            ds.close();
            return ip;
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return "localhost";
    }

    static String message(boolean ok, String msg)
    {
        return "{\"ok\":" + ok + ",\"message\":\"" + esc(msg) + "\"}";
    }

    // makes text safe to put inside JSON
    static String esc(String s)
    {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
