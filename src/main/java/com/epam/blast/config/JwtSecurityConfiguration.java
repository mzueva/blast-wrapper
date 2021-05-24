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

package com.epam.blast.config;

import com.epam.blast.security.jwt.JwtAuthenticationProvider;
import com.epam.blast.security.jwt.JwtFilterAuthenticationFilter;
import com.epam.blast.security.jwt.JwtTokenVerifier;
import com.epam.blast.security.jwt.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(1)
@ConditionalOnProperty(value = "security.jwt.auth.enabled", matchIfMissing = false, havingValue = "true")
public class JwtSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.jwt.public.key}")
    private String publicKey;

    @Value("${security.jwt.auth.bearer.prefixes}")
    private List<String> bearerPrefixes;

    @Value("${security.jwt.auth.header.names}")
    private List<String> authHeaderNames;

    @Value("${security.jwt.auth.cookie.names}")
    private List<String> authCookieNames;

    @Bean
    public JwtTokenVerifier jwtTokenVerifier() {
        return new JwtTokenVerifier(publicKey);
    }

    @Bean
    protected JwtAuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(jwtTokenVerifier());
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(jwtAuthenticationProvider());
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .requestMatcher(getFullRequestMatcher())
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(getUnsecuredResources()).permitAll()
                .antMatchers(getSecuredResources()).authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .addFilterBefore(getJwtAuthenticationFilter(bearerPrefixes, authHeaderNames, authCookieNames),
                        UsernamePasswordAuthenticationFilter.class);
    }

    protected JwtFilterAuthenticationFilter getJwtAuthenticationFilter(
            final List<String> bearerPrefixes,
            final List<String> authHeaderNames,
            final List<String> authCookieNames) {
        return new JwtFilterAuthenticationFilter(jwtTokenVerifier(),
                bearerPrefixes, authHeaderNames, authCookieNames);
    }

    protected RequestMatcher getFullRequestMatcher() {
        return new AntPathRequestMatcher(getSecuredResources());
    }

    protected String getSecuredResources() {
        return "/**";
    }

    protected String[] getUnsecuredResources() {
        return new String[] {
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/springfox-swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
        };
    }
}
