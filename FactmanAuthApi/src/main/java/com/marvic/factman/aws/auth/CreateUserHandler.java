package com.marvic.factman.aws.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marvic.factman.aws.service.CognitoUserService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

/**
 * Handler for requests to Lambda function.
 */
public class CreateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson = new Gson();
    private final CognitoUserService cognitoUserService;
    private final String appClientId;
    private final String appClientSecret;

    public CreateUserHandler() {
        this.cognitoUserService = new CognitoUserService(System.getenv("AWS_REGION"));
        this.appClientId = System.getenv("AWS_COGNITO_USER_POOL_APP_CLIENT_ID");
        this.appClientSecret = System.getenv("AWS_COGNITO_USER_POOL_APP_CLIENT_SECRET");
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        LambdaLogger logger = context.getLogger();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);

        // Input
        String requestBody = input.getBody();
        JsonObject userDetails = JsonParser.parseString(requestBody).getAsJsonObject();
        // logger.log(String.format("\n\nCreating user: [%s] \n\n", userDetails.get("email")));

        // Action
        try {
            JsonObject createdUser = cognitoUserService.createUser(userDetails, appClientId, appClientSecret);

            response.withStatusCode(200);
            response.withBody(gson.toJson(createdUser, JsonObject.class));

        } catch (AwsServiceException e) {
            String errorMessage = e.awsErrorDetails().errorCode() + " - " + e.awsErrorDetails().serviceName() + ":" + e.awsErrorDetails().errorMessage();
            logger.log(errorMessage);
            response.withStatusCode(500);
            response.withBody(errorMessage);
        }

        return response;

//
//        logger.log("Handling HTTP Post: " + context.getFunctionName() + "= " + input.getRequestContext().getPath());
//        return response;

//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//        headers.put("X-Custom-Header", "application/json");
//
//        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
//                .withHeaders(headers);
//        try {
//            final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
//            String output = String.format("{ \"message\": \"hello world\", \"location\": \"%s\" }", pageContents);
//
//            return response
//                    .withStatusCode(200)
//                    .withBody(output);
//        } catch (IOException e) {
//            return response
//                    .withBody("{}")
//                    .withStatusCode(500);
//        }
    }

//    private String getPageContents(String address) throws IOException{
//        URL url = new URL(address);
//        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
//            return br.lines().collect(Collectors.joining(System.lineSeparator()));
//        }
//    }
}
