package sk.kubena.fakenews.user;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sk.kubena.fakenews.error.UserAlreadyExistException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Qualifier("messageSource")
    @Autowired
    private MessageSource messages;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String showRegistrationForm(final HttpServletRequest request, Model model) {
        UserDTO userDto = new UserDTO();
        model.addAttribute("user", userDto);
        return "views/registration";
    }

    @PostMapping("/user/registration")
    public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid final UserDTO userDto, final HttpServletRequest request, final Errors errors) {
        LOGGER.info("Registering user account with information: {}", userDto);

        try {
            final User registered = userService.registerNewUserAccount(userDto);

            final String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), appUrl));
        } catch (final UserAlreadyExistException uaeEx) {
            ModelAndView mav = new ModelAndView("views/registration", "user", userDto);
            String errMessage = messages.getMessage("message.regError", null, request.getLocale());
            mav.addObject("message", errMessage);
            return mav;
        } catch (final RuntimeException ex) {
            LOGGER.info("Unable to register user", ex);
            return new ModelAndView("views/emailError", "user", userDto);
        }

        return new ModelAndView("views/successRegister", "user", userDto);
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());

        return "views/users";
    }

    @PutMapping(path = "/users/enabled", consumes = "application/json")
    public ResponseEntity<String> changeAccountStatus(@RequestBody String request) {

        if (request == null || request.isEmpty()) {
            LOGGER.info("Invalid request: {}", request);
            return ResponseEntity.badRequest().body("Invalid request!");

        } else {
            JSONObject jsonObject = new JSONObject(request);
            int id = Integer.parseInt(jsonObject.get("id").toString());
            boolean value = Boolean.parseBoolean(jsonObject.get("value").toString());

            if (userService.getUser(id) == null) {
                LOGGER.info("User not found!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");

            } else if (value) {
                LOGGER.info("User {} enabled!", id);
                userService.changeEnabledValue(id, true);
                return ResponseEntity.ok().body("User enabled!");

            } else {
                LOGGER.info("User {} disabled!", id);
                userService.changeEnabledValue(id, false);
                return ResponseEntity.ok().body("User disabled!");
            }
        }
    }

    @PutMapping(path = "/users/role", consumes = "application/json")
    public ResponseEntity<String> changeAccountRole(@RequestBody String request) {

        // check if request is null
        if (request == null || request.isEmpty()) {
            LOGGER.info("Invalid request: {}", request);
            return ResponseEntity.badRequest().body("Invalid request!");

            // if request is not null
        } else {
            JSONObject jsonObject = new JSONObject(request);
            int id = Integer.parseInt(jsonObject.get("id").toString());
            String role = jsonObject.get("role").toString();
            LOGGER.info("{} {}", id, role);

            // check if user does not exist
            if (userService.getUser(id) == null) {
                LOGGER.info("User not found!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");

                // if role is USER, change it to ADMIN
            } else if (role.equals("ROLE_USER")) {
                userService.changeRole(id, "ROLE_ADMIN");
                LOGGER.info("User {} promoted to ADMIN!", id);
                return ResponseEntity.ok().body("User promoted!");

                // if role is ADMIN, change it to USER
            } else if (role.equals("ROLE_ADMIN")) {
                userService.changeRole(id, "ROLE_USER");
                LOGGER.info("User {} demoted to USER!", id);
                return ResponseEntity.ok().body("User demoted!");

                // invalid role
            } else {
                LOGGER.info("Invalid role: {}", role);
                return ResponseEntity.badRequest().body("Invalid role!");
            }
        }
    }
}
