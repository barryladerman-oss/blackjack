package us.laderman.blackjack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
/**
 * Controller to handle redirect from /doc to the Javadoc index page.
 */
public class DocRedirectController {
    /**
     * Redirects /doc to /doc/index.html for serving Javadoc documentation.
     *
     * @return redirect string to Javadoc index page
     */
    @GetMapping("/doc")
    public String redirectToDocIndex() {
        return "redirect:/doc/index.html";
    }
}
