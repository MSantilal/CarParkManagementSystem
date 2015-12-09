package server;

import com.sun.media.jfxmedia.logging.Logger;

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

    public ServerMain()
    {
        EstablishServerAvailability();
        CreateSocket();

        sharedCarParkState = new SharedCarParkState();

        if (IsConnected)
        {
            try
            {
                BeginProcessingClients();
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

    private void BeginProcessingClients() throws IOException
    {
        try
        {
            while (serverSocket.isBound())
            {
                new CarParkProcessingThread(serverSocket.accept(), sharedCarParkState).start();
            }
        }
        catch (Exception e)
        {
            Logger.logMsg(Logger.ERROR, e.getMessage());
        }
    }
}
