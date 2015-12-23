package server;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.Socket;

public class CarParkProcessingThread extends Thread
{
    private Socket clientSocket;
    private SharedCarParkState sharedCarParkState;

    public Logger Logger;

    public CarParkProcessingThread(Socket socket, SharedCarParkState sharedCarParkState, Logger logger)
    {
        super("CarParkProcessingThread");
        this.clientSocket = socket;
        this.sharedCarParkState = sharedCarParkState;
        this.Logger = logger;
    }

    @Override
    public void run()
    {
        if (clientSocket != null && clientSocket.isConnected())
        {
            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);

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
                                printWriter.println(sharedCarParkState.UpdateFloorSpaces());
                                printWriter.flush();
                                break;
                            case GROUNDFLOOREXIT:
                                sharedCarParkState.ProcessExit(ClientType.GROUNDFLOOREXIT);
                                printWriter.println(sharedCarParkState.UpdateFloorSpaces());
                                printWriter.flush();
                                break;
                            case FIRSTFLOOREXIT:
                                sharedCarParkState.ProcessExit(ClientType.FIRSTFLOOREXIT);
                                printWriter.println(sharedCarParkState.UpdateFloorSpaces());
                                printWriter.flush();
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

    private void Dispose() throws IOException
    {
        clientSocket.close();
        clientSocket = null;
    }
}

