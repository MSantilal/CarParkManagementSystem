package server;


import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerMain
{
    private InetAddress localMachine;
    private ServerSocket serverSocket;
    private volatile List<Socket> SocketList;
    public final SharedCarParkState sharedCarParkState;
    private Timer timer;

    public Logger Logger;


    public ServerMain()
    {
        Logger = Logger.getLogger(this.getClass().getCanonicalName());
        SocketList = new ArrayList<Socket>();
        EstablishServerAvailability();
        CreateSocket();

        sharedCarParkState = new SharedCarParkState();
        timer = new Timer();

        if (serverSocket.isBound())
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
            }
            else
            {
                Logger.info("Server Socket could not be created.");
            }
        }
    }

    private void BeginProcessingClients() throws IOException
    {
        try
        {
            while (serverSocket.isBound())
            {
                Logger.info("Socket Bound. Waiting for Connection.");
                Socket sock = serverSocket.accept();
                Logger.info("A Client has connected!");
                SocketList.add(sock);
                Logger.info(sock.getInetAddress().getAddress().toString() + " Added to SocketList. " +
                        "Current number of connected clients: " + SocketList.size());
                timer = null;
                timer = new Timer();
                timer.scheduleAtFixedRate(new UpdateSpaces(), 0, 5 * 1000);
                new CarParkProcessingThread(sock, sharedCarParkState).start();
            }
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage());
        }
    }

    class UpdateSpaces extends TimerTask
    {
        @Override
        public void run()
        {
            for (Object s : SocketList.toArray())
            {
                try
                {
                    sharedCarParkState.AcquireLock();
                    try
                    {
                        if (s != null)
                        {
                            Socket sock = (Socket) s;
                            sock.getOutputStream().write(sharedCarParkState.UpdateFloorSpaces().getBytes());
                        }
                    }
                    catch (IOException e)
                    {
                        Logger.info("Client Disconnected");
                        SocketList.remove(s);
                    }
                    sharedCarParkState.ReleaseLock();

                }
                catch (InterruptedException e)
                {
                    Logger.error("Updating of spaces interrupted. " + e.getMessage());
                }
            }
        }
    }
}

