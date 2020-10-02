import org.json.JSONObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * This class listens a specific port then, when it receives a message, it writes it to a file and inserts it to a database according to its priority
 *
 * @author ismet abacı
 */
public class Server {
    final static Logger logger = Logger.getLogger(String.valueOf(Server.class));

    /**
     * this method listens for a message on a port and process received messages
     *
     * @param args
     */
    public static void main(String[] args) {
        int port = 49999;
        try {
            logger.info("Server started listening (port:" + port + ")...");
            runServer(port);
        } catch (Exception e) {
            logger.warning("Error on Server-client conn -> " + e);
        }
    }

    /**
     * This method listens the given port as long as it runs. when it receives a message, it processes it
     *
     * @param portNumber
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void runServer(int portNumber) throws IOException, ClassNotFoundException, InterruptedException {
        ServerSocket ss = new ServerSocket(portNumber);
        Socket s;
        BufferedReader in;
        JSONObject msg = new JSONObject();
        String input;

        MyThread highThread = new MyThread(Thread.MIN_PRIORITY);
        MyThread normalThread = new MyThread(Thread.NORM_PRIORITY);
        MyThread lowThread = new MyThread(Thread.MAX_PRIORITY);

        highThread.start();
        normalThread.start();
        lowThread.start();

        while (true) { //Question: is this the right usage??
            s = ss.accept();
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            if ((input = in.readLine()) != null) {
                msg = new JSONObject(input);
                switch (msg.get("priority").toString()){
                    case "high":
                        highThread.addIntoQueue(msg);// adds a new message to the thread's queue
                        break;
                    case "normal":
                        normalThread.addIntoQueue(msg);
                        break;
                    case "low":
                        lowThread.addIntoQueue(msg);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + msg.get("priority").toString());
                }
            } else {
                logger.warning("Error on server -> Received message is null");
            }
        }
    }
}
