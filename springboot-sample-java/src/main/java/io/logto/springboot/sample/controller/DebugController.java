package io.logto.springboot.sample.controller;

import io.logto.springboot.sample.util.JsonUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DebugController {

    @GetMapping("/debug")
    public String debug(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        model.addAttribute("oidcUser", oidcUser);
        model.addAttribute("oidcUserJson", JsonUtil.stringify(oidcUser));
        model.addAttribute("claims", oidcUser.getClaims());
        model.addAttribute("claimsJson", JsonUtil.stringify(oidcUser.getClaims()));
        return "debug";
    }
}
