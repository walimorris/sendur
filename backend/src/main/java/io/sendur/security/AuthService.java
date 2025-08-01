package io.sendur.security;

import io.sendur.configuration.N8NConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    @Value("${spring.security.oauth2.client.provider.cognito.issuer-uri}")
    private String issuer;

    private final N8NConfigurationProperties n8NConfigProps;

    private static final String CLIENT_ID = "client_id";
    private static final String ISS = "iss";

    private static final String FORBIDDEN = "FORBIDDEN";

    public AuthService(final N8NConfigurationProperties n8NConfigProps) {
        this.n8NConfigProps = n8NConfigProps;
    }

    /**
     * Explicit Authorization comes after validating the state of the requester, and it's valid
     * credentials. Without explicit authentication all access is denied.
     *
     * @param authentication {@link Authentication}
     *
     * @return boolean
     */
    public boolean isExplicitlyAuthorized(Authentication authentication) {
        return isValidJwtAuthenticatedMachine(authentication) || isValidOauth2Authentication(authentication);
    }

    /**
     * Validates authorized read scope for M2M (Machine to Machine) communication with n8n.
     *
     * @param clientId machine's clientId claim
     *
     * @return boolean
     */
    public boolean isAuthorizedReaderMachine(String clientId) {
        return StringUtils.isNotBlank(clientId) && clientId.equals(n8NConfigProps.getReaderClient());
    }

    /**
     * A basic UNAUTHORIZED response utility that can be reused in controller classes.
     *
     * @return {@link ResponseEntity}
     */
    public ResponseEntity<?> forbiddenResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .body(FORBIDDEN);
    }

    public boolean isNotAuthorized(Authentication authentication) {
        return !isExplicitlyAuthorized(authentication);
    }

    /**
     * {@code Sendur} auth is handled by Cognito and utilizes {@linkplain OAuth2AuthenticationToken OAuth2 Tokens}.
     * This method keeps Zero Trust best practices by validating the authenticating state. Here, we validate granted
     * authorities and issuer as a first step to validating correct OAuth2 authentication state.
     *
     * @param authentication {@link Authentication}
     *
     * @return boolean
     */
    private boolean isValidOauth2Authentication(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oidcToken) {
            List<String> grantedAuthorities = oidcToken.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            Map<String, Object> userAttributes = oidcToken.getPrincipal().getAttributes();
            String iss = String.valueOf(userAttributes.getOrDefault(ISS, null));
            return StringUtils.isNotBlank(iss) && iss.equals(issuer) && grantedAuthorities.contains("OIDC_USER");
        }
        return false;
    }

    /**
     * {@code Sendur} utilizing resource servers on Cognito to issue M2M (Machine to Machine)
     * {@linkplain JwtAuthenticationToken Jwt Tokens}. Here, the token's {@code Client ID}
     * claim and authorization scopes are validated for the requesting machine.
     *
     * @param authentication {@link Authentication}
     *
     * @return boolean
     */
    private boolean isValidJwtAuthenticatedMachine(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String claim = jwtAuthenticationToken.getToken().getClaimAsString(CLIENT_ID);
            return isAuthorizedReaderMachine(claim) || isAuthorizedReaderWriterMachine(claim);
        }
        return false;
    }

    /**
     * Validates authorized read/write scope for M2M (Machine to Machine) communication with n8n.
     *
     * @param clientId machine's clientId claim
     *
     * @return boolean
     */
    private boolean isAuthorizedReaderWriterMachine(String clientId) {
        return StringUtils.isNotBlank(clientId) && clientId.equals(n8NConfigProps.getReaderWriterClient());
    }
}
