package com.serverless;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.model.Address;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class AddressHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger logger = LogManager.getLogger(AddressHandler.class);

    private static final String ACCOUNT_ID = "accountId";
    private DynamoDBMapper mapper;

    private final ObjectMapper om;

    public AddressHandler() {
        om = new ObjectMapper();
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTH_1)
                .build();
        mapper = new DynamoDBMapper(client);
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        logger.info("received: {}", input);
        Response responseBody = new Response("serverless function", input);
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(responseBody)
                .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                .build();
    }

    public ApiGatewayResponse getAddresses(Map<String, Object> input, Context context) {
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");

        String addressId = pathParameters.get("addressId");
        String accountId = pathParameters.get("accountId");

        logger.info("getting address for account -> {} and address -> {}", accountId, addressId);

        //get data from the dynamodb table, based on addressId and accountId
        Address partitionKey = new Address();
        DynamoDBQueryExpression<Address> queryExpression = null;

        List<Address> addressList = null;

        if(addressId == null) {
            partitionKey.setAccountId(accountId);
            queryExpression = new DynamoDBQueryExpression<Address>()
                    .withHashKeyValues(partitionKey)
                    .withIndexName("walmart-address-secondaryIndex")
                    .withConsistentRead(false);
            addressList = mapper.query(Address.class, queryExpression)
                    .stream().collect(Collectors.toList());
        } else {
            partitionKey.setAddressId(addressId);
            queryExpression = new DynamoDBQueryExpression<Address>()
                    .withHashKeyValues(partitionKey)
                    .withRangeKeyCondition(
                            ACCOUNT_ID,
                            new Condition()
                                    .withComparisonOperator(ComparisonOperator.EQ)
                                    .withAttributeValueList(
                                            new AttributeValue()
                                                    .withS(accountId)
                                    )
                    );
            addressList = mapper.query(Address.class, queryExpression)
                    .stream().collect(Collectors.toList());
        }



        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(addressList)
                .build();
    }

    public ApiGatewayResponse addAddress(Map<String, Object> input, Context context) {

        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        logger.info(input);
        String body = (String) input.get("body");
        logger.info(body);
        String accountId = pathParameters.get("accountId");
        logger.info(body);

        if (StringUtils.isBlank(accountId)) {
            return
                    ApiGatewayResponse.builder()
                            .setStatusCode(403)
                            .setRawBody("please provide accountId")
                            .build();
        }

        try {
            Address address = om.readValue(body, Address.class);
            address.setAccountId(accountId);
            mapper.save(address);
        } catch (AmazonDynamoDBException | JsonProcessingException e) {
            return
                    ApiGatewayResponse.builder()
                            .setStatusCode(403)
                            .setRawBody(e.getMessage())
                            .build();
        }

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody("address saved successfully")
                .build();

        //send email after address is created successfully via dynamodbstreams
        //track email statistics - configSet
    }

    public ApiGatewayResponse deleteAddress(Map<String, Object> input, Context context) {

        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");

        String addressId = pathParameters.get("addressId");
        String accountId = pathParameters.get("accountId");

        if ((accountId == null || StringUtils.isBlank(accountId)) && (addressId == null || StringUtils.isBlank(addressId))) {
            return
                    ApiGatewayResponse.builder()
                            .setStatusCode(403)
                            .setRawBody("please provide accountId/addressId")
                            .build();
        }

        Address address = new Address();
        address.setAddressId(addressId);
        address.setAccountId(accountId);

        try {
            mapper.delete(address);
        } catch (Exception e) {
            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody(e.getMessage())
                    .build();
        }
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody("address deleted successfully")
                .build();
    }
}
