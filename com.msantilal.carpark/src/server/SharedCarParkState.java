package server;


import org.apache.log4j.Logger;

import java.util.ArrayList;

public class SharedCarParkState
{
    private boolean isAccessing = false;
    private int waitingThreads = 0;

    private final ArrayList<CarDataModel> groundFloorCollection;
    private final ArrayList<CarDataModel> firstFloorCollection;
    private final ArrayList<CarDataModel> queuedCarsCollection;

    public Logger Logger;

    public SharedCarParkState(Logger logger)
    {
        this.Logger = logger;

        groundFloorCollection = new ArrayList<CarDataModel>();
        firstFloorCollection = new ArrayList<CarDataModel>();
        queuedCarsCollection = new ArrayList<CarDataModel>();

    }

    public synchronized void AcquireLock() throws InterruptedException
    {
        Thread me = Thread.currentThread(); // get a ref to the current thread
        Logger.debug(me.getName() + ": Attempting to acquire a lock.");
        waitingThreads++;
        while (isAccessing)
        {  // while someone else is accessing or threadsWaiting > 0
            Logger.debug(me.getName() + ": Waiting to get a lock on as it is currently held by another thread.");
            //wait for the lock to be released - see releaseLock() below
            wait();
        }
        Logger.debug("No active thread currently has a lock on. Free for all active!");
        // nobody has got a lock so get one
        waitingThreads--;
        isAccessing = true;
        Logger.debug(me.getName() + ": Has acquired a lock on.");
    }

    public synchronized void ReleaseLock()
    {
        //release the lock and tell everyone
        Logger.debug("Releasing Lock...");
        isAccessing = false;
        notifyAll();

        Thread me = Thread.currentThread(); // get a ref to the current thread
        Logger.debug(me.getName() + ": Has released a lock.");
    }

    public synchronized void ProcessEntry(ClientType clientType, ClientDataModel clientData)
    {
        Logger.info("Car Data received from " + clientType.toString());

        if (groundFloorCollection.size() < 20)
        {
            Logger.info("Ground Floor free to park cars.");
            Logger.info("Number of Cars on Ground Floor: " + groundFloorCollection.size());
            groundFloorCollection.add(clientData.CarDataModel);
            Logger.info("Car Parked - Details: Make: " + clientData.CarDataModel.CarMake + " Licence Plate: " + clientData.CarDataModel.CarLicence);
            Logger.info("NEW CAR PARKED - Number of Cars on Ground Floor: " + groundFloorCollection.size());
        }
        else if (firstFloorCollection.size() < 20)
        {
            Logger.info("First Floor free to park cars.");
            Logger.info("Number of Cars on First Floor: " + firstFloorCollection.size());
            firstFloorCollection.add(clientData.CarDataModel);
            Logger.info("Car Parked - Details: Make: " + clientData.CarDataModel.CarMake + " Licence Plate: " + clientData.CarDataModel.CarLicence);
            Logger.info("NEW CAR PARKED - Number of Cars on First Floor: " + firstFloorCollection.size());
        }
        else
        {
            //queuedCarsCollection.add(carData);

            //Extra Cars -- Double check.
        }
    }

    public synchronized void ProcessExit(ClientType clientType)
    {
        Logger.info("Exit Request received from " + clientType.toString());

        switch (clientType)
        {
            case GROUNDFLOOREXIT:
                Logger.info("Number of Cars on Ground Floor: " + groundFloorCollection.size());
                groundFloorCollection.remove(groundFloorCollection.size() - 1);
                Logger.info("Car Removed. Current number of Cars on GF: " + groundFloorCollection.size());
                break;
            case FIRSTFLOOREXIT:
                Logger.info("Number of Cars on First Floor: " + firstFloorCollection.size());
                firstFloorCollection.remove(firstFloorCollection.size() - 1);
                Logger.info("Car Removed. Current number of Cars on FF: " + firstFloorCollection.size());
                break;
        }

//        if (queuedCarsCollection.size() > 0)
//        {
//            Logger.logMsg(Logger.INFO, "Queued Cars Found. ");
//            if (groundFloorCollection.size() < 20)
//            {
//                groundFloorCollection.add(carData);
//                return groundFloorCollection;
//            }
//            else if (firstFloorCollection.size() < 20)
//            {
//                firstFloorCollection.add(carData);
//                return firstFloorCollection;
//            }
//        }


//        Lock lock = new ReentrantLock(true);
//
//        lock.lock();
//        //insert
//        lock.unlock();


    }
}
