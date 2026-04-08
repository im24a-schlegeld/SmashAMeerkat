package com.meerkat.smashameerkat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the main page of the game.
 */
@Controller
public class HomeController {

    /**
     * Returns the template name for the home screen.
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
}
