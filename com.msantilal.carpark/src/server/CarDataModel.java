package server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.atomic.AtomicInteger;

@XmlRootElement(name = "Car")
public class CarDataModel
{
    public static final AtomicInteger AtomicCount = new AtomicInteger(0);
    private int CarEntryId;

    public String getCarMake()
    {
        return CarMake;
    }

    public void setCarMake(String carMake)
    {
        CarMake = carMake;
    }

    @XmlAttribute(name = "Make")
    public String CarMake;

    public String getCarLicence()
    {
        return CarLicence;
    }

    public void setCarLicence(String carLicence)
    {
        CarLicence = carLicence;
    }

    @XmlAttribute(name = "Licence")
    public String CarLicence;

    public CarDataModel()
    {
        CarEntryId = AtomicCount.incrementAndGet();
    }
}
