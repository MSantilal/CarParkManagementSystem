package server;

import java.net.Socket;

public class CarParkProcessingThread extends Thread
{
    private Socket clientSocket;
    private ClientType clientType;
    private SharedCarParkState sharedCarParkState;

    public CarParkProcessingThread(Socket socket, ClientType clientType, SharedCarParkState sharedCarParkState)
    {
        super("CarParkProcessingThread");
        this.clientSocket = socket;
        this.clientType = clientType;
        this.sharedCarParkState = sharedCarParkState;
    }

    @Override
    public void run()
    {
        try
        {
            sharedCarParkState.AcquireLock();
            sharedCarParkState.ProcessEntry(clientType, new CarDataModel("Mercedes", "W05"));
            sharedCarParkState.ReleaseLock();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }
}
