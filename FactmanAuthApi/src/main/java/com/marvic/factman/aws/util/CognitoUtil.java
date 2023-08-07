package com.marvic.factman.aws.util;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CognitoUtil {

    public static AttributeType buildAttribute(String name, String value) {
        return AttributeType.builder().name(name).value(value).build();
    }

    /**
     * Computing secret hash values
     * https://docs.aws.amazon.com/cognito/latest/developerguide/signing-up-users-in-your-app.html
     *
     * @param userPoolClientId
     * @param userPoolClientSecret
     * @param userName
     * @return
     */
    public static String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating");
        }
    }

}
