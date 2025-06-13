package io.sendur.models;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Cognito has a custom logout url.
 * See more information <a href="https://docs.aws.amazon.com/cognito/latest/developerguide/logout-endpoint.html">here</a>.
 */
public class CognitoLogoutHandler extends SimpleUrlLogoutSuccessHandler {

    /**
     * user pool domain.
     */
    @Value("${cognito.domain}")
    private String domain;

    /**
     * An allowed callback URL.
     */
    @Value("${cognito.logout-url}")
    private String logoutRedirectUrl;

    /**
     * User Pool Client ID
     */
    @Value("${cognito.user-pool-client-id}")
    private String userPoolClientId;

    public static final String LOGOUT = "/logout";
    public static final String CLIENT_ID = "client_id";
    public static final String LOGOUT_URI = "logout_uri";

    /**
     * Here, we must implement the new logout URL request. We define what URL to send our request to, and set out client_id and logout_uri parameters.
     */
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        return UriComponentsBuilder
                .fromUri(URI.create(domain + LOGOUT))
                .queryParam(CLIENT_ID, userPoolClientId)
                .queryParam(LOGOUT_URI, logoutRedirectUrl)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }
}
