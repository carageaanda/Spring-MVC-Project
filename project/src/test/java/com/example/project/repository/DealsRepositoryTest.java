package com.example.project.repository;

import com.example.project.model.Deals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("h2")
public class DealsRepositoryTest {

    @Autowired
    private DealsRepository dealsRepository;

    @Test
    public void addDeal() throws ParseException {
        Deals deal = new Deals();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        deal.setSigningDate(dateFormat.parse("2018-04-06"));
        deal.setContractLength(10);

        dealsRepository.save(deal);
    }


    @Test
    public void findDealsBySigningDateAndContractLength() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date signingDate = formatter.parse("2018-04-16");

        Optional<Deals> deals = dealsRepository.findDealsBySigningDateAndContractLength(signingDate, 10);
        assertTrue(deals.isPresent());
    }


    @Test
    public void findBySigningDateAndContractLength_notFound() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date signingDate = formatter.parse("2018-04-16");
        Optional<Deals> deals = dealsRepository.findDealsBySigningDateAndContractLength(signingDate, 500);
        assertTrue(deals.isEmpty());
    }


    @Test
    public void findByIdIsIn() {
        List<Deals> deals = dealsRepository.findByIdIsIn(Arrays.asList(1L, 2L));
        assertFalse(deals.isEmpty());
    }

    @Test
    public void findAll() {
        List<Deals> deals = dealsRepository.findAll();
        assertEquals(2, deals.size());
    }

    @Test
    public void removeDeal() {
        dealsRepository.deleteById(1L);
        assertTrue(dealsRepository.findById(1L).isEmpty());
    }
}
