package client;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class EntranceClient extends JFrame
{
    private JButton connectToCarParkButton;
    private JButton disconnectFromCarParkButton;
    private JComboBox carMakeCombo;
    private JComboBox carLicenceCombo;
    private JLabel carsOnGroundFloor;
    private JLabel carsOnFirstFloor;
    private JButton sendCarDetailsButton;
    private JPanel EntranceForm;
    private JLabel connectionStatus;

    private boolean isConnected;
    private Socket clientSocket;


    public EntranceClient()
    {
        super("Entrance Client");
        setContentPane(EntranceForm);

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

                StringBuilder outgoingXml = new StringBuilder();
                outgoingXml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                outgoingXml.append("<Client Type=\"ENTRANCE\"><Car Make=\"" + carMake);
                outgoingXml.append("\" Licence=\"" + carLicence);
                outgoingXml.append("\"/>");
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
            //
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
        clientSocket.close();
        isConnected = false;
    }

}
