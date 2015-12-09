package server;

/**
 * Created by Monil on 09/12/2015.
 */
public class ClientDataModel extends CarDataModel
{
    public ClientType ClientType;

    public ClientDataModel(ClientType clientType, String carMake, String carLicence)
    {
        super(carMake, carLicence);
        this.ClientType = clientType;
    }
}
