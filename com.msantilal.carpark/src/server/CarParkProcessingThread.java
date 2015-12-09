package server;

import com.thoughtworks.xstream.XStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class CarParkProcessingThread extends Thread
{
    private Socket clientSocket;
    private SharedCarParkState sharedCarParkState;
    private XStream xStream;

    public CarParkProcessingThread(Socket socket, SharedCarParkState sharedCarParkState)
    {
        super("CarParkProcessingThread");
        this.clientSocket = socket;
        this.sharedCarParkState = sharedCarParkState;
        xStream = new XStream();
    }

    @Override
    public void run()
    {
        if (clientSocket != null && clientSocket.isConnected())
        {
            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                try
                {
                    while (bufferedReader.readLine() != null)
                    {
                        ClientDataModel deserialisedClientData = (ClientDataModel) xStream.fromXML(bufferedReader.readLine());
                        sharedCarParkState.AcquireLock();
                        switch (deserialisedClientData.ClientType)
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
                catch (IOException e)
                {
                    e.printStackTrace();
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

        }

    }
}
