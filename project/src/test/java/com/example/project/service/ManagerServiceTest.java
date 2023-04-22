package com.example.project.service;

import com.example.project.exception.EntityNotFoundException;
import com.example.project.model.Consult;
import com.example.project.model.Manager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
/**
 * Test for {@link com.example.project.service.ManagerServiceImpl}
 */
public class ManagerServiceTest {

    @Autowired
    private ManagerServiceImpl managerService;

    @Test
    public void getAll() {
        List<Manager> managers = managerService.getAllManagers();

        assertEquals(3, managers.size());
        assertEquals("Smith", managers.get(0).getLastName());
        assertEquals("Lee", managers.get(1).getLastName());
        assertEquals("Garcia", managers.get(2).getLastName());
    }

    @Test
    public void getById() {
        Manager manager = managerService.getById(1L);
        assertNotNull(manager);
        assertEquals("Smith", manager.getLastName());
        assertEquals(1L, manager.getRecordLabel().getId());
    }

//    @Test
//    public void getAllConsultsForManager() {
//        List<Consult> consults = managerService.getAllConsultsForManager(2L);
//        assertEquals(1, consults.size());
//        assertEquals(2L, consults.get(0).getId());
//        assertEquals(3L, consults.get(1).getId());
//    }

    @Test
    public void saveManager() {
        Manager manager = new Manager();
        manager.setLastName("NumeTest");
        manager.setFirstName("PrenumeTest");

        managerService.saveManager(manager);
        List<Manager> managers = managerService.getAllManagers();
        Manager lastManager = managers.get(managers.size() - 1);

        assertEquals("NumeTest", lastManager.getLastName());
        assertEquals("PrenumeTest", lastManager.getFirstName());
    }

    @Test
    public void removeManager() {
        managerService.deleteManagerById(3L);
        assertThrows(EntityNotFoundException.class, () -> managerService.getById(3L));
    }
}
