import org.json.JSONObject;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * @author ismet abacı
 */
public class MyThread extends Thread{

    final static Logger logger = Logger.getLogger(String.valueOf(MyThread.class));
    final int CAPACITY = 10;
    private static Semaphore mutex = new Semaphore(10);
    int line=0,next=0;
    int name;
    JSONObject [] msg = new JSONObject[CAPACITY];

    /**
     * this is a constructor method which changes the thread's priority with the given integer argument
     * @param threadPriority this is the thread's new priority
     */
    public MyThread (int threadPriority){
        Thread.currentThread().setPriority(threadPriority);
    }

    /**
     * this method checks the queue and processes it
     */
    @Override
    public void run() {
        while(true){
            while(line==0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warning("Error on Thread -> " + e);
                }
            }
            writeMessageToFile(msg[next]);
            insertIntoDatabase(msg[next]);
            next = (next+1)%CAPACITY;
            line--;
        }
    }

    /**
     * this method checks, is the queue full or not, if it is full then it waits then it adds a new message to the queue
     */
    public void addIntoQueue(JSONObject msg)  {
        while(line>=CAPACITY);
        this.msg[next+line] = msg;
        this.line++;
    }

    /**
     *
     * this method inserts the messages into a database according to message's priority
     * @param m this is the message we got from the user
     */
    private static void insertIntoDatabase(JSONObject m){

        String url = "jdbc:mysql://192.168.64.2:3306/";
        String databaseName = "messages";
        String userName="root2";
        String password="111";
        Connection connect;
        PreparedStatement preparedStatement;
        try{
            String tableName = (String) m.get("priority"); //get table name according to the message's priority
            if (tableName == null) return;
            connect = DriverManager.getConnection(url + databaseName +"?user="+ userName +"&password=" + password); //WARNING: I used xampp for the db Connection
            String sql = "insert into " + tableName + " (id,sender,receiver,subject,cc,message,priority) values (default, ?, ?, ?, ? , ?, ?);";
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setString(1, m.get("sender").toString());
            preparedStatement.setString(2, m.get("receiver").toString());
            preparedStatement.setString(3, m.get("subject").toString());
            preparedStatement.setString(4, m.get("cc").toString());
            preparedStatement.setString(5, m.get("message").toString());
            preparedStatement.setString(6, m.get("priority").toString());
            preparedStatement.executeUpdate();
        }catch(Exception e){
            logger.warning("Error on client-db conn -> " + e);
        }
    }

    /**
     * this method writes the message to a file according to message priority
     * @param m this is the message we got from the user
     */
    private static void writeMessageToFile(JSONObject m){
        String data = m.get("sender").toString() + "," + m.get("receiver").toString() + "," + m.get("subject").toString() + "," + m.get("cc").toString() + "," + m.get("message").toString() + "," + m.get("priority").toString();
        String fileName = m.get("priority").toString() + ".txt";
        try{
            FileWriter fw = new FileWriter(fileName,true);
            PrintWriter printWriter = new PrintWriter(fw);
            printWriter.println(data);
            printWriter.close();
        }catch(Exception e ){
            logger.warning("Error on server-fileWriter -> " + e);
        }
    }
}


