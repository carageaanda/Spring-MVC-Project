package com.example.project.controller;

import com.example.project.model.Manager;
import com.example.project.model.security.User;
import com.example.project.service.ManagerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test for {@link com.example.project.controller.ManagerController}
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
public class ManagerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ManagerServiceImpl managerService;

    // get all

    @Test
    @WithAnonymousUser
    public void showManagers_anonymous_success() throws Exception {
        mockMvc.perform(get("/managers"))
                .andExpect(status().isOk())
                .andExpect(view().name("managers"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    public void showDoctors_admin_success() throws Exception {
        mockMvc.perform(get("/managers"))
                .andExpect(status().isOk())
                .andExpect(view().name("managers"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    public void showManagerr_manager_success() throws Exception {
        mockMvc.perform(get("/managers"))
                .andExpect(status().isOk())
                .andExpect(view().name("managers"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }


    // get by id


    @Test
    @WithAnonymousUser
    public void showManager_anonymous_success() throws Exception {
        mockMvc.perform(get("/managers/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager_info"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    public void showManager_admin_success() throws Exception {
        mockMvc.perform(get("/managers/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager_info"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    public void showManager_manager_success() throws Exception {
        mockMvc.perform(get("/managers/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager_info"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    // edit manager (GET)


    @Test
    @WithAnonymousUser
    public void editManager_anonymous_fail() throws Exception {
        mockMvc.perform(get("/managers/{id}/edit", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    public void editManager_admin_success() throws Exception {
        mockMvc.perform(get("/managers/{id}/edit", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager_form"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    public void editManager_manager_success() throws Exception {
        mockMvc.perform(get("/managers/{id}/edit", "2"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("err_access_denied"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    public void editManager_manager_fail() throws Exception {
        mockMvc.perform(get("/managers/{id}/edit", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/managers/my-profile"));
    }


    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    public void editManager_POST_fail() throws Exception {

        Manager manager = managerService.getById(1L);
        User user = manager.getUser();

        manager.setLastName("LastNameTest");
        manager.setFirstName("FirstNameTest");
        user.setUsername("");

        mockMvc.perform(post("/managers")
                        .flashAttr("manager", manager)
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/managers/my-profile"));
    }

    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    public void editManager_POST_success() throws Exception {

        Manager manager = managerService.getById(1L);
        User user = manager.getUser();

        manager.setLastName("LastNameTest");
        manager.setFirstName("FirstNameTest");
        user.setEmail("mailtest@gmail.com");

        mockMvc.perform(post("/managers")
                        .flashAttr("manager", manager)
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/managers/my-profile"));

        manager = managerService.getById(1L);
        assertEquals("LastNameTest", manager.getLastName());
        assertEquals("FirstNameTest", manager.getFirstName());
        assertEquals("mailtest@gmail.com", manager.getUser().getEmail());
    }

    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    public void editManager_POST_admin_fail() throws Exception {

        Manager manager = managerService.getById(1L);
        User user = manager.getUser();

        manager.setLastName("LastNameTest");
        manager.setFirstName("FirstNameTest");
        user.setUsername("");

        mockMvc.perform(post("/managers")
                        .flashAttr("manager", manager)
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/managers/1/edit"));
    }

    @Test
    @WithAnonymousUser
    public void myProfileManager_anonymous_fail() throws Exception {
        mockMvc.perform(get("/managers/my-profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    public void myProfileManager_admin_fail() throws Exception {
        mockMvc.perform(get("/managers/my-profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    public void myProfileManager_manager_success() throws Exception {
        mockMvc.perform(get("/managers/my-profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager_form"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }


    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    public void deleteManager_admin_success() throws Exception {
        mockMvc.perform(get("/managers/{id}/delete", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/managers"));
    }

    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "DOCTOR")
    public void deleteManager_manager_fail() throws Exception {
        mockMvc.perform(get("/managers/{id}/delete", "1"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    @WithAnonymousUser
    public void deleteManager_anonymous_fail() throws Exception {
        mockMvc.perform(get("/managers/{id}/delete", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}
