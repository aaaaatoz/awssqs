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
// http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-create-queue-sse.html
// review result:
// 1. replacing the depreceted methods
// 2. createRequest.setAttributes(attributes); is missing, adding back
// 3. change the 864000 to 86400 as 864000 is a typo value



import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.HashMap;
import java.util.Map;

public class SQSSSEQueueCreationSample {
    public static void main(String[] args) throws Exception {

        AmazonSQS sqs = AmazonSQSClientBuilder
                .standard()
                //.withCredentials(new EnvironmentVariableCredentialsProvider())
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(Regions.US_EAST_2)
                .build();
        final String AWSAccount = "620428855768";
        final String region = Regions.US_EAST_2.toString().toLowerCase();

        System.out.println("=======================================================");
        System.out.println("Create Standard Queue with AWS-managed CMK");
        System.out.println("=======================================================\n");

        try {
            CreateQueueRequest createRequestCMK1 = new CreateQueueRequest("MyQueueAWSCMK");
            Map<String, String> attributes = new HashMap<String, String>();

            // Enable server-side encryption by specifying the alias ARN of the
            // AWS-managed CMK for Amazon SQS
            String kmsMasterKeyAlias = "arn:aws:kms:" + region + ":" + AWSAccount + ":alias/aws/sqs";
            attributes.put("KmsMasterKeyId", kmsMasterKeyAlias);
            createRequestCMK1.setAttributes(attributes);

            // (Optional) Specify the length of time, in seconds, for which Amazon SQS can reuse
            attributes.put("KmsDataKeyReusePeriodSeconds", "60");

            CreateQueueResult createResult = sqs.createQueue(createRequestCMK1);
            System.out.println("Created the queue successfully.\n");
            // Delete the queue
            System.out.println("Deleting the queue.\n");
            sqs.deleteQueue(new DeleteQueueRequest(createResult.getQueueUrl()));

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

        System.out.println("=======================================================");
        System.out.println("Create Standard Queue with custom CMK");
        System.out.println("=======================================================\n");

        try {

            CreateQueueRequest createRequestCustomCMK = new CreateQueueRequest("MyQueueCustomCMK");
            Map<String, String> attributes = new HashMap<String, String>();

            // Enable server-side encryption by specifying the alias ARN of the
            // AWS-managed CMK for Amazon SQS.
            String kmsMasterKeyAlias = "arn:aws:kms:" + region + ":" + AWSAccount + ":alias/rafasqs";
            attributes.put("KmsMasterKeyId", kmsMasterKeyAlias);
            createRequestCustomCMK.setAttributes(attributes);

            // (Optional) Specify the length of time, in seconds, for which Amazon SQS can reuse
            attributes.put("KmsDataKeyReusePeriodSeconds", "86400");

            CreateQueueResult createResult = sqs.createQueue(createRequestCustomCMK);
            System.out.println("Created the queue successfully.\n");
            // Delete the queue
            System.out.println("Deleting the queue.\n");
            //sqs.deleteQueue(new DeleteQueueRequest(createResult.getQueueUrl()));

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
