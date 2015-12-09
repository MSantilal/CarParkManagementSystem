package server;

import java.util.concurrent.atomic.AtomicInteger;


public class CarDataModel
{
    public static final AtomicInteger AtomicCount = new AtomicInteger(0);
    private int CarEntryId;
    public String CarMake;
    public String CarLicence;

    public CarDataModel(String carMake, String carLicence)
    {
        CarEntryId = AtomicCount.incrementAndGet();
        CarMake = carMake;
        CarLicence = carLicence;
    }
}
