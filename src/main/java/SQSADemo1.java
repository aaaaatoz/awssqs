/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

// This is the update code for :
// http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/standard-queues.html#standard-queues-getting-started-java

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.*;
import java.util.Map.Entry;

import static java.lang.Thread.sleep;

/**
 * This sample demonstrates how to make basic requests to Amazon SQS using the
 * AWS SDK for Java.
 *
 * Prerequisites: You must have a valid Amazon Web Services developer account,
 * and be signed up to use Amazon SQS. For more information about Amazon SQS,
 * see http://aws.amazon.com/sqs
 *
 * Fill in your AWS access credentials in the provided credentials file
 * template, and be sure to move the file to the default location
 * (~/.aws/credentials) where the sample code loads the credentials from.
 *
 * IMPORTANT: To avoid accidental leakage of your credentials, DO NOT
 * keep the credentials file in your source directory.
 */



public class SQSADemo1 {

    public static void main(String[] args) throws Exception {

        AmazonSQS sqs = AmazonSQSClientBuilder
                .standard()
                //.withCredentials(new EnvironmentVariableCredentialsProvider())
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(Regions.US_EAST_2)
                .build();


        try {
            // Create queues
            System.out.println("Creating new Queues.\n");
            CreateQueueRequest createQueueRequest = new CreateQueueRequest("QueueOne");
            String queue1Url = sqs.createQueue(createQueueRequest).getQueueUrl();

            createQueueRequest = new CreateQueueRequest("QueueTwo");
            String queue2Url = sqs.createQueue(createQueueRequest).getQueueUrl();

            createQueueRequest = new CreateQueueRequest("fifoQueue.fifo");
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("FifoQueue", "true");
            attributes.put("ContentBasedDeduplication", "true");
            createQueueRequest.withAttributes(attributes);
            String fifoQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

            utils.Prompt("Create Queues finished. Next Step - list Queues. ");

            System.out.println("Listing all queues in your account/region.\n");
            for (String queueUrl : sqs.listQueues().getQueueUrls()) {
                System.out.println("  QueueUrl: " + queueUrl);
            }
            System.out.println();

            utils.Prompt("list Queues finished. Next Step - get Queue all attributes. ");

            // can build GetQueueAttributesRequest or QueueURL + Attribute list
            List<String> queueAttributes = new ArrayList<String>();
            queueAttributes.add("All");
            GetQueueAttributesResult getQueueAttributesResult = sqs.getQueueAttributes(queue1Url, queueAttributes);
            Map<String, String> attributesResults = getQueueAttributesResult.getAttributes();
            for (Map.Entry<String, String> entry : attributesResults.entrySet()) {
                System.out.println(queue1Url + " Attribute " + entry.getKey() + " is: " + entry.getValue());
            }

            utils.Prompt("Get Queues Attributes finished. Next Step - Change Queue Attributes - max msg size => 1k. ");

            // we will change the MaximumMessageSize to 1k
            Map<String, String> maximumMessageSizeAttributes = new HashMap<String, String>();
            maximumMessageSizeAttributes.put("MaximumMessageSize", "1024");
            sqs.setQueueAttributes(queue1Url, maximumMessageSizeAttributes);

            queueAttributes.clear();
            queueAttributes.add("MaximumMessageSize");
            getQueueAttributesResult = sqs.getQueueAttributes(queue1Url, queueAttributes);
            attributesResults = getQueueAttributesResult.getAttributes();
            for (Map.Entry<String, String> entry : attributesResults.entrySet()) {
                System.out.println(queue1Url + " Attribute " + entry.getKey() + " is: " + entry.getValue());
            }

            //Test sending a big message then failed
            char[] chars1 = new char[1023];
            char[] chars2 = new char[1025];
            Arrays.fill(chars1, 'a');
            Arrays.fill(chars2, 'a');
            String smallMSG = new String(chars1);
            String bigMSG = new String(chars2);

            sqs.sendMessage(queue1Url, "testMSG");
            try {
                sqs.sendMessage(queue1Url, bigMSG);
            } catch (AmazonServiceException e) {
                System.out.println("You should be able to see an error saying the message is too big");
                System.out.println(e.toString());
            }

            SendMessageBatchRequestEntry entry1 = new SendMessageBatchRequestEntry();
            entry1.setId("1");
            entry1.setMessageBody(smallMSG);

            SendMessageBatchRequestEntry entry2 = new SendMessageBatchRequestEntry();
            entry2.setId("2");
            entry2.setMessageBody(bigMSG);

            SendMessageBatchRequest batchRequests = new SendMessageBatchRequest()
                    .withQueueUrl(queue1Url)
                    .withEntries(entry1)
                    .withEntries(entry2);

            SendMessageBatchResult batchResult = sqs.sendMessageBatch(batchRequests);
            System.out.println("\nUsually the batch sending result HTTP code is always: " + batchResult.getSdkHttpMetadata().getHttpStatusCode());
            System.out.println("But usually you should check successful and failed messages individually: " );

            if (batchResult.getSuccessful().size() != 0) {
                System.out.println("successful messages: " + batchResult.getSuccessful().toString());
            }

            if (batchResult.getFailed().size() != 0) {
                System.out.println("failed messages: " + batchResult.getFailed().toString());
            }

            utils.Prompt("Get Queues Size finished. Next Step - Change Queue DLQ. ");
            Map<String, String> dlqAttributes = new HashMap<String, String>();
            String dlq = "{\"deadLetterTargetArn\":\"arn:aws:sqs:us-east-2:620428855768:QueueTwo\",\"maxReceiveCount\":2}";
            dlqAttributes.put("RedrivePolicy", dlq);
            sqs.setQueueAttributes(queue1Url, dlqAttributes);

            queueAttributes.clear();
            queueAttributes.add("RedrivePolicy");
            getQueueAttributesResult = sqs.getQueueAttributes(queue1Url, queueAttributes);
            System.out.println("Queue Redrive policy: " + getQueueAttributesResult.getAttributes().get("RedrivePolicy"));


            utils.Prompt("Change Queue DLQ finished. Next Step - set Queue permission. ");
            Map<String, String> policyAttributes = new HashMap<String, String>();
            String policy = "{\"Version\":\"2012-10-17\",\"Id\":\"arn:aws:sqs:us-east-2:620428855768:QueueOne/SQSDefaultPolicy\",\"Statement\":[{\"Sid\":\"Sid1501407097849\",\"Effect\":\"Deny\",\"Principal\":\"*\",\"Action\":\"SQS:SendMessage\",\"Resource\":\"arn:aws:sqs:us-east-2:620428855768:QueueOne\"}]}";
            policyAttributes.put("Policy", policy);
            sqs.setQueueAttributes(queue1Url, policyAttributes);

            System.out.println("set permission successfully....");
            queueAttributes.clear();
            queueAttributes.add("Policy");
            getQueueAttributesResult = sqs.getQueueAttributes(queue1Url, queueAttributes);
            System.out.println("Queue Permission: " + getQueueAttributesResult.getAttributes().get("Policy"));

            System.out.println("testing sending messages");

            try {
                // Test with a redrive policy
                SendMessageResult messageResult = sqs.sendMessage(queue1Url, smallMSG);
                System.out.println(messageResult.toString());

            } catch (AmazonServiceException ase) {
                System.out.println("send message failed...");
                System.out.println(ase.toString());
            }

            System.out.println("remove policy and testing sending message");
            policyAttributes.clear();
            policyAttributes.put("Policy", "");
            sqs.setQueueAttributes(queue1Url, policyAttributes);
            SendMessageResult messageResult = sqs.sendMessage(queue1Url, smallMSG);
            System.out.println("sent message successfully");

            utils.Prompt("Policy testing successfully, Next Step - Purge Queue. ");
            queueAttributes.clear();
            queueAttributes.add("ApproximateNumberOfMessages");
            getQueueAttributesResult = sqs.getQueueAttributes(queue1Url, queueAttributes);
            System.out.println("Before purge the queue, there are about "+ getQueueAttributesResult.getAttributes().get("ApproximateNumberOfMessages") + " messages" );

            PurgeQueueRequest purgeQueueRequest = new PurgeQueueRequest();
            purgeQueueRequest.withQueueUrl(queue1Url);
            sqs.purgeQueue(purgeQueueRequest);
            System.out.println("waiting for 1 min to check...");
            sleep(60000);
            getQueueAttributesResult = sqs.getQueueAttributes(queue1Url, queueAttributes);
            System.out.println("After purge the queue, there are about "+ getQueueAttributesResult.getAttributes().get("ApproximateNumberOfMessages") + " messages" );

            utils.Prompt("Purge Queue finished. Next Step - Sending 100 Messages. ");

            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.withQueueUrl(queue1Url);

            Map messageAttributes = new HashMap();

            for(int count = 1; count <= 100; count++) {
                System.out.println("sending message - " + String.valueOf(count));
                sendMessageRequest.withMessageBody("TestMessage - " + String.valueOf(count) );
                messageAttributes.put("ID",
                        new MessageAttributeValue()
                                .withDataType("Number")
                                .withStringValue(String.valueOf(count)));
                sendMessageRequest.withMessageAttributes(messageAttributes);
                sqs.sendMessage(sendMessageRequest);
                sleep(10);
            }

            utils.Prompt("Sending Message finished. Next Step - Receive Messages ");

            ReceiveMessageResult  receiveMessageResult = null;
            int value = 0;

            System.out.println("now I begin to receive message");

            while (true) {
                //short polling and record empty receives
                receiveMessageResult = sqs.receiveMessage(queue1Url);
                List<Message> msg = receiveMessageResult.getMessages();
                if (msg.size() == 0) {
                    System.out.println("empty receives at: " + value);
                } else {
                    value ++;
                    System.out.println("now I am receiving Message " + msg.get(0).getBody());
                    if (value >= 99) break;
                }
            }

            utils.Prompt("Receiving Message finished.. Next Step - Delete Queue. ");

            sqs.deleteQueue(queue1Url);
            sqs.deleteQueue(queue2Url);
            sqs.deleteQueue(fifoQueueUrl);

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