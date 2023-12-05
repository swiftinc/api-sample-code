# Swift API Sample Code

[![Spring Boot](https://github.com/swiftinc/api-sample-code/actions/workflows/spring_boot.yaml/badge.svg)](https://github.com/swiftinc/api-sample-code/actions/workflows/spring_boot.yaml)
[![Quarkus](https://github.com/swiftinc/api-sample-code/actions/workflows/quarkus.yaml/badge.svg)](https://github.com/swiftinc/api-sample-code/actions/workflows/quarkus.yaml)
[![dotnet](https://github.com/swiftinc/api-sample-code/actions/workflows/dotnet.yaml/badge.svg)](https://github.com/swiftinc/api-sample-code/actions/workflows/dotnet.yaml)
[![Node.js](https://github.com/swiftinc/api-sample-code/actions/workflows/nodejs.yaml/badge.svg)](https://github.com/swiftinc/api-sample-code/actions/workflows/nodejs.yaml)
[![python](https://github.com/swiftinc/api-sample-code/actions/workflows/python.yaml/badge.svg)](https://github.com/swiftinc/api-sample-code/actions/workflows/python.yaml)

## Quickstart

### Create an application

1. [Create an application](https://developer.swift.com/myapps) and select the API products you want to use.
   More than one API product can be assigned to an application and during pilot period the application can be modified to include more products.

2. Copy the **consumer key** and **consumer secret** values into `CONSUMER_KEY` and `CONSUMER_SECRET` environment variables.

   On Linux and macOS:

   ```shell script
   export CONSUMER_KEY=<consumer key value from dev portal>
   export CONSUMER_SECRET=<consumer secret value from dev portal>
   ```

   On Windows:

   ```PowerShell script
   # PowerShell
   $env:CONSUMER_KEY=<consumer key value from dev portal>
   $env:CONSUMER_SECRET=<consumer secret value from dev portal>

   # Command prompt
   set CONSUMER_KEY=<consumer key value from dev portal>
   set CONSUMER_SECRET=<consumer secret value from dev portal>
   ```

### Start the application

This monorepo contains multiple sample code projects organized in subfolders. 
The folder structure is as follows: `language -> framework(optional) -> project`

To start the application:

1. Navigate to the project subfolder, for example:

   ```shell script
     # cd language/framework/<project> 
     cd java/spring-boot/messaging-api
   ```
   or
   ```shell script
     # cd language/<project> 
     cd nodejs/messaging-api
   ```

2. Launch the application
   | Language | Command |
   | --- | --- |
   | Spring Boot | `./mvnw spring-boot:run` |
   | Quarkus | `./mvnw quarkus:dev` |
   | .NET | `dotnet run` |
   | NodeJS | `npm install && npm run dev` |
   | Python | `python -m flask run --port 8080` |

## Testing

REST API endpoints are exposed to quickly test the code and [Swagger UI](https://swagger.io/tools/swagger-ui/) can be used to easily execute the tests.

The Swagger UI page will be available at http://127.0.0.1:8080/test

## Troubleshooting

### Client application cannot be authenticated

When an `invalid_client` error is returned by the API gateway the credentials used in Basic HTTP Authentication scheme are wrong. Make sure that credentials form [your app](https://developer.swift.com/myapps) are used in the authorization header during the token retrieval.

```JSON
{
    "error": "invalid_client",
    "errorDescription": "Client application cannot be authenticated."
}
```

### Invalid scope

Each API has its own scope (swift.preval, swift.messaging.api, etc.), this error is thrown when the OAuth client is configured wioth the wrong scope.
Make sure that the correct scope is used.

```JSON
{
    "error": "invalid_scope",
    "error_description": "Access to requested scope cannot be granted."
}
```
