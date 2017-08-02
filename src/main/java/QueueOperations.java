import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rafaxu on 7/24/17.
 */
public class QueueOperations {
    public static void main(String[] args) throws Exception {


        AmazonSQS sqs = AmazonSQSClientBuilder
                .standard()
                //.withCredentials(new EnvironmentVariableCredentialsProvider())
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(Regions.US_EAST_2)
                .build();

        try {
            System.out.println("===========================================");
            System.out.println("Set Redrive policy");
            System.out.println("===========================================\n");
            // Create a queue
            // make sure the DLQ exist
            final String myQueueUrl = "https://sqs.us-east-2.amazonaws.com/620428855768/SQSSDKDemo";
            final String dlqARN = "arn:aws:sqs:us-east-2:620428855768:TestQueue";
            String redrivePolicy = "{\"maxReceiveCount\":\"5\", \"deadLetterTargetArn\":\"" + dlqARN + "\"}";
            SetQueueAttributesRequest queueAttributes = new SetQueueAttributesRequest();
            Map<String,String> attributes = new HashMap<String,String>();
            attributes.put("RedrivePolicy", redrivePolicy);
            queueAttributes.setAttributes(attributes);
            queueAttributes.setQueueUrl(myQueueUrl);
            sqs.setQueueAttributes(queueAttributes);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

        try {
            System.out.println("===========================================");
            System.out.println("Send Queue Message with Attributes");
            System.out.println("===========================================\n");

            final String myQueueUrl = "https://sqs.us-east-2.amazonaws.com/620428855768/SQSSDKDemo";

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("attributeString", new MessageAttributeValue().withDataType("String").withStringValue("string-value-attribute-value"));
            messageAttributes.put("attributeNumber", new MessageAttributeValue().withDataType("Number").withStringValue("230.000000000000000001"));
            messageAttributes.put("attributeBinary", new MessageAttributeValue().withDataType("Binary").withBinaryValue(ByteBuffer.wrap(new byte[10])));
            messageAttributes.put("AccountIdString", new MessageAttributeValue().withDataType("String.AccountId").withStringValue("000123456"));
            messageAttributes.put("AccountIdNumber", new MessageAttributeValue().withDataType("Number.AccountId").withStringValue("000123456"));
            messageAttributes.put("PhoneIcon", new MessageAttributeValue().withDataType("Binary.JPEG").withBinaryValue(ByteBuffer.wrap(new byte[10])));
            SendMessageRequest request = new SendMessageRequest();
            request.withMessageBody("A test message body.");
            request.withQueueUrl(myQueueUrl);
            request.withMessageAttributes(messageAttributes);
            sqs.sendMessage(request);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

    }
}
