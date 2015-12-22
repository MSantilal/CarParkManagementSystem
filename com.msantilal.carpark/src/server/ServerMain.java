package server;



import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class ServerMain
{
    private InetAddress localMachine;
    private ServerSocket serverSocket;

    public final SharedCarParkState sharedCarParkState;

    public boolean IsConnected;

    public Logger Logger;


    public ServerMain()
    {
        Logger = Logger.getLogger(this.getClass().getCanonicalName());

        EstablishServerAvailability();
        CreateSocket();

        sharedCarParkState = new SharedCarParkState(Logger);

        if (IsConnected)
        {
            try
            {
                BeginProcessingClients();
            }
            catch (IOException e)
            {
                Logger.error(e.getMessage());
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
            Logger.error(e.getMessage());
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
            Logger.error(e.getMessage());
        }
        finally
        {
            if (serverSocket.isBound())
            {
                Logger.info("Server Socket created. Car Park Server open at: " + localMachine.getHostAddress() + " on Port: " + serverSocket.getLocalPort());
                IsConnected = true;
            }
            else
            {
                Logger.info("Server Socket could not be created.");
                IsConnected = false;
            }
        }
    }

    private void BeginProcessingClients() throws IOException
    {
        try
        {
            while (serverSocket.isBound())
            {
                Logger.info("New Client Connected.");
                new CarParkProcessingThread(serverSocket.accept(), sharedCarParkState, Logger).start();
            }
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage());
        }
    }
}
