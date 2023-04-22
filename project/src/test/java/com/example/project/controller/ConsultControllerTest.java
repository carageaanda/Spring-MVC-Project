package com.example.project.controller;

import com.example.project.exception.EntityNotFoundException;
import com.example.project.model.Artist;
import com.example.project.model.Consult;
import com.example.project.model.Deals;
import com.example.project.model.Manager;
import com.example.project.service.ConsultServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test for {@link ConsultController}
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
public class ConsultControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private ConsultServiceImpl consultService;

    @MockBean
    Model model;

    private Manager manager1;
    private Artist artist1;
    private Consult consult1, consult2, consult3;
    private Deals deal;

    @BeforeEach
    public void setUp() {
        manager1 = new Manager();
        manager1.setLastName("NumeManagerTest");
        manager1.setFirstName("PrenumeManagerTest");

        artist1 = new Artist();
        artist1.setLastName("NumeArtistTest");
        artist1.setFirstName("PrenumeArtistTest");


        deal = new Deals();
        deal.setSigningDate(new Date());
        deal.setContractLength(4);

        consult1 = new Consult();
        consult1.setComment("Comment 1");
        consult1.setDate(new Date());
        consult1.setArtist(artist1);
        consult1.setManager(manager1);

        consult2 = new Consult();
        consult2.setComment("Comment 2");
        consult2.setDate(new Date());
        consult2.setArtist(artist1);
        consult2.setManager(manager1);


        consult3 = new Consult();
        consult3.setComment("Comment 3");
        consult3.setDate(new Date());
        consult2.setArtist(artist1);
        consult2.setManager(manager1);
        consult3.setDeals(List.of(deal));

    }

    @Test
    @WithAnonymousUser
    public void accessConsults_Fails() throws Exception {
        mockMvc.perform(get("/consults"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    public void accessConsults_Admin_Success() throws Exception {
        var consults = new PageImpl<>(List.of(consult1, consult2));
        when(consultService.getAllConsults(any())).thenReturn(consults);
        mockMvc.perform(get("/consults"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("consults", consults));
    }

    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    public void accessConsults_Manager_Success() throws Exception {
        var consults = new PageImpl<>(List.of(consult1, consult2));
        when(consultService.getAllConsults(any())).thenReturn(consults);
        mockMvc.perform(get("/consults"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("consults", consults));
    }

    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    public void accessConsult_Admin_Fails() throws Exception {
        when(consultService.getConsultById(1L)).thenThrow(new EntityNotFoundException("Consult", 1L));
        mockMvc.perform(get("/consults/{id}", "1"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("err_not_found"));
    }



    @Test
    @WithAnonymousUser
    public void accessNewConsult_Anonymous_Success() throws Exception {
        mockMvc.perform(get("/consults/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }


    @Test
    @WithMockUser(username = "manager_1", password = "123456", roles = "MANAGER")
    void accessMovieDeleteAccessDenied() throws Exception {
        doNothing().when(consultService).deleteConsultById(1L);
        mockMvc.perform(get("/consults/1/delete"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "admin_1", password = "123456", roles = "ADMIN")
    void accessMovieDeleteSuccess() throws Exception {
        doNothing().when(consultService).deleteConsultById(1L);
        mockMvc.perform(get("/consults/1/delete"))
                .andExpect(redirectedUrl("/consults"));
    }

}
