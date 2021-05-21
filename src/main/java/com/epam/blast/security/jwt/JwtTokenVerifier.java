/*
 *   MIT License
 *
 *   Copyright (c) 2021 EPAM Systems
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */

package com.epam.blast.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.epam.blast.security.jwt.entity.JwtTokenClaims;
import com.epam.blast.security.jwt.exception.JwtInitializationException;
import com.epam.blast.security.jwt.exception.TokenVerificationException;
import org.apache.commons.lang3.StringUtils;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;

import static com.epam.blast.security.jwt.entity.JwtTokenClaims.*;


public class JwtTokenVerifier {

    private final RSAPublicKey publicKey;

    public JwtTokenVerifier(final String publicKey) {
        try {
            this.publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new JwtInitializationException(e);
        }
    }

    public JwtTokenClaims readClaims(final String jwtToken) {
        DecodedJWT decodedToken;

        try {
            decodedToken = JWT.require(Algorithm.RSA512(publicKey, null))
                    .build()
                    .verify(jwtToken);

        } catch (JWTVerificationException jve) {
            throw new TokenVerificationException(jve);
        }

        final JwtTokenClaims tokenClaims = JwtTokenClaims.builder()
                .jwtTokenId(decodedToken.getId())
                .userName(decodedToken.getSubject())
                .userId(decodedToken.getClaim(CLAIM_USER_ID).asString())
                .orgUnitId(decodedToken.getClaim(CLAIM_ORG_UNIT_ID).asString())
                .roles(Arrays.asList(decodedToken.getClaim(CLAIM_ROLES).asArray(String.class)))
                .groups(Arrays.asList(decodedToken.getClaim(CLAIM_GROUPS).asArray(String.class)))
                .issuedAt(decodedToken.getIssuedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .expiresAt(decodedToken.getExpiresAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();
        return validateClaims(tokenClaims);
    }

    private JwtTokenClaims validateClaims(final JwtTokenClaims tokenClaims) {
        if (StringUtils.isEmpty(tokenClaims.getJwtTokenId())) {
            throw new TokenVerificationException("Invalid token: token ID is empty");
        }

        if (StringUtils.isEmpty(tokenClaims.getUserId())) {
            throw new TokenVerificationException("Invalid token: user ID is empty");
        }

        if (StringUtils.isEmpty(tokenClaims.getUserName())) {
            throw new TokenVerificationException("Invalid token: user name is empty");
        }
        return tokenClaims;
    }

}
