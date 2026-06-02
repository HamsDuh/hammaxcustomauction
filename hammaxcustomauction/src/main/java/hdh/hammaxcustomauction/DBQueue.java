package hdh.hammaxcustomauction;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DBQueue {

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private Thread workerThread;

    public void start(){

        workerThread = new Thread(() -> {
            while(true) {
                try {
                    Runnable job = queue.take();
                    job.run();
                }catch (Exception e){
                    System.out.println("DBQueue fehler /: ");
                    e.printStackTrace();
                }
            }
        });

        workerThread.setName("Auction-DB-Worker");
        workerThread.setDaemon(true);   //soll thread beiserverstop beenden
        workerThread.start();
    }

    public void addToQueue(Runnable task){
        queue.add(task);
    }







    //alte version

    /*
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private Thread workerThread;

    public void start(){

        workerThread = new Thread(() -> {
            while(true) {
                try {
                    Runnable job = queue.take();
                    job.run();
                }catch (Exception e){
                    System.out.println("DBQueue fehler /: ");
                    e.printStackTrace();
                }
            }
        });

        workerThread.setName("Auction-DB-Worker");
        workerThread.setDaemon(true);   //soll thread beiserverstop beenden
        workerThread.start();
    }

    public void addToQueue(Runnable task){
        queue.add(task);
    }
*/
}
