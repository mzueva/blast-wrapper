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

import com.epam.blast.security.jwt.entity.JwtAuthenticationToken;
import com.epam.blast.security.jwt.entity.JwtRawToken;
import com.epam.blast.security.jwt.entity.JwtTokenClaims;
import com.epam.blast.security.jwt.entity.UserContext;
import com.epam.blast.security.jwt.exception.TokenVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtTokenVerifier tokenVerifier;

    @Override
    public Authentication authenticate(final Authentication authentication) {
        final JwtRawToken jwtRawToken = (JwtRawToken) authentication.getCredentials();
        if (jwtRawToken == null) {
            throw new AuthenticationServiceException("Authentication error: missing token");
        }
        JwtTokenClaims claims;
        try {
            claims = tokenVerifier.readClaims(jwtRawToken.getToken());
        } catch (TokenVerificationException e) {
            throw new AuthenticationServiceException("Authentication error", e);
        }

        final UserContext context = new UserContext(jwtRawToken, claims);

        return new JwtAuthenticationToken(context, context.getAuthorities());
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
