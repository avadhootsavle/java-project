import java.awt.*;
import java.awt.event.*;

public class SeatMap extends Canvas implements MouseListener
{
    SeatSelectionFrame frame;
    String[] seatNos = new String[0];
    String[] status = new String[0];
    boolean[] picked = new boolean[0];
    int count = 0;
    int size = 40;

    public SeatMap(SeatSelectionFrame f)
    {
        frame = f;
        setBackground(Color.white);
        addMouseListener(this);
    }

    int seatX(int i) { return 60 + (i / 6) * (size + 5); }

    int seatY(int i)
    {
        int y = 30 + (i % 6) * (size + 5);
        if (i % 6 >= 3)
            y = y + 30;
        return y;
    }

    public void mousePressed(MouseEvent e)
    {
        for (int i = 0; i < seatNos.length; i++)
        {
            boolean inside = e.getX() >= seatX(i) && e.getX() <= seatX(i) + size
                          && e.getY() >= seatY(i) && e.getY() <= seatY(i) + size;

            if (inside && status[i].equals("AVAILABLE"))
            {
                if (picked[i])
                {
                    picked[i] = false;
                    count--;
                }
                else if (count == 6)
                    new MessageBox(frame, "You can book maximum 6 seats at a time");
                else
                {
                    picked[i] = true;
                    count++;
                }
                repaint();
                frame.updateSelected();
            }
        }
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void paint(Graphics g)
    {
        g.setColor(Color.black);
        g.drawString("front", 15, 25);

        for (int i = 0; i < seatNos.length; i++)
        {
            String text = seatNos[i];
            Color textColor = Color.black;

            if (picked[i])
                g.setColor(Color.lightGray);
            else if (status[i].equals("AVAILABLE"))
                g.setColor(Color.white);
            else
            {
                g.setColor(Color.black);
                textColor = Color.white;
                text = "X";
            }
            g.fillRect(seatX(i), seatY(i), size, size);

            g.setColor(Color.black);
            g.drawRect(seatX(i), seatY(i), size, size);

            g.setColor(textColor);
            g.drawString(text, seatX(i) + 8, seatY(i) + 25);
        }
    }
}
