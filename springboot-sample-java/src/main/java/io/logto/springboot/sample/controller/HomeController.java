package io.logto.springboot.sample.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            model.addAttribute("profile", oidcUser.getClaims());
        }
        return "index";
    }
}
