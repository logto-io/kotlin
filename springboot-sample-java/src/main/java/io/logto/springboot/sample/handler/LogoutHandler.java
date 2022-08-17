package io.logto.springboot.sample.handler;

import java.io.IOException;
import java.util.Optional;

import io.logto.springboot.sample.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LogoutHandler extends SecurityContextLogoutHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final String postLogoutRedirectUri;

    @Autowired
    public LogoutHandler(
            ClientRegistrationRepository clientRegistrationRepository,
            @Value("${postLogoutRedirectUri}") String postLogoutRedirectUri
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        super.logout(httpServletRequest, httpServletResponse, authentication);
        logger.debug("Authentication: {}", JsonUtil.stringify(authentication));

        String issuer = Optional.ofNullable(this.clientRegistrationRepository.findByRegistrationId("logto"))
                .map(ClientRegistration::getProviderDetails)
                .map(ClientRegistration.ProviderDetails::getConfigurationMetadata)
                .map(o -> o.get("issuer"))
                .filter(o -> o instanceof String)
                .map(o -> (String) o)
                .orElseThrow(() -> new RuntimeException("Issuer not found or invalid."));
        String idTokenValue = Optional.ofNullable(authentication.getPrincipal())
                .filter(o -> o instanceof OidcUser)
                .map(o -> (OidcUser) o)
                .map(OidcUser::getIdToken)
                .map(AbstractOAuth2Token::getTokenValue)
                .orElseThrow(() -> new RuntimeException("ID token not found or invalid."));
        String signOutUrl = UriComponentsBuilder.fromHttpUrl(issuer + "/session/end?id_token_hint={clientId}&post_logout_redirect_uri={postLogoutRedirectUri}")
                .encode()
                .buildAndExpand(idTokenValue, postLogoutRedirectUri)
                .toUriString();

        try {
            logger.debug("Redirecting to sign-out URL: {}", signOutUrl);
            httpServletResponse.sendRedirect(signOutUrl);
        } catch (IOException e) {
            logger.error("Failed to redirect to sign-out URL.", e);
        }
    }

}
