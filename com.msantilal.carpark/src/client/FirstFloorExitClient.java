package client;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class FirstFloorExitClient extends JFrame
{


    private boolean isConnected;
    private Socket clientSocket;
    private JPanel FirstFloorExit;
    private JButton connectToCarParkButton;
    private JButton disconnectFromCarParkButton;
    private JButton exitCarFromCarButton;
    private JLabel connectionStatus;
    private JLabel carsOnGroundFloor;
    private JLabel carsOnFirstFloor;


    public FirstFloorExitClient()
    {
        super("First Floor Exit Client");
        setContentPane(FirstFloorExit);

        connectionStatus.setVisible(false);
        disconnectFromCarParkButton.setEnabled(false);
        this.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent)
            {
                try
                {
                    Dispose();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        connectToCarParkButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    ConnectMain();

                    if (isConnected)
                    {
                        connectToCarParkButton.setEnabled(false);
                        disconnectFromCarParkButton.setEnabled(true);
                        connectionStatus.setVisible(true);
                    }
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });

        disconnectFromCarParkButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Dispose();

                    if (!isConnected)
                    {
                        connectToCarParkButton.setEnabled(true);
                        disconnectFromCarParkButton.setEnabled(false);
                        connectionStatus.setVisible(false);
                    }
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });


        pack();
        setVisible(true);
        exitCarFromCarButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                StringBuilder outgoingXml = new StringBuilder();
                outgoingXml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                outgoingXml.append("<Client Type=\"FIRSTFLOOREXIT\">");
                outgoingXml.append("</Client>");

                try
                {
                    SendData(outgoingXml.toString());
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
                catch (JAXBException e1)
                {
                    e1.printStackTrace();
                }

            }
        });
    }

    private void ConnectMain() throws IOException
    {
        try
        {
            clientSocket = new Socket("127.0.0.1", 10031);

            if (clientSocket.isConnected())
            {
                isConnected = clientSocket.isConnected();
            }
        }
        catch (Exception e)
        {

        }
    }

    private void SendData(String outgoingXml) throws IOException, JAXBException
    {
        if (isConnected)
        {
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            printWriter.println(outgoingXml);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String incomingParkingSpacesUpdate = bufferedReader.readLine();

            System.out.println(incomingParkingSpacesUpdate);

            JAXBContext jaxbContext = JAXBContext.newInstance(FloorSpaceDataModel.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            StringReader stringReader = new StringReader(incomingParkingSpacesUpdate);
            FloorSpaceDataModel deserialisedFloorInfo = (FloorSpaceDataModel) unmarshaller.unmarshal(stringReader);

            for (FloorInfo info : deserialisedFloorInfo.FloorInfoList)
            {
                if (info.Level == FloorLevel.GROUNDFLOOR)
                {
                    carsOnGroundFloor.setText(info.SpaceCount);
                }
                else if (info.Level == FloorLevel.FIRSTFLOOR)
                {
                    carsOnFirstFloor.setText(info.SpaceCount);
                }
            }

        }
    }

    private void Dispose() throws IOException
    {
        if (isConnected)
        {
            clientSocket.close();
            isConnected = false;
        }
    }

}
