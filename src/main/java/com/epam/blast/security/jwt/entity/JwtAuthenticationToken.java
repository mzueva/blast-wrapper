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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private JwtRawToken jwtRawToken;
    private UserContext userContext;
    private Date tokenExpiration;
    private static final int TOKEN_SESSION_TIMEOUT = 60;

    public JwtAuthenticationToken(final JwtRawToken jwtRawToken) {
        super(null);
        this.jwtRawToken = jwtRawToken;
        this.setAuthenticated(false);
        this.tokenExpiration = toDate(now().plusSeconds(TOKEN_SESSION_TIMEOUT));
    }

    public JwtAuthenticationToken(final UserContext userContext,
                                  final Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        super.setAuthenticated(true);
        this.userContext = userContext;
        this.tokenExpiration = toDate(now().plusSeconds(TOKEN_SESSION_TIMEOUT));
    }

    @Override
    public void setAuthenticated(final boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    @Override
    public boolean isAuthenticated() {
        if (tokenExpiration != null && new Date().compareTo(tokenExpiration) >= 0) {
            return false;
        } else {
            return super.isAuthenticated();
        }
    }

    @Override
    public Object getCredentials() {
        return this.jwtRawToken;
    }

    @Override
    public Object getPrincipal() {
        return this.userContext;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.jwtRawToken = null;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(Clock.systemUTC());
    }

    private Date toDate(final LocalDateTime time) {
        return Date.from(time.toInstant(ZoneOffset.UTC));
    }
}
