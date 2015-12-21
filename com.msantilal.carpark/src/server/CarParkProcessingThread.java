package server;

import com.thoughtworks.xstream.XStream;

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
    private XStream xStream;

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
                String inputString = null;
                StringBuilder sb = new StringBuilder();

//                for (Iterator<String> i = bufferedReader.lines().iterator();;)
//                {
//                    String item = i.next();
//                    System.out.println(sb.toString());
//                    if (!i.equals(""))
//                    {
//                        sb.append(item);
//                    }
//                    else
//                    {
//                        break;
//                    }
//                }

                sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                sb.append("<Client Type=\"ENTRANCE\"><Car Make=\"Ferrari\" Licence=\"SF15-T\"/>");
                sb.append("</Client>");


                String tempString = sb.toString();
                System.out.println(tempString);

                JAXBContext jaxbContext = JAXBContext.newInstance(ClientDataModel.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                StringReader stringReader = new StringReader(sb.toString());
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
}
