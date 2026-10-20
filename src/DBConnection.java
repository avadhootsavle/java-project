import java.sql.*;

// every window calls DBConnection.getConnection() to talk to MySQL
public class DBConnection
{
    private static Integer workingPort = null;

    public static Connection getConnection() throws Exception
    {
        // Load MySQL JDBC Driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        if (workingPort != null)
        {
            try
            {
                return DriverManager.getConnection("jdbc:mysql://localhost:" + workingPort + "/airline_db", "root", "");
            }
            catch (Exception ignored)
            {
                workingPort = null;
            }
        }

        // Try standard port 3306 first, then fallback to 3307 (common in custom/XAMPP setups)
        int[] ports = { 3306, 3307 };
        Exception lastEx = null;
        for (int p : ports)
        {
            try
            {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:" + p + "/airline_db", "root", "");
                workingPort = p;
                return conn;
            }
            catch (Exception e)
            {
                lastEx = e;
            }
        }
        throw (lastEx != null) ? lastEx : new SQLException("Unable to connect to MySQL on ports 3306 or 3307");
    }
}
