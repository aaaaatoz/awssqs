import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

public class JMSSample {

    public static void main(String[] args) throws Exception {
        // Create the connection factory using the environment variable credential provider.
        // Connections this factory creates can talk to the queues in us-east-2 region.
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion(Regions.US_EAST_2)
                        //.withCredentials(new EnvironmentVariableCredentialsProvider())
                        .withCredentials(new ProfileCredentialsProvider())
        );

        // Create the connection.
        SQSConnection connection = connectionFactory.createConnection();

        // Get the wrapped client
        AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();

        // Create an SQS queue named TestQueue, if it does not already exist
        if (!client.queueExists("TestQueue")) {
            client.createQueue("TestQueue");
        }

        // Create an Amazon SQS FIFO queue named TestQueue.fifo, if it does not already exist
        if (!client.queueExists("TestQueue.fifo")) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("FifoQueue", "true");
            attributes.put("ContentBasedDeduplication", "true");
            client.createQueue(new CreateQueueRequest().withQueueName("TestQueue.fifo").withAttributes(attributes));
        }

        // Create the nontransacted session with AUTO_ACKNOWLEDGE mode
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


    }

}
