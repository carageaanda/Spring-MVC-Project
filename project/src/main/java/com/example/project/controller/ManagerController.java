package com.example.project.controller;

import com.example.project.exception.ForbiddenException;
import com.example.project.exception.NotUniqueEmailException;
import com.example.project.exception.NotUniqueUsernameException;
import com.example.project.model.Manager;
import com.example.project.model.security.User;
import com.example.project.service.ManagerServiceImpl;
import com.example.project.service.RecordLabelServiceImpl;
import com.example.project.service.security.AuthorityService;
import com.example.project.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Set;

import static com.example.project.configuration.SecurityConfiguration.ROLE_MANAGER;
import static com.example.project.controller.RecordLabelController.BINDING_RESULT_PATH;
import static com.example.project.controller.RecordLabelController.REDIRECT;

@Controller
@RequestMapping("/managers")
@RequiredArgsConstructor
@Slf4j
public class ManagerController {

    private final static String ALL_MANAGERS = "managers";
    private final static String VIEW_MANAGER = "manager_info";
    private final static String EDIT_MANAGER = "manager_form";

    private final ManagerServiceImpl managerService;
    private final RecordLabelServiceImpl recordLabelService;
    private final UserService userService;
    private final AuthorityService authorityService;

    @GetMapping
    public String getAll(Model model) {
        var managers = managerService.getAllManagers();
        model.addAttribute("managers", managers);
        return ALL_MANAGERS;
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable("id") String managerId, Model model) {
        var manager = managerService.getById(Long.valueOf(managerId));
        model.addAttribute("manager", manager);
        return VIEW_MANAGER;
    }

    @GetMapping("/my-profile")
    public String getMyProfile(Model model) {
        // here is the view accessible only to the managers where they can see info about themselves and edit data
        var user = userService.getCurrentUser();
        var manager = user.getManagers();

        if (!model.containsAttribute("user")) {
            model.addAttribute("user", user);
        } else {
            user = (User) model.getAttribute("user");
            user.setPassword("");
        }
        if (!model.containsAttribute("manager")) {
            model.addAttribute("manager", manager);
        }
        model.addAttribute("recordLabelAll", recordLabelService.getAllRecordLabels());
        model.addAttribute("isManager", true);
        model.addAttribute("password", "");

        return EDIT_MANAGER;
    }

    @GetMapping("/{id}/edit")
    public String editManager(@PathVariable("id") String managerId, Model model) {
        if (userService.isManager()) {
            if (!userService.checkIfCurrentUserIsSameManager(Long.valueOf(managerId))) {
                throw new ForbiddenException();
            } else {
                return REDIRECT + ALL_MANAGERS + "/my-profile";
            }
        }
        // this view is intended only for admins. Managers can change their role in the "my-profile" section
        var manager = managerService.getById(Long.valueOf(managerId));
        var user = manager.getUser();

        if (!model.containsAttribute("user")) {
            model.addAttribute("user", user);
        } else {
            user = (User) model.getAttribute("user");
            user.setPassword("");
        }
        if (!model.containsAttribute("manager")) {
            model.addAttribute("manager", manager);
        }

        model.addAttribute("password", "");
        model.addAttribute("recordLabelAll", recordLabelService.getAllRecordLabels());
        model.addAttribute("isManager", false);

        return EDIT_MANAGER;
    }

    @PostMapping
    public String saveOrUpdate(@ModelAttribute("user") @Valid User user, BindingResult bindingResultUser,
                               @ModelAttribute("manager") @Valid Manager manager, BindingResult bindingResultManager,
                               @ModelAttribute("password") String password, RedirectAttributes attr) {

        if (bindingResultUser.hasErrors() || bindingResultManager.hasErrors()) {
            log.info("Model binding has errors!");

            attr.addFlashAttribute(BINDING_RESULT_PATH + "user", bindingResultUser);
            attr.addFlashAttribute(BINDING_RESULT_PATH + "manager", bindingResultManager);
            attr.addFlashAttribute("user", user);
            attr.addFlashAttribute("manager", manager);

            if (userService.isManager()) {
                return REDIRECT + ALL_MANAGERS + "/my-profile";
            } else {
                return REDIRECT + ALL_MANAGERS + "/" + manager.getId() + "/edit";
            }
        }

        user.setAuthorities(Set.of(authorityService.getByRole(ROLE_MANAGER)));
        user.setManagers(manager);
        manager.setUser(user);

        try {
            managerService.saveOrUpdateUser(user, manager, password);
        } catch (NotUniqueEmailException e) {
            log.info("Error when saving into database! Error message = " + e.getMessage());

            attr.addFlashAttribute(BINDING_RESULT_PATH + "user", bindingResultUser);
            attr.addFlashAttribute(BINDING_RESULT_PATH + "manager", bindingResultManager);
            attr.addFlashAttribute("user", user);
            attr.addFlashAttribute("manager", manager);
            attr.addFlashAttribute("error_email", e.getMessage());
            return REDIRECT + ALL_MANAGERS + "/" + manager.getId() + "/edit";
        } catch (NotUniqueUsernameException e) {
            log.info("Error when saving into database! Error message = " + e.getMessage());

            attr.addFlashAttribute(BINDING_RESULT_PATH + "user", bindingResultUser);
            attr.addFlashAttribute(BINDING_RESULT_PATH + "manager", bindingResultManager);
            attr.addFlashAttribute("user", user);
            attr.addFlashAttribute("manager", manager);
            attr.addFlashAttribute("error_username", e.getMessage());
            return REDIRECT + ALL_MANAGERS + "/" + manager.getId() + "/edit";
        }
        return REDIRECT + ALL_MANAGERS;
    }

    @GetMapping("/{id}/delete")
    public String deleteManager(@PathVariable Long id) {
        managerService.deleteManagerById(id);
        return REDIRECT + ALL_MANAGERS;
    }
}
