package com.chatapplication.chatbusinessservice.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Component
public class DynamoDbInitializer {

    private final DynamoDbClient client;
    public static final String TABLE_NAME = "WhatsAppSingleTable";

    public DynamoDbInitializer(DynamoDbClient client) {
        this.client = client;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createTableOnStartup() {
        try {
            // Check if table exists
            client.describeTable(DescribeTableRequest.builder().tableName(TABLE_NAME).build());
            System.out.println("Table '" + TABLE_NAME + "' initialized and verified.");
        } catch (ResourceNotFoundException e) {
            // Table doesn't exist -> Provision it via single-table composite schema parameters
            System.out.println("Provisioning Local Single-Table Infrastructure space...");
            
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder().attributeName("PK").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("SK").keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("PK").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("SK").attributeType(ScalarAttributeType.S).build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            client.createTable(request);
            System.out.println("Single-Table creation completed.");
        }
    }
}