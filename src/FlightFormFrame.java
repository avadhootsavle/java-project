import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;


public class FlightFormFrame extends Frame implements ActionListener, WindowListener
{
    Label l1, l2, l3, l4, l5, l6, l7, l8, l9, l10;
    TextField noTf, airlineTf, sourceTf, destTf, depTf, arrTf, priceTf, seatsTf;
    Button saveBtn, backBtn;
    ManageFlightsFrame parent;
    int flightId;

    public FlightFormFrame(ManageFlightsFrame mf, int id)
    {
        parent = mf;
        flightId = id;


        setLayout(new GridLayout(10, 2, 0, 8));


        if (flightId == 0)
            l1 = new Label("   ADD NEW FLIGHT");
        else
            l1 = new Label("   UPDATE FLIGHT  (ID " + flightId + ")");
        l10 = new Label("");
        l2 = new Label("    Flight Number");
        l3 = new Label("    Airline");
        l4 = new Label("    Source");
        l5 = new Label("    Destination");
        l6 = new Label("    Departure (dd-MM-yyyy HH:mm)");
        l7 = new Label("    Arrival (dd-MM-yyyy HH:mm)");
        l8 = new Label("    Ticket Price (Rs)");
        l9 = new Label("    Total Seats (max 180)");
        noTf = new TextField(18);
        airlineTf = new TextField(18);
        sourceTf = new TextField(18);
        destTf = new TextField(18);
        depTf = new TextField(18);
        arrTf = new TextField(18);
        priceTf = new TextField(18);
        seatsTf = new TextField(18);
        saveBtn = new Button("Save Flight");
        backBtn = new Button("Back");


        setBackground(new Color(232, 240, 250));
        setFont(new Font("Arial", Font.PLAIN, 15));
        l1.setBackground(new Color(22, 54, 110));
        l1.setForeground(Color.white);
        l1.setFont(new Font("Arial", Font.BOLD, 22));
        l10.setBackground(new Color(22, 54, 110));


        saveBtn.addActionListener(this);
        backBtn.addActionListener(this);


        add(l1);
        add(l10);
        add(l2);
        add(noTf);
        add(l3);
        add(airlineTf);
        add(l4);
        add(sourceTf);
        add(l5);
        add(destTf);
        add(l6);
        add(depTf);
        add(l7);
        add(arrTf);
        add(l8);
        add(priceTf);
        add(l9);
        add(seatsTf);
        add(saveBtn);
        add(backBtn);


        addWindowListener(this);


        setTitle("Flight Details");
        setSize(600, 560);
        setLocationRelativeTo(null);
        setVisible(true);


        if (flightId != 0)
        {
            try
            {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM flights WHERE flight_id = ?");
                ps.setInt(1, flightId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                noTf.setText(rs.getString("flight_no"));
                airlineTf.setText(rs.getString("airline"));
                sourceTf.setText(rs.getString("source"));
                destTf.setText(rs.getString("destination"));
                depTf.setText(DateTimeFormat.format(rs.getTimestamp("departure_time")));
                arrTf.setText(DateTimeFormat.format(rs.getTimestamp("arrival_time")));
                priceTf.setText("" + rs.getDouble("price"));
                seatsTf.setText("" + rs.getInt("total_seats"));
                con.close();
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
    }


    public void actionPerformed(ActionEvent e)
    {
        Button btn = (Button) e.getSource();
        if (btn == backBtn)
        {
            dispose();
            return;
        }


        LocalDateTime dep, arr;
        double price;
        int seats;
        try
        {
            dep = DateTimeFormat.parse(depTf.getText());
            arr = DateTimeFormat.parse(arrTf.getText());
            price = Double.parseDouble(priceTf.getText().trim());
            seats = Integer.parseInt(seatsTf.getText().trim());
        }
        catch (Exception ex)
        {
            new MessageBox(this, "Please enter correct values\nTime format: dd-MM-yyyy HH:mm\nPrice and seats must be numbers");
            return;
        }

        if (noTf.getText().trim().isEmpty() || airlineTf.getText().trim().isEmpty() || sourceTf.getText().trim().isEmpty() || destTf.getText().trim().isEmpty())
            new MessageBox(this, "All fields are required");
        else if (sourceTf.getText().trim().equalsIgnoreCase(destTf.getText().trim()))
            new MessageBox(this, "Source and destination cannot be same");
        else if (!arr.isAfter(dep))
            new MessageBox(this, "Arrival time must be after departure time");
        else if (price <= 0 || seats < 1 || seats > 180)
            new MessageBox(this, "Price must be more than 0 and seats between 1 and 180");
        else if (flightId == 0 && dep.isBefore(LocalDateTime.now()))
            new MessageBox(this, "Departure time must be in the future");
        else
            saveFlight(price, seats);
    }


    void saveFlight(double price, int seats)
    {
        Connection con = null;
        try
        {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            int available = seats;
            boolean makeSeats = true;

            if (flightId != 0)
            {

                PreparedStatement ps1 = con.prepareStatement("SELECT total_seats, available_seats, (SELECT COUNT(*) FROM bookings "
                        + "WHERE flight_id = ? AND status = 'CONFIRMED') AS booked FROM flights WHERE flight_id = ? FOR UPDATE");
                ps1.setInt(1, flightId);
                ps1.setInt(2, flightId);
                ResultSet rs = ps1.executeQuery();
                rs.next();
                if (seats == rs.getInt("total_seats"))
                {
                    available = rs.getInt("available_seats");
                    makeSeats = false;
                }
                else if (rs.getInt("booked") > 0)
                    throw new Exception("Total seats cannot be changed, this flight has active bookings");
                else
                {
                    PreparedStatement ps2 = con.prepareStatement("DELETE FROM seats WHERE flight_id = ?");
                    ps2.setInt(1, flightId);
                    ps2.executeUpdate();
                }
            }

            String sql = "UPDATE flights SET flight_no = ?, airline = ?, source = ?, destination = ?, departure_time = ?, "
                    + "arrival_time = ?, price = ?, total_seats = ?, available_seats = ? WHERE flight_id = ?";
            if (flightId == 0)
                sql = "INSERT INTO flights (flight_no, airline, source, destination, departure_time, arrival_time, price, "
                        + "total_seats, available_seats) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, noTf.getText().trim().toUpperCase());
            ps.setString(2, airlineTf.getText().trim());
            ps.setString(3, sourceTf.getText().trim());
            ps.setString(4, destTf.getText().trim());
            ps.setTimestamp(5, Timestamp.valueOf(DateTimeFormat.parse(depTf.getText())));
            ps.setTimestamp(6, Timestamp.valueOf(DateTimeFormat.parse(arrTf.getText())));
            ps.setDouble(7, price);
            ps.setInt(8, seats);
            ps.setInt(9, available);
            if (flightId != 0)
                ps.setInt(10, flightId);
            ps.executeUpdate();


            int id = flightId;
            ResultSet keys = ps.getGeneratedKeys();
            if (flightId == 0 && keys.next())
                id = keys.getInt(1);


            PreparedStatement ps3 = con.prepareStatement("INSERT INTO seats (flight_id, seat_no) VALUES (?, ?)");
            for (int i = 0; makeSeats && i < seats; i++)
            {
                ps3.setInt(1, id);
                ps3.setString(2, (i / 6 + 1) + "" + "ABCDEF".charAt(i % 6));
                ps3.executeUpdate();
            }

            con.commit();
            con.close();
            new MessageBox(this, "Flight saved successfully!\nFlight ID: " + id);
            parent.loadFlights("");
            dispose();
        }
        catch (Exception e)
        {

            try
            {
                con.rollback();
                con.close();
            }
            catch (Exception ex)
            {
                System.out.println(ex);
            }
            new MessageBox(this, "Error: " + e.getMessage());
        }
    }


    public void windowClosing(WindowEvent we)
    {
        dispose();
    }
    public void windowOpened(WindowEvent we) {}
    public void windowClosed(WindowEvent we) {}
    public void windowIconified(WindowEvent we) {}
    public void windowDeiconified(WindowEvent we) {}
    public void windowActivated(WindowEvent we) {}
    public void windowDeactivated(WindowEvent we) {}
}
