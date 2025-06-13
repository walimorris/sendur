package io.sendur.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MasterController {
    public static final Logger LOGGER = LoggerFactory.getLogger(MasterController.class);

    /**
     * The Master Controller forwards the specified routes to the built index.html.
     * The reason behind this is that we are using client-side routing with React.
     * For example, when the Spring Boot webserver sees a route like '/dashboard',
     * attempting to process this route directly will result in an error since
     * there is no '/dashboard' template to render. Instead, we forward these
     * routes to our '/built/index.html' static template, delegating all routing
     * to React, which will then render the correct React component.
     *
     * @return forwarding route to index.html
     */
    @RequestMapping(value = {
            "/",
            "/{path:^(?!api|oauth2|login|logout|built|favicon\\.ico$).*$}"
    })
    public String forward() {
        return "forward:/built/index.html";
    }
}
