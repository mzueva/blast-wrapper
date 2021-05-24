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

package com.epam.blast.security.jwt.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;

import javax.servlet.http.Cookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class JwtRawToken {

    private final String token;

    public static JwtRawToken fromHeader(final String authorizationHeader, final List<String> headerPrefix) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            throw new AuthenticationServiceException("Authorization header is blank");
        }
        return jwtRawToken(authorizationHeader, headerPrefix);
    }

    public static JwtRawToken fromCookie(final Cookie authCookie, final List<String> headerPrefix) {
        if (authCookie == null || StringUtils.isEmpty(authCookie.getValue())) {
            throw new AuthenticationServiceException("Authorization cookie is blank");
        }
        final String authCookieValue = URLDecoder.decode(authCookie.getValue(), StandardCharsets.UTF_8);
        return jwtRawToken(authCookieValue, headerPrefix);
    }

    private static JwtRawToken jwtRawToken(final String authValue, final List<String> headerPrefix) {
        final String prefix = getPrefix(authValue, headerPrefix);
        return new JwtRawToken(StringUtils.trim(authValue.substring(prefix.length())));
    }

    private static String getPrefix(final String authorizationHeader, final List<String> headerPrefix) {
        return ListUtils.emptyIfNull(headerPrefix)
                .stream()
                .sorted(Comparator.comparing(String::length).reversed())
                .filter(authorizationHeader::startsWith)
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }
}
