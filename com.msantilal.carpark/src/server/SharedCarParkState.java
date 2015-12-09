package server;

import com.sun.media.jfxmedia.logging.Logger;

import java.util.ArrayList;

public class SharedCarParkState
{
    private ClientType clientType;
    private boolean isAccessing = false;
    private int waitingThreads = 0;

    private ArrayList<CarDataModel> groundFloorCollection;
    private ArrayList<CarDataModel> firstFloorCollection;

    public SharedCarParkState()
    {
        groundFloorCollection = new ArrayList<CarDataModel>();
        firstFloorCollection = new ArrayList<CarDataModel>();
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

    public synchronized ArrayList<CarDataModel> ProcessEntry(ClientType clientType, CarDataModel carData)
    {
        Logger.logMsg(Logger.INFO, "Car Data received from " + clientType.toString());

        System.out.println("I'm here...");
        if (groundFloorCollection.size() < 20)
        {
            groundFloorCollection.add(carData);
            System.out.println("Ground: " + groundFloorCollection.size());
            return groundFloorCollection;
        }
        else if (firstFloorCollection.size() < 20)
        {
            firstFloorCollection.add(carData);
            return firstFloorCollection;
        }
        else
        {
            //add to waiting collection
            //once exit has been processed
            //check if there is anyone waiting
        }

        return null;

    }

    public synchronized ArrayList<CarDataModel> ProcessExit(ClientType clientType, CarDataModel carData)
    {
        Logger.logMsg(Logger.INFO, "Exit Request received from " + clientType.toString());

        switch (clientType)
        {
            case GROUNDFLOOREXIT:
                groundFloorCollection.remove(groundFloorCollection.size() - 1);
                return groundFloorCollection;
            case FIRSTFLOOREXIT:
                firstFloorCollection.remove(firstFloorCollection.size() - 1);
                return firstFloorCollection;
        }

//        Lock lock = new ReentrantLock(true);
//
//        lock.lock();
//        //insert
//        lock.unlock();

        return null;

    }
}
