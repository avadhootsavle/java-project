import java.awt.*;
import java.awt.event.*;

// AWT does not have JOptionPane so this is our own popup box
public class MessageBox extends Dialog implements ActionListener, WindowListener
{
    Button okBtn, yesBtn, noBtn;
    public boolean yes = false; // true if user clicks Yes

    // simple message with OK button
    public MessageBox(Frame f, String msg)
    {
        this(f, msg, false);
    }

    // if askYesNo is true, it shows Yes and No buttons
    public MessageBox(Frame f, String msg, boolean askYesNo)
    {
        super(f, "Message", true);

        // setting the layout
        setLayout(new GridLayout(0, 1, 5, 5));

        // creating objects
        okBtn = new Button("   OK   ");
        yesBtn = new Button("   Yes   ");
        noBtn = new Button("   No   ");

        // design
        setBackground(new Color(232, 240, 250));
        setFont(new Font("Arial", Font.PLAIN, 15));

        // event handling
        okBtn.addActionListener(this);
        yesBtn.addActionListener(this);
        noBtn.addActionListener(this);

        // adding components (one label for every line of the message)
        String[] lines = msg.split("\n");
        for (int i = 0; i < lines.length; i++)
            add(new Label("     " + lines[i] + "     ", Label.CENTER));
        if (askYesNo)
        {
            add(yesBtn);
            add(noBtn);
        }
        else
            add(okBtn);

        // closing the window
        addWindowListener(this);

        pack();
        setSize(Math.max(320, getWidth()), getHeight());
        setLocationRelativeTo(f);
        setVisible(true); // program waits here till user clicks a button
    }

    // event handling method
    public void actionPerformed(ActionEvent e)
    {
        Button btn = (Button) e.getSource();
        if (btn == yesBtn)
            yes = true;
        dispose();
    }

    // window events (only closing is used, WindowListener needs all 7 methods)
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
