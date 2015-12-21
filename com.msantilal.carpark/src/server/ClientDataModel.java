package server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Client", namespace = "")
public class ClientDataModel
{
    @XmlAttribute(name = "Type")
    public ClientType ClientType;

    @XmlElement(name = "Car")
    public CarDataModel CarDataModel;

    public ClientType getClientType()
    {
        return ClientType;
    }

    public void setClientType(ClientType clientType)
    {
        ClientType = clientType;
    }

    public CarDataModel getCarDataModel()
    {
        return CarDataModel;
    }

    public void setCarDataModel(CarDataModel carDataModel)
    {
        CarDataModel = carDataModel;
    }



}


