package server;

import com.sun.media.jfxmedia.logging.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ServerMain
{
    private InetAddress localMachine;
    private ServerSocket serverSocket;

    public final SharedCarParkState sharedCarParkState;

    public ArrayList<CarDataModel> GroundFloorCollection;
    public ArrayList<CarDataModel> FirstFloorCollection;

    public boolean IsConnected;

    public ServerMain()
    {
        EstablishServerAvailability();
        CreateSocket();

        GroundFloorCollection = new ArrayList<CarDataModel>();
        FirstFloorCollection = new ArrayList<CarDataModel>();

        sharedCarParkState = new SharedCarParkState(GroundFloorCollection, FirstFloorCollection);

        if (IsConnected)
        {
            try
            {
                BeingProcessingClients();
            }
            catch (IOException e)
            {
                Logger.logMsg(Logger.ERROR, e.getMessage());
            }
        }

    }

    private void EstablishServerAvailability()
    {
        try
        {
            localMachine = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            Logger.logMsg(Logger.ERROR, e.getMessage());
        }
    }

    private void CreateSocket()
    {
        try
        {
            serverSocket = new ServerSocket(10031);
        }
        catch (IOException e)
        {
            Logger.logMsg(Logger.ERROR, e.getMessage());
        }
        finally
        {
            if (serverSocket.isBound())
            {
                Logger.logMsg(Logger.INFO, "Server Socket created. Car Park Server open at: " + localMachine.getHostAddress() + " on Port: " + serverSocket.getLocalPort());
                IsConnected = true;
            }
            else
            {
                Logger.logMsg(Logger.INFO, "Server Socket could not be created.");
                IsConnected = false;
            }
        }
    }

    private void BeingProcessingClients() throws IOException
    {
        try
        {
            while (serverSocket.isBound())
            {
                new CarParkProcessingThread(serverSocket.accept(), ClientType.ENTRANCE, sharedCarParkState).start();
            }
        }
        catch (Exception e)
        {
            Logger.logMsg(Logger.ERROR, e.getMessage());
        }
    }
}
