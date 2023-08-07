package com.marvic.factman.aws.service;

import com.google.gson.JsonObject;
import static com.marvic.factman.aws.util.CognitoUtil.*;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CognitoUserService {

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

    public CognitoUserService(String region) {
        this.cognitoIdentityProviderClient = CognitoIdentityProviderClient
                .builder()
                .region(Region.of(region))
                .build();
    }

    public CognitoUserService(CognitoIdentityProviderClient cognitoIdentityProviderClient) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
    }

    public JsonObject createUser(JsonObject user, String userPoolClientId, String userPoolClientSecret) {
        String email = user.get("email").getAsString();
        String password = user.get("password").getAsString();
        String name = user.get("name").getAsString();
        String nickname = user.get("nickname").getAsString();
        String userId = UUID.randomUUID().toString();
        String license = "DEMO" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        AttributeType emailAtt = buildAttribute("email", email);
        AttributeType nameAtt = buildAttribute("name", name);
        AttributeType nicknameAtt = buildAttribute("nickname", nickname);
        AttributeType userIdAtt = buildAttribute("custom:user_id", userId);
        AttributeType licenseAtt = buildAttribute("custom:license", license);

        List<AttributeType> attributeList = Arrays.asList(emailAtt, nameAtt, nicknameAtt, userIdAtt, licenseAtt);

        String secretHash = calculateSecretHash(userPoolClientId, userPoolClientSecret, email);
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username(email)
                .password(password)
                .userAttributes(attributeList)
                .clientId(userPoolClientId)
                .secretHash(secretHash)
                .build();

        SignUpResponse signUpResponse = cognitoIdentityProviderClient.signUp(signUpRequest);

        JsonObject createdUserResult = new JsonObject();
        createdUserResult.addProperty("isSuccessful", signUpResponse.sdkHttpResponse().isSuccessful());
        createdUserResult.addProperty("statusCode", signUpResponse.sdkHttpResponse().statusCode());
        createdUserResult.addProperty("cognitoUserId", signUpResponse.userSub());
        createdUserResult.addProperty("isConfirmed", signUpResponse.userConfirmed());
        return createdUserResult;
    }

}
