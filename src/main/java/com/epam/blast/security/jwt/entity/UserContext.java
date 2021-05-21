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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@NoArgsConstructor
public class UserContext implements UserDetails {
    private JwtRawToken jwtRawToken;
    private Long userId;
    private String userName;
    private String orgUnitId;
    private List<String> roles = new ArrayList<>();
    private List<String> groups = new ArrayList<>();

    public UserContext(final JwtRawToken jwtRawToken, final JwtTokenClaims claims) {
        this.jwtRawToken = jwtRawToken;
        if (NumberUtils.isDigits(claims.getUserId())) {
            this.userId = Long.parseLong(claims.getUserId());
        }
        this.userName = claims.getUserName().toUpperCase(Locale.getDefault());
        this.orgUnitId = claims.getOrgUnitId();
        this.roles = new ArrayList<>(claims.getRoles());
        this.groups = new ArrayList<>(claims.getGroups());
    }

    public UserContext(final String userName) {
        this.userName = userName;
        this.orgUnitId = "";
    }

    public UserContext(final String userName, final Long id) {
        this(userName);
        this.userId = id;
    }

    public JwtTokenClaims toClaims() {
        return JwtTokenClaims.builder()
                .userId(userId.toString())
                .userName(userName)
                .orgUnitId(orgUnitId)
                .roles(roles)
                .groups(groups)
                .build();
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        final List<GrantedAuthority> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(roles)) {
            roles.forEach(role -> result.add(new SimpleGrantedAuthority(role)));
        }
        if (!CollectionUtils.isEmpty(groups)) {
            groups.forEach(group -> result.add(new SimpleGrantedAuthority(group)));
        }
        return result;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
