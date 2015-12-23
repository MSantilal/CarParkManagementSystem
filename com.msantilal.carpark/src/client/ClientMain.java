package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientMain extends JFrame
{
    private JButton entranceButton;
    private JButton groundFloorExitButton;
    private JButton firstFloorExitButton;
    private javax.swing.JPanel JPanel;

    public ClientMain()
    {
        super("Advanced Car Park System - Student ID: 1115752 (MONIL SANTILAL)");


        setContentPane(JPanel);


        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        entranceButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new EntranceClient();
            }
        });


        setVisible(true);
        groundFloorExitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new GroundFloorExitClient();
            }
        });
        firstFloorExitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new FirstFloorExitClient();
            }
        });
    }
}
