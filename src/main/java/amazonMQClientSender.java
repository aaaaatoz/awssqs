import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class amazonMQClientSender  {

    public static void main(String[] args)  {
        Thread thread = new Thread(new sender());
        thread.start();
    }
}

class sender implements Runnable {
    public void run() {
        System.out.println("sending msg.");
        // Create a connection factory.
        String failoverConnection = "failover:(ssl://b-2353da1e-694c-416e-bd90-f7b74b92f5a6-1.mq.us-west-2.amazonaws.com:61617,ssl://b-2353da1e-694c-416e-bd90-f7b74b92f5a6-2.mq.us-west-2.amazonaws.com:61617)";
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(failoverConnection);

        // Specify the username and password.
        connectionFactory.setUserName("root");
        connectionFactory.setPassword("mypassword");

        // Create a pooled connection factory.
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(connectionFactory);
        pooledConnectionFactory.setMaxConnections(10);
        try {
            // Establish a connection for the producer.
            Connection producerConnection = pooledConnectionFactory.createConnection();
            producerConnection.start();

            // Create a session.
            Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create a queue named "MyQueue".
            Destination producerDestination = producerSession.createQueue("NewQueue");

            // Create a producer from the session to the queue.
            MessageProducer producer = producerSession.createProducer(producerDestination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);


            // Create a message.
            String text = "Hello from Amazon MQ!";
            TextMessage producerMessage = producerSession.createTextMessage(text);

            long delay = 1 * 1000;
            producerMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            System.out.println("Messages sending started at:" + dateFormat.format(date));
            for (int index = 1 ; index <= 100000; index++) {
                producer.send(producerMessage);
                if (index % 1000 == 0) {
                    date = new Date();
                    System.out.println("sending " + index + " message at:" + dateFormat.format(date));
                }
            }

            date = new Date();
            System.out.println("Messages sending stopped at:" + dateFormat.format(date));

        } catch (Exception e) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            date = new Date();
            System.out.println("Exception happened at:" + dateFormat.format(date));
            System.out.println(e.toString());
        }
    }
}
