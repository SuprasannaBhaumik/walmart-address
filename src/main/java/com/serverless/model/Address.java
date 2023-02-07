package com.serverless.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@DynamoDBTable(tableName = "walmart-address")
public class Address {
    @DynamoDBRangeKey
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "walmart-address-secondaryIndex")
    String accountId;
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    String addressId;

    String addressLine1;
    String addressLine2;
    String addressLine3;
    String state;
    String city;
    String pincode;

}