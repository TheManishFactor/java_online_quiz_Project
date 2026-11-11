package com.onlinequiz.util;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Educational demonstration of the AWT delegation event model. The main
 * application itself stays JavaFX; this small window only illustrates how the
 * same concepts (source, event object, listener, registration) work in AWT.
 *
 * Run with:
 * java -cp "target/classes" com.onlinequiz.util.EventHandlingDemo
 */
public final class EventHandlingDemo {

    private EventHandlingDemo() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("AWT is headless here, so the event demo cannot open a window.");
            return;
        }
        SwingUtilities.invokeLater(EventHandlingDemo::createWindow);
    }

    private static void createWindow() {
        JFrame frame = new JFrame("AWT Delegation Event Model");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(560, 300);
        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel("Delegation event model demonstration");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(0x64, 0x8C, 0xFF));

        JLabel status = new JLabel("Type in the field, click the button, or click the heading.");
        JTextField input = new JTextField("type here");
        JButton button = new JButton("Raise ActionEvent");

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(new Color(0x0B, 0x12, 0x26));
        title.setOpaque(true);
        title.setBackground(new Color(0x0B, 0x12, 0x26));
        status.setForeground(new Color(0xDF, 0xE7, 0xF7));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        content.add(title, BorderLayout.NORTH);
        content.add(input, BorderLayout.CENTER);
        content.add(button, BorderLayout.EAST);
        content.add(status, BorderLayout.SOUTH);

        // EVENT SOURCE: input. EVENT OBJECT: KeyEvent.
        // LISTENER: KeyAdapter. REGISTRATION: addKeyListener.
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                status.setText("KeyEvent: keyCode=" + event.getKeyCode()
                        + " char='" + event.getKeyChar() + "'");
            }
        });

        // EVENT SOURCE: button. EVENT OBJECT: ActionEvent.
        // LISTENER: ActionListener. REGISTRATION: addActionListener.
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                status.setText("ActionEvent: " + event.getActionCommand()
                        + " from " + event.getSource().getClass().getSimpleName());
            }
        });

        // EVENT SOURCE: title label. EVENT OBJECT: MouseEvent.
        // LISTENER: MouseAdapter (adapter class). REGISTRATION: addMouseListener.
        title.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                status.setText("MouseEvent: clicked at " + event.getX() + ", " + event.getY());
            }
        });

        // EVENT SOURCE: frame. EVENT OBJECT: WindowEvent.
        // LISTENER: WindowAdapter. REGISTRATION: addWindowListener.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                status.setText("WindowAdapter caught windowClosing; exiting demo.");
                frame.dispose();
            }
        });

        frame.setContentPane(content);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        System.out.println("AWT demo window opened. Close it to return to the console.");
    }
}
