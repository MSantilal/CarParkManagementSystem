package server;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;

public class CarParkProcessingThread extends Thread
{
    private Socket clientSocket;
    private SharedCarParkState sharedCarParkState;
    public Logger Logger;

    public CarParkProcessingThread(Socket socket, SharedCarParkState sharedCarParkState)
    {
        super("CarParkProcessingThread");
        this.clientSocket = socket;
        this.sharedCarParkState = sharedCarParkState;
        Logger = Logger.getLogger(this.getClass().getCanonicalName());
    }

    @Override
    public void run()
    {
        if (clientSocket != null && clientSocket.isConnected())
        {
            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputString;

                while ((inputString = bufferedReader.readLine()) != null)
                {
                    if (inputString.contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"))
                    {
                        JAXBContext jaxbContext = JAXBContext.newInstance(ClientDataModel.class);
                        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                        StringReader stringReader = new StringReader(inputString);
                        ClientDataModel deserialisedClientData = (ClientDataModel) unmarshaller.unmarshal(stringReader);

                        sharedCarParkState.AcquireLock();
                        Logger.info("Incoming Client Request from: " + deserialisedClientData.getClientType());
                        switch (deserialisedClientData.getClientType())
                        {
                            case ENTRANCE:
                                sharedCarParkState.ProcessEntry(ClientType.ENTRANCE, deserialisedClientData);
                                break;
                            case GROUNDFLOOREXIT:
                                sharedCarParkState.ProcessExit(ClientType.GROUNDFLOOREXIT);
                                break;
                            case FIRSTFLOOREXIT:
                                sharedCarParkState.ProcessExit(ClientType.FIRSTFLOOREXIT);
                                break;
                        }
                        sharedCarParkState.ReleaseLock();
                    }
                }
            }
            catch (InterruptedException e)
            {
                Logger.error(e.getMessage());
            }
            catch (IOException e)
            {
                Logger.error(e.getMessage());
            }
            catch (JAXBException e)
            {
                Logger.error(e.getMessage());
            }
        }
    }


}

