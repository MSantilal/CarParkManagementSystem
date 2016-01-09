package client;

import org.apache.log4j.Logger;
import server.CarDataModel;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class EntranceClient extends JFrame
{
    private Logger Logger;
    private JButton connectToCarParkButton;
    private JButton disconnectFromCarParkButton;
    private JComboBox carMakeCombo;
    private JComboBox carLicenceCombo;
    private JLabel carsOnGroundFloor;
    private JLabel carsOnFirstFloor;
    private JButton sendCarDetailsButton;
    private JPanel EntranceForm;
    private JLabel connectionStatus;
    private JLabel carsQueued;

    private volatile boolean isConnected;
    private Socket clientSocket;
    private Timer Timer;

    private final ArrayList<CarDataModel> queuedCarsCollection;

    public EntranceClient()
    {
        super("Entrance Client");
        setContentPane(EntranceForm);

        Logger = org.apache.log4j.Logger.getLogger(this.getClass().getCanonicalName());
        connectionStatus.setVisible(false);
        disconnectFromCarParkButton.setEnabled(false);
        carMakeCombo.addItem("Acura");
        carMakeCombo.addItem("Aston Martin");
        carMakeCombo.addItem("Audi");
        carMakeCombo.addItem("Bentley");
        carMakeCombo.addItem("BMW");
        carMakeCombo.addItem("Bugatti");
        carMakeCombo.addItem("Dodge");
        carMakeCombo.addItem("Ferrari");
        carMakeCombo.addItem("Fiat");
        carMakeCombo.addItem("Ford");
        carMakeCombo.addItem("Honda");
        carMakeCombo.addItem("Hyundai");
        carMakeCombo.addItem("Infiniti");
        carMakeCombo.addItem("Jaguar");
        carMakeCombo.addItem("Jeep");
        carMakeCombo.addItem("KIA");
        carMakeCombo.addItem("Koenigsegg");
        carMakeCombo.addItem("Land Rover");
        carMakeCombo.addItem("Lexus");
        carMakeCombo.addItem("Lotus");
        carMakeCombo.addItem("Maserati");
        carMakeCombo.addItem("Mazda");
        carMakeCombo.addItem("McLaren");
        carMakeCombo.addItem("Mercedes");
        carMakeCombo.addItem("MG");
        carMakeCombo.addItem("Mini");
        carMakeCombo.addItem("Mitsubishi");
        carMakeCombo.addItem("Nissan");
        carMakeCombo.addItem("Noble");
        carMakeCombo.addItem("Porsche");
        carMakeCombo.addItem("Rolls-Royce");
        carMakeCombo.addItem("SAAB");
        carMakeCombo.addItem("Smart");
        carMakeCombo.addItem("Subaru");
        carMakeCombo.addItem("Suzuki");
        carMakeCombo.addItem("Tesla");
        carMakeCombo.addItem("Toyota");
        carMakeCombo.addItem("Volkswagen");
        carMakeCombo.addItem("Volvo");

        for (int i = 0; i < 100; i++)
        {
            carLicenceCombo.addItem(LicencePlate.generateLicensePlate());
        }


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
        sendCarDetailsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedIndexMake = carMakeCombo.getSelectedIndex();
                String carMake = carMakeCombo.getItemAt(selectedIndexMake).toString();


                int selectedIndexLicence = carLicenceCombo.getSelectedIndex();
                String carLicence = carLicenceCombo.getItemAt(selectedIndexLicence).toString();

                try
                {
                    SendData(carMake, carLicence);
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

        queuedCarsCollection = new ArrayList<CarDataModel>();

    }


    private void ConnectMain() throws IOException
    {
        try
        {
            Logger.info("Opening Client Connection..");
            clientSocket = new Socket("127.0.0.1", 10031);

            if (clientSocket.isConnected())
            {
                Logger.info("Connected to Server on: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                isConnected = clientSocket.isConnected();
                Timer = new Timer();
                Timer.scheduleAtFixedRate(new WorkerClass(), 0, 2 * 1000);
                Logger.info("Polling timer enabled to run at intervals of 2 seconds");
            }
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage());
        }
    }

    private void SendData(String carMake, String carLicence) throws IOException, JAXBException
    {
        if (isConnected)
        {
            Logger.info("User Requested to Send Car Data");

            StringBuilder outgoingXml = new StringBuilder();
            outgoingXml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            outgoingXml.append("<Client Type=\"ENTRANCE\"><Car Make=\"" + carMake);
            outgoingXml.append("\" Licence=\"" + carLicence);
            outgoingXml.append("\"/>");
            outgoingXml.append("</Client>\r\n");

            if (Integer.parseInt(carsOnGroundFloor.getText()) != 20 || Integer.parseInt(carsOnFirstFloor.getText()) != 20)
            {
                Logger.info("Car Park has spaces available to Park in.");
                PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                printWriter.println(outgoingXml);
            }
            else
            {
                Logger.info("No free spaces in Car Park. Car to be queued at Entrance.");
                CarDataModel carModel = new CarDataModel();
                carModel.setCarMake(carMake);
                carModel.setCarLicence(carLicence);
                queuedCarsCollection.add(carModel);
                Logger.info("Car Queued - Make: " + carMake + " Licence: " + carLicence);
            }

        }
    }

    private void Dispose() throws IOException
    {
        if (isConnected)
        {
            Logger.info("Client Disconnected from Server. Disposing all resources.");
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
                                Logger.info("Received Space Info from Server: " + incomingParkingSpacesUpdate);

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
                    }
                    catch (IOException e)
                    {
                        Logger.error(e.getMessage());
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

                            carsQueued.setText(Integer.toString(queuedCarsCollection.size()));

                            int groundFloorSpace = 0;
                            int firstFloorSpace = 0;

                            for (final FloorInfo info : s.FloorInfoList)
                            {
                                if (info.Level == FloorLevel.GROUNDFLOOR)
                                {
                                    Logger.info("Current Number of Cars Parked on Ground Floor: " + info.SpaceCount);
                                    carsOnGroundFloor.setText(info.SpaceCount);
                                    groundFloorSpace = Integer.parseInt(info.SpaceCount);
                                }
                                else if (info.Level == FloorLevel.FIRSTFLOOR)
                                {
                                    Logger.info("Current Number of Cars Parked on First Floor: " + info.SpaceCount);
                                    carsOnFirstFloor.setText(info.SpaceCount);
                                    firstFloorSpace = Integer.parseInt(info.SpaceCount);
                                }
                            }

                            if (queuedCarsCollection.size() > 0)
                            {
                                Logger.info("Queued Cars Found. ");

                                int differenceGF;
                                int differenceFF;

                                differenceGF = 20 - groundFloorSpace;
                                differenceFF = 20 - firstFloorSpace;

                                if (differenceGF != 0)
                                {
                                    Logger.info("Spaces available on Ground Floor: " + differenceGF + "Dequeueing Cars...");
                                    //do groundfloor
                                    for (int i = 0; i < differenceGF; i++)
                                    {
                                        CarDataModel model = queuedCarsCollection.get(i);
                                        SendData(model.CarMake, model.CarLicence);
                                        queuedCarsCollection.remove(i);
                                    }
                                }

                                if (differenceFF != 0)
                                {
                                    Logger.info("Spaces available on First Floor: " + differenceFF + "Dequeueing Cars...");
                                    //do firstfloor
                                    for (int i = 0; i < differenceFF; i++)
                                    {
                                        CarDataModel model = queuedCarsCollection.get(i);
                                        SendData(model.CarMake, model.CarLicence);
                                        queuedCarsCollection.remove(i);
                                    }
                                }
                            }
                        }
                    }
                    catch (InterruptedException e)
                    {
                        Logger.error(e.getMessage());
                    }
                    catch (ExecutionException e)
                    {
                        Logger.error(e.getMessage());
                    }
                    catch (JAXBException e)
                    {
                        Logger.error("XML Parsing Error: " + e.getMessage());
                    }
                    catch (IOException e)
                    {
                        Logger.error(e.getMessage());
                    }
                }
            }.execute();
        }
    }
}
