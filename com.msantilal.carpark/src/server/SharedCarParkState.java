package server;

import com.sun.media.jfxmedia.logging.Logger;

import java.util.ArrayList;

public class SharedCarParkState
{
    private boolean isAccessing = false;
    private int waitingThreads = 0;

    private final ArrayList<CarDataModel> groundFloorCollection;
    private final ArrayList<CarDataModel> firstFloorCollection;
    private final ArrayList<CarDataModel> queuedCarsCollection;

    public SharedCarParkState()
    {
        groundFloorCollection = new ArrayList<CarDataModel>();
        firstFloorCollection = new ArrayList<CarDataModel>();
        queuedCarsCollection = new ArrayList<CarDataModel>();
    }

    public synchronized void AcquireLock() throws InterruptedException
    {
        Thread me = Thread.currentThread(); // get a ref to the current thread
        System.out.println(me.getName() + " is attempting to acquire a lock!");
        waitingThreads++;
        while (isAccessing)
        {  // while someone else is accessing or threadsWaiting > 0
            System.out.println(me.getName() + " waiting to get a lock as someone else is accessing...");
            //wait for the lock to be released - see releaseLock() below
            wait();
        }

        // nobody has got a lock so get one
        waitingThreads--;
        isAccessing = true;
        System.out.println(me.getName() + " got a lock!");
    }

    // Releases a lock to when a thread is finished

    public synchronized void ReleaseLock()
    {
        //release the lock and tell everyone
        isAccessing = false;
        notifyAll();
        Thread me = Thread.currentThread(); // get a ref to the current thread
        System.out.println(me.getName() + " released a lock!");
    }

    public synchronized void ProcessEntry(ClientType clientType, CarDataModel carData)
    {
        Logger.logMsg(Logger.INFO, "Car Data received from " + clientType.toString());

        if (groundFloorCollection.size() < 20)
        {
            groundFloorCollection.add(carData);
        }
        else if (firstFloorCollection.size() < 20)
        {
            firstFloorCollection.add(carData);
        }
        else
        {
            //queuedCarsCollection.add(carData);

            //Extra Cars -- Double check.
        }


    }

    public synchronized void ProcessExit(ClientType clientType)
    {
        Logger.logMsg(Logger.INFO, "Exit Request received from " + clientType.toString());

        switch (clientType)
        {
            case GROUNDFLOOREXIT:
                groundFloorCollection.remove(groundFloorCollection.size() - 1);
                break;
            case FIRSTFLOOREXIT:
                firstFloorCollection.remove(firstFloorCollection.size() - 1);
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
