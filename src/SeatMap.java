import java.awt.*;
import java.awt.event.*;

// draws the aeroplane seat map (nose on the left, A B C | aisle | D E F)
// the drawing is scaled so the full plane fits in the window
public class SeatMap extends Canvas implements MouseListener
{
    SeatSelectionFrame frame;
    String[] seatNos = new String[0];  // 1A, 1B, ...
    String[] status = new String[0];   // AVAILABLE or BOOKED
    int[] selected = new int[6];       // index of selected seats (max 6)
    int selCount = 0, rows = 0, offX = 0, offY = 0;
    int seat = 36, gap = 6, aisle = 44, startX = 230, top = 20;
    double scale = 1;

    public SeatMap(SeatSelectionFrame f)
    {
        frame = f;
        setBackground(new Color(232, 240, 250));

        addMouseListener(this); // clicking a seat selects it
    }

    // clicking a seat selects it, clicking it again removes it
    public void mousePressed(MouseEvent e)
    {
        // mouse position is changed back to drawing size (because the drawing is scaled)
        int x = (int) ((e.getX() - offX) / scale);
        int y = (int) ((e.getY() - offY) / scale);

        for (int i = 0; i < seatNos.length; i++)
        {
            boolean inside = x >= seatX(i) && x <= seatX(i) + seat && y >= seatY(i) && y <= seatY(i) + seat;
            if (!inside || !status[i].equals("AVAILABLE"))
                continue;

            int pos = findSelected(i);
            if (pos >= 0)
            {
                selected[pos] = selected[selCount - 1]; // last selected seat comes in its place
                selCount--;
            }
            else if (selCount == 6)
                new MessageBox(frame, "You can book maximum 6 seats at a time");
            else
            {
                selected[selCount] = i;
                selCount++;
            }
            repaint();
            frame.updateSelected();
        }
    }

    // other mouse events are not used (MouseListener needs all 5 methods)
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // returns position of seat i in the selected array, or -1
    int findSelected(int i)
    {
        for (int j = 0; j < selCount; j++)
            if (selected[j] == i)
                return j;
        return -1;
    }

    // x position of seat i (i / 6 = row number, rows go from left to right)
    int seatX(int i)
    {
        return startX + (i / 6) * (seat + 8);
    }

    // y position of seat i (i % 6 = A to F, D E F come after the aisle)
    int seatY(int i)
    {
        int y = top + 25 + (i % 6) * (seat + gap);
        if (i % 6 >= 3)
            y = y + aisle - gap;
        return y;
    }

    // drawing directly on screen flickers, so update() only calls paint()
    public void update(Graphics g)
    {
        paint(g);
    }

    public void paint(Graphics g)
    {
        if (getWidth() == 0)
            return;
        rows = (seatNos.length + 5) / 6;
        int right = startX + rows * (seat + 8) + 45;         // end of the plane body
        int bottom = top + 6 * seat + 5 * gap + aisle + 45;  // bottom of the plane body
        int h = bottom - top, midY = (top + bottom) / 2;

        // everything is drawn on an image first, then shown (no flicker)
        Image buffer = createImage(getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) buffer.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        // scale so the full plane fits in the window
        scale = Math.min(1, Math.min((getWidth() - 20.0) / (right + 20), (getHeight() - 10.0) / (bottom + 20)));
        offX = (int) ((getWidth() - (right + 20) * scale) / 2);
        offY = (int) ((getHeight() - (bottom + 20) * scale) / 2);
        g2.translate(offX, offY);
        g2.scale(scale, scale);

        // body with round nose (left) and round tail (right)
        g2.setColor(new Color(250, 251, 253));
        g2.fillOval(10, top, 300, h);
        g2.fillOval(right - 150, top, 150, h);
        g2.fillRect(160, top, right - 235, h);
        g2.setColor(new Color(160, 172, 190));
        g2.setStroke(new BasicStroke(2));
        g2.drawArc(10, top, 300, h, 90, 180);
        g2.drawArc(right - 150, top, 150, h, 270, 180);
        g2.drawLine(160, top, right - 75, top);
        g2.drawLine(160, bottom, right - 75, bottom);

        // cockpit windows, FRONT, seat letters and row numbers
        g2.setColor(new Color(22, 54, 110));
        g2.fillRoundRect(40, midY - 36, 16, 32, 10, 10);
        g2.fillRoundRect(40, midY + 4, 16, 32, 10, 10);
        g2.setColor(Color.gray);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.drawString("FRONT", 90, midY + 5);
        for (int c = 0; c < 6; c++)
            g2.drawString("" + "ABCDEF".charAt(c), startX - 22, seatY(c) + 23);
        for (int r = 0; r < rows; r++)
            g2.drawString("" + (r + 1), seatX(r * 6) + 10, (seatY(2) + seat + seatY(3)) / 2 + 5);

        // seats: white = available, green = selected, grey with X = booked
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        for (int i = 0; i < seatNos.length; i++)
        {
            String text = seatNos[i].substring(seatNos[i].length() - 1);
            Color fill = Color.white;
            Color textColor = Color.white;
            if (findSelected(i) >= 0)
                fill = new Color(40, 160, 90);
            else if (!status[i].equals("AVAILABLE"))
            {
                fill = new Color(200, 204, 211);
                text = "X";
            }
            else
                textColor = new Color(22, 54, 110);

            g2.setColor(fill);
            g2.fillRoundRect(seatX(i), seatY(i), seat, seat, 12, 12);
            g2.setColor(new Color(150, 160, 178));
            g2.drawRoundRect(seatX(i), seatY(i), seat, seat, 12, 12);
            g2.setColor(textColor);
            g2.drawString(text, seatX(i) + 13, seatY(i) + 23);
        }

        g.drawImage(buffer, 0, 0, this);
        g2.dispose();
    }
}
