import java.awt.*;
import java.awt.event.*;
import java.sql.*;


public class SeatSelectionFrame extends Frame implements ActionListener, WindowListener
{
    Label l1, l2, l3, selectedLabel;
    Button continueBtn, backBtn;
    Panel p1, p2;
    SeatMap map;
    AvailableFlightsFrame flightsFrame;
    int customerId, flightId;
    double price = 0;
    int[] seatIds = new int[0];

    public SeatSelectionFrame(AvailableFlightsFrame af, int cid, int fid)
    {
        flightsFrame = af;
        customerId = cid;
        flightId = fid;


        setLayout(new GridLayout(2, 1, 0, 5));


        l1 = new Label("SELECT YOUR SEATS", Label.CENTER);
        l2 = new Label("", Label.CENTER);
        l3 = new Label("White = Available     Grey = Selected     Blacj (X) = Booked", Label.CENTER);
        selectedLabel = new Label("", Label.CENTER);
        continueBtn = new Button("Continue");
        backBtn = new Button("Back");
        map = new SeatMap(this);

        p1 = new Panel(new GridLayout(5, 1, 0, 8));
        p2 = new Panel(new GridLayout(1, 2, 20, 0));

        setBackground(new Color(232, 240, 250));
        setFont(new Font("Arial", Font.PLAIN, 15));
        l1.setBackground(new Color(22, 54, 110));
        l1.setForeground(Color.white);
        l1.setFont(new Font("Arial", Font.BOLD, 22));
        l2.setFont(new Font("Arial", Font.BOLD, 15));
        selectedLabel.setFont(new Font("Arial", Font.BOLD, 15));


        continueBtn.addActionListener(this);
        backBtn.addActionListener(this);


        p2.add(continueBtn);
        p2.add(backBtn);
        p1.add(l1);
        p1.add(l2);
        p1.add(l3);
        p1.add(selectedLabel);
        p1.add(p2);
        add(p1);
        add(map);


        addWindowListener(this);


        setTitle("Select Seats");
        setSize(1150, 800);
        setLocationRelativeTo(null);
        setVisible(true);

        loadSeats();
    }


    public void loadSeats()
    {
        try
        {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps1 = con.prepareStatement("SELECT * FROM flights WHERE flight_id = ?");
            ps1.setInt(1, flightId);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            price = rs1.getDouble("price");
            l2.setText(rs1.getString("flight_no") + "   |   " + rs1.getString("source") + " -> " + rs1.getString("destination")
                    + "   |   " + DateTimeFormat.format(rs1.getTimestamp("departure_time")) + "   |   Rs. " + price + " per seat");


            int n = rs1.getInt("total_seats");
            seatIds = new int[n];
            map.seatNos = new String[n];
            map.status = new String[n];
            map.picked = new boolean[n];
            map.count = 0;

            PreparedStatement ps2 = con.prepareStatement("SELECT * FROM seats WHERE flight_id = ? ORDER BY seat_id");
            ps2.setInt(1, flightId);
            ResultSet rs2 = ps2.executeQuery();
            for (int i = 0; i < n && rs2.next(); i++)
            {
                seatIds[i] = rs2.getInt("seat_id");
                map.seatNos[i] = rs2.getString("seat_no");
                map.status[i] = rs2.getString("status");
            }
            con.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
            new MessageBox(this, "Error: " + e.getMessage());
        }
        map.repaint();
        updateSelected();
    }


    public void updateSelected()
    {
        String seats = "";
        for (int i = 0; i < map.seatNos.length; i++)
            if (map.picked[i])
                seats = seats + map.seatNos[i] + " ";

        if (map.count == 0)
            selectedLabel.setText("Selected seats: none   (click on a seat, max 6)");
        else
            selectedLabel.setText("Selected seats: " + seats + "  |   Total: Rs. " + (price * map.count) + " + 5% GST");
    }


    public void actionPerformed(ActionEvent e)
    {
        Button btn = (Button) e.getSource();

        if (btn == backBtn)
            dispose();
        else if (map.count == 0)
            new MessageBox(this, "Please select at least one seat");
        else
        {

            int[] ids = new int[map.count];
            String[] nos = new String[map.count];
            int j = 0;
            for (int i = 0; i < map.seatNos.length; i++)
            {
                if (map.picked[i])
                {
                    ids[j] = seatIds[i];
                    nos[j] = map.seatNos[i];
                    j++;
                }
            }
            new PassengerDetailsFrame(this, flightsFrame, customerId, flightId, ids, nos, price);
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
