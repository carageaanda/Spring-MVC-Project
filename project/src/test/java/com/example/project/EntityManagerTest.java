package com.example.project;

import com.example.project.model.Artist;
import com.example.project.model.Consult;
import com.example.project.model.Deals;
import com.example.project.model.Manager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Rollback(false)
@ActiveProfiles("h2")
@Slf4j
public class EntityManagerTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @Order(1)
    public void findManager() {
        Manager manager = entityManager.find(Manager.class, 1L);
        assertNotNull(manager);
        assertEquals("John", manager.getFirstName());
        assertEquals("Smith", manager.getLastName());
        assertEquals(1L, manager.getRecordLabel().getId());
    }

    @Test
    @Order(2)
    public void updateManager() {
        Manager manager = entityManager.find(Manager.class, 1L);
        manager.setLastName("SmithTest");
        entityManager.persist(manager);
        entityManager.flush();
    }

    @Test
    @Order(3)
    public void findConsult() {
        Consult consult = entityManager.find(Consult.class, 1L);
        assertNotNull(consult);
        assertEquals(1L, consult.getManager().getId());
        assertEquals(1L, consult.getArtist().getId());
        assertEquals("We had a great discussion about her new album. We talked about the concept and direction of the album and I gave her some ideas on how to promote it", consult.getComment());
    }

    @Test
    @Order(4)
    public void updateConsult() {
        Consult consult = entityManager.find(Consult.class, 1L);
        consult.setDate(new Date());
        consult.setComment("new comment");
        consult.setDeals(new ArrayList<>());

        Artist newArtist = entityManager.find(Artist.class, 3L);
        consult.setArtist(newArtist);

        entityManager.persist(consult);
        entityManager.flush();
    }

    @Test
    @Order(5)
    public void findDeal() {
        Deals deals = entityManager.find(Deals.class, 1L);
        assertNotNull(deals);
        assertEquals("2018-04-16", deals.getSigningDate().toString());
        assertEquals(10, deals.getContractLength());
    }


    @Test
    @Order(6)
    public void updateDeal() throws ParseException {
        Deals deals = entityManager.find(Deals.class, 1L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date signingDate = dateFormat.parse("2018-10-16");
        deals.setSigningDate(signingDate);
        deals.setContractLength(15);
        entityManager.persist(deals);
        entityManager.flush();
    }

}
