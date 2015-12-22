package server;

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

    public CarParkProcessingThread(Socket socket, SharedCarParkState sharedCarParkState)
    {
        super("CarParkProcessingThread");
        this.clientSocket = socket;
        this.sharedCarParkState = sharedCarParkState;
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
                    else if (inputString.contains("DISCONNECT"))
                    {
                        Dispose();
                        break;
                    }
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (JAXBException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void Dispose() throws IOException
    {
        clientSocket.close();
        clientSocket = null;
    }
}

