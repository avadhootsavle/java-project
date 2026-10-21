import java.io.*;
import java.net.*;
import java.sql.*;
import com.sun.net.httpserver.*;

public class PaymentServer implements HttpHandler
{
    static PaymentFrame current = null;

    public static void start()
    {
        try
        {
            HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);

            server.createContext("/api/payment", new PaymentServer());

            server.start();
            System.out.println("Payment server started on port 8085");
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public void handle(HttpExchange ex) throws IOException
    {
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();
        String ref = "";
        if (query != null)
            ref = query.substring(4);

        String reply = "";
        if (path.endsWith("/details"))
            reply = getDetails(ref);
        else if (path.endsWith("/success"))
            reply = paymentDone(ref);
        else if (path.endsWith("/fail"))
            reply = paymentFailed(ref);

        byte[] data = reply.getBytes();
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, data.length);
        OutputStream os = ex.getResponseBody();
        os.write(data);
        os.close();

        if (path.endsWith("/success") && current != null && current.done)
            current.makeTicket();
    }

    static String getDetails(String ref)
    {
        String json = "{\"ok\":false, \"message\":\"Payment not found\"}";
        try
        {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM payments p JOIN flights f ON p.flight_id = f.flight_id "
                    + "JOIN customers c ON p.customer_id = c.customer_id WHERE p.payment_ref = ?");
            ps.setString(1, ref);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                json = "{\"ok\":true";
                json = json + ", \"ref\":\"" + ref + "\"";
                json = json + ", \"status\":\"" + rs.getString("status") + "\"";
                json = json + ", \"amount\":" + rs.getDouble("amount");
                json = json + ", \"seats\":\"" + rs.getString("seats") + "\"";
                json = json + ", \"passengers\":" + rs.getInt("passengers");
                json = json + ", \"flightNo\":\"" + rs.getString("flight_no") + "\"";
                json = json + ", \"airline\":\"" + rs.getString("airline") + "\"";
                json = json + ", \"from\":\"" + rs.getString("source") + "\"";
                json = json + ", \"to\":\"" + rs.getString("destination") + "\"";
                json = json + ", \"departure\":\"" + DateTimeFormat.format(rs.getTimestamp("departure_time")) + "\"";
                json = json + ", \"customer\":\"" + rs.getString("name") + "\"";
                json = json + ", \"pnr\":\"" + rs.getString("pnr") + "\"}";
            }
            con.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return json;
    }

    static String paymentDone(String ref)
    {
        if (current == null || !current.ref.equals(ref))
            return "{\"ok\":false, \"message\":\"Payment window is closed. Please book again.\"}";

        if (update("UPDATE payments SET status = 'SUCCESS' WHERE payment_ref = ? AND status = 'PENDING'", ref) == 0)
            return "{\"ok\":false, \"message\":\"Payment is already done or failed.\"}";

        try
        {
            String pnr = current.paymentSuccess();
            return "{\"ok\":true, \"message\":\"Booking confirmed! PNR: " + pnr + "\"}";
        }
        catch (Exception e)
        {
            return "{\"ok\":false, \"message\":\"Booking failed, seats are not available.\"}";
        }
    }

    static String paymentFailed(String ref)
    {
        if (update("UPDATE payments SET status = 'FAILED' WHERE payment_ref = ? AND status = 'PENDING'", ref) == 0)
            return "{\"ok\":false, \"message\":\"Payment is already done or failed.\"}";

        if (current != null && current.ref.equals(ref))
            current.paymentFailed();
        return "{\"ok\":true, \"message\":\"Payment failed. No seat was booked.\"}";
    }

    static int update(String sql, String ref)
    {
        int rows = 0;
        try
        {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ref);
            rows = ps.executeUpdate();
            con.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return rows;
    }

    static String getLaptopIp()
    {
        String ip = "localhost";
        try
        {
            DatagramSocket ds = new DatagramSocket();
            ds.connect(InetAddress.getByName("8.8.8.8"), 80);
            ip = ds.getLocalAddress().getHostAddress();
            ds.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return ip;
    }
}
