package ${package}.server.configuration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home redirection to swagger api documentation
 */
@Controller
public class HomeController {

  @GetMapping(path = "/")
  public String index() {
    return "redirect:swagger-ui/";
  }

  @GetMapping(path = "/swagger-ui.html")
  public String springFoxLegacy() {
    return "redirect:swagger-ui/";
  }

}
