package client;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class GroundFloorExitClient extends JFrame
{

    private org.apache.log4j.Logger Logger;
    private Socket clientSocket;
    private JPanel GroundFloorExit;
    private JButton connectToCarParkButton;
    private JButton disconnectFromCarParkButton;
    private JButton exitCarFromCarButton;
    private JLabel connectionStatus;
    private JLabel carsOnGroundFloor;
    private JLabel carsOnFirstFloor;

    private volatile boolean isConnected;
    private java.util.Timer Timer;


    public GroundFloorExitClient()
    {
        super("Ground Floor Exit Client");
        setContentPane(GroundFloorExit);

        Logger = org.apache.log4j.Logger.getLogger(this.getClass().getCanonicalName());

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
                outgoingXml.append("<Client Type=\"GROUNDFLOOREXIT\">");
                outgoingXml.append("</Client>\r\n");

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
                Timer = new Timer();
                Timer.scheduleAtFixedRate(new WorkerClass(), 0, 5 * 1000);
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


    class WorkerClass extends TimerTask
    {
        @Override
        public void run()
        {
            new SwingWorker<FloorSpaceDataModel, TimerTask>()
            {
                @Override
                protected FloorSpaceDataModel doInBackground() throws Exception
                {
                    try
                    {
                        if (isConnected)
                        {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                            String incomingParkingSpacesUpdate;

                            while ((incomingParkingSpacesUpdate = bufferedReader.readLine()) != null)
                            {
                                System.out.println(incomingParkingSpacesUpdate);

                                JAXBContext jaxbContext = JAXBContext.newInstance(FloorSpaceDataModel.class);
                                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                                StringReader stringReader = new StringReader(incomingParkingSpacesUpdate);
                                FloorSpaceDataModel deserialisedFloorInfo = (FloorSpaceDataModel) unmarshaller.unmarshal(stringReader);

                                return deserialisedFloorInfo;
                            }
                        }
                    }
                    catch (JAXBException e)
                    {
                        Logger.error(e.getMessage());
                        //e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        Logger.error(e.getMessage());
                        //e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void done()
                {
                    super.done();

                    try
                    {
                        if (isConnected)
                        {
                            FloorSpaceDataModel s = get();

                            for (final FloorInfo info : s.FloorInfoList)
                            {
                                if (info.Level == FloorLevel.GROUNDFLOOR)
                                {
                                    Logger.info("Current Number of Cars Parked on Ground Floor: " + info.SpaceCount);
                                    carsOnGroundFloor.setText(info.SpaceCount);
                                }
                                else if (info.Level == FloorLevel.FIRSTFLOOR)
                                {
                                    Logger.info("Current Number of Cars Parked on First Floor: " + info.SpaceCount);
                                    carsOnFirstFloor.setText(info.SpaceCount);
                                }
                            }
                        }
                    }
                    catch (InterruptedException e)
                    {
                        Logger.error(e.getMessage());
                        //e.printStackTrace();
                    }
                    catch (ExecutionException e)
                    {
                        Logger.error(e.getMessage());
                        //e.printStackTrace();
                    }
                }
            }.execute();
        }
    }
}
