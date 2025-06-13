package io.sendur.models;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Cognito has a custom logout url.
 * See more information <a href="https://docs.aws.amazon.com/cognito/latest/developerguide/logout-endpoint.html">here</a>.
 */
@Component
public class CognitoLogoutHandler extends SimpleUrlLogoutSuccessHandler {

    /**
     * user pool domain.
     */
    @Value("${cognito.domain}")
    private String domain;

    /**
     * User Pool Client ID
     */
    @Value("${cognito.user-pool-client-id}")
    private String userPoolClientId;

    /**
     * Redirect URI
     */
    @Value("${cognito.redirect-uri}")
    private String redirectUri;

    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String CLIENT_ID = "client_id";
    public static final String LOGOUT_URI = "logout_uri";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String CODE = "code";
    public static final String SCOPE = "scope";
    public static final String OPEN_ID = "openid";

    /**
     * Here, we must implement the new logout URL request. We define what URL to send our request to, and set out client_id and logout_uri parameters.
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // building the post logout url
        String loginRedirectUrl = UriComponentsBuilder
                .fromUriString(domain + LOGIN)
                .queryParam(CLIENT_ID, userPoolClientId)
                .queryParam(RESPONSE_TYPE, CODE)
                .queryParam(SCOPE, OPEN_ID)
                .queryParam(REDIRECT_URI, redirectUri)
                .build()
                .toUriString();

        // building the cognito logout url
        String fullLogoutUrl = UriComponentsBuilder
                .fromUriString(domain + LOGOUT)
                .queryParam(CLIENT_ID, userPoolClientId)
                .queryParam(LOGOUT_URI, loginRedirectUrl)
                .build()
                .toUriString();

        response.sendRedirect(fullLogoutUrl);
    }
}
