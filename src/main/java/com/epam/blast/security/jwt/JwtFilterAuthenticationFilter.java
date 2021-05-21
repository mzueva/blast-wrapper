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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class JwtFilterAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenVerifier tokenVerifier;
    private final List<String> bearerPrefixes;
    private final List<String> authHeaderNames;
    private final List<String> authCookieNames;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = extractAuthHeader(request);
        try {
            final JwtRawToken rawToken = retrieveToken(request, authorizationHeader);
            final JwtTokenClaims claims = tokenVerifier.readClaims(rawToken.getToken());
            final UserContext context = new UserContext(rawToken, claims);
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(context, context.getAuthorities()));
            log.info("Attempting to authenticate user with name: " + context.getUsername());
        } catch (AuthenticationServiceException | TokenVerificationException e) {
            logger.trace(e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }

    private String extractAuthHeader(final HttpServletRequest request) {
        return ListUtils.emptyIfNull(authHeaderNames)
                .stream()
                .map(request::getHeader)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private Cookie extractAuthCookie(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ListUtils.emptyIfNull(authCookieNames)
                        .stream()
                        .anyMatch(name -> cookie.getName().equals(name))) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private JwtRawToken retrieveToken(final HttpServletRequest request, final String authorizationHeader) {
        final JwtRawToken rawToken;
        if (!StringUtils.isEmpty(authorizationHeader)) { // attempt obtain JWT token from HTTP header
            rawToken = JwtRawToken.fromHeader(authorizationHeader, bearerPrefixes);
            log.debug("Extracted JWT token from authorization HTTP header");
        } else {                                           // else try to get token from cookies
            final Cookie authCookie = extractAuthCookie(request);
            rawToken = JwtRawToken.fromCookie(authCookie, bearerPrefixes);
            log.debug("Extracted JWT token from authorization cookie");
        }
        return rawToken;
    }

}
