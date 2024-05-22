package com.deepak.app.gateway.jwt;


import com.deepak.app.gateway.exception.JwtMalformedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Configuration
public class JwtUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.token.jwt.public-key}")
    private String rsaPublicKey;

    public   Claims validateToken(String requestHeaders){
        // first we fetch the token and validate it
        String token = getToken(requestHeaders);
        PublicKey publicKey = null;
        try {
            publicKey = getPublicKey();
            Jws<Claims> jwt = Jwts.parser()
                                  .verifyWith(publicKey)
                                 .build()
                                .parseSignedClaims(token);
            LOGGER.info("Token Generated Successfully", token);
            return jwt.getPayload();
        }
        catch (NoSuchAlgorithmException e) {
            throw new JwtMalformedException("Signed Token algorithm is not configured");
        }
        catch (InvalidKeySpecException e) {
            LOGGER.error("Invalid JWT token");
            throw new JwtMalformedException("Public key is not valid or not found");
        }
        catch (ExpiredJwtException e) {
            LOGGER.error("JWT Token is expired");
            throw new JwtMalformedException("Token is expired");
        } catch (MalformedJwtException e) {
            throw new JwtMalformedException("Malformed token");
        } catch (SignatureException e) {
            throw new JwtMalformedException("Invalid token signature key");
        }
    }

    private  PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {

        rsaPublicKey = rsaPublicKey.replace("-----BEGIN PUBLIC KEY-----", "");
        rsaPublicKey = rsaPublicKey.replace("-----END PUBLIC KEY-----", "");
        rsaPublicKey = rsaPublicKey.trim().replaceAll(" ", "");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPublicKey));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(keySpec);
        return publicKey;
    }

    private  String getToken(String requestHeaders) {
        String[] headers = requestHeaders.split(" ");
        if (headers.length != 2 || !"Bearer".equalsIgnoreCase(headers[0])) {
            throw new RuntimeException();
        }
        return headers[1];
    }

}
