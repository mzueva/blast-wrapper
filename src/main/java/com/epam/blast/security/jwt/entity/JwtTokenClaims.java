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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class JwtTokenClaims {
    public static final String CLAIM_USER_ID = "user_id";
    public static final String CLAIM_ORG_UNIT_ID = "org_unit_id";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_GROUPS = "groups";
    public static final String SECURITY_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    @JsonProperty("jti")
    private String jwtTokenId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("username")
    private String userName;
    @JsonProperty("org_unit_id")
    private String orgUnitId;
    @JsonProperty("issued_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SECURITY_DATE_TIME_FORMAT)
    private LocalDateTime issuedAt;
    @JsonProperty("expires_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SECURITY_DATE_TIME_FORMAT)
    private LocalDateTime expiresAt;
    private List<String> roles;
    private List<String> groups;
}
