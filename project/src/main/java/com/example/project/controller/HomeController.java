package com.example.project.controller;

import com.example.project.exception.NotUniqueEmailException;
import com.example.project.exception.NotUniqueUsernameException;
import com.example.project.model.Manager;
import com.example.project.model.security.User;
import com.example.project.service.ManagerServiceImpl;
import com.example.project.service.RecordLabelServiceImpl;
import com.example.project.service.security.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Set;

import static com.example.project.configuration.SecurityConfiguration.ROLE_MANAGER;
import static com.example.project.controller.RecordLabelController.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    public final static String ACCESS_DENIED = "access-denied";
    private final static String REGISTER_FORM = "register_form";
    private final static String REGISTER = "register";
    private final static String LOGIN = "login";

    private final PasswordEncoder passwordEncoder;
    private final RecordLabelServiceImpl recordLabelService;
    private final AuthorityService authorityService;
    private final ManagerServiceImpl managerService;

    @GetMapping({"", "/", "/index"})
    public String recordLabelsList(Model model) {
        model.addAttribute("recordLabels", recordLabelService.getAllRecordLabels());
        return ALL_RECORDLABELS;
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        } else {
            User user = (User) model.getAttribute("user");
            user.setPassword("");
        }
        if (!model.containsAttribute("manager")) {
            model.addAttribute("manager", new Manager());
        }
        model.addAttribute("password", "");
        model.addAttribute("recordLabelAll", recordLabelService.getAllRecordLabels());

        return REGISTER_FORM;
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") @Valid User user, BindingResult bindingResultUser,
                                  @ModelAttribute("manager") @Valid Manager manager, BindingResult bindingResultManager,
                                  @ModelAttribute("password") String password, RedirectAttributes attr) {
        if (bindingResultUser.hasErrors() || bindingResultManager.hasErrors()) {
            attr.addFlashAttribute(BINDING_RESULT_PATH + "user", bindingResultUser);
            attr.addFlashAttribute(BINDING_RESULT_PATH + "manager", bindingResultManager);
            attr.addFlashAttribute("user", user);
            attr.addFlashAttribute("manager", manager);

            if (bindingResultUser.getFieldError("password") != null) {
                attr.addFlashAttribute("error_password", bindingResultUser.getFieldError("password").getDefaultMessage());
            }

            return REDIRECT + REGISTER;
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setAuthorities(Set.of(authorityService.getByRole(ROLE_MANAGER)));

        user.setManagers(manager);
        manager.setUser(user);

        try {
            managerService.saveOrUpdateUser(user, manager, password);
        } catch (NotUniqueEmailException e) {
            attr.addFlashAttribute(BINDING_RESULT_PATH + "user", bindingResultUser);
            attr.addFlashAttribute(BINDING_RESULT_PATH + "manager", bindingResultManager);
            attr.addFlashAttribute("user", user);
            attr.addFlashAttribute("manager", manager);
            attr.addFlashAttribute("error_email", e.getMessage());
            return REDIRECT + REGISTER;
        } catch (NotUniqueUsernameException e) {
            attr.addFlashAttribute(BINDING_RESULT_PATH + "user", bindingResultUser);
            attr.addFlashAttribute(BINDING_RESULT_PATH + "manager", bindingResultManager);
            attr.addFlashAttribute("user", user);
            attr.addFlashAttribute("manager", manager);
            attr.addFlashAttribute("error_username", e.getMessage());
            return REDIRECT + REGISTER;
        }
        return REDIRECT + LOGIN;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError() {
        return "login-error";
    }

    @GetMapping("/" + ACCESS_DENIED)
    public String accessDenied() {
        return "err_access_denied";
    }
}
