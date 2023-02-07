Service to manage the address of customers @ Walmart


Serverless Java based Lambda application



```To deploy
- install serverless
    npm i -g serverless

- Setup aws programatic access via 
    - export AWS_ACCESS_KEY_ID=<key>
    - export AWS_SECRET_ACCESS_KEY=<secret>

- Running command
    - sls deploy --stage dev --region ap-south-1

```
To deploy the serverless, sometimes change this readme file
Make sure to run "mvn clean install -DskipTests" before each deployment


```Project learnings```
- **Global Secondary Index**
  - While defining GSI, Hash/Range keys have to be defined in KeySchema 
  - They have to be followed by  specific provisioned throughput parameters
  - Annotations in Java POJO need to mention both the original hash and range key
    - Also have to define the hash/range key for the index
- **DynamoDB Streams**
  - Stream view type must document the nature of streams to propagate the data
    - e.g. NEW_AND_OLD_IMAGES means both new data and updates are send
- **CRUD**
  - GET
    - search is based on partition key(HASH)
    - to search with other than partition key, global or secondary index must be defined
  - DELETE
    - to delete both range and hash key have to be defined to the mapper.delete(obj)

