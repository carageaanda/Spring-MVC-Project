package com.example.project;

import com.example.project.model.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("h2")
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CascadeTypeTest {
    @Autowired
    private EntityManager entityManager;

    @Test
    public void oneToOne_save() {
        Artist artist = new Artist();
        artist.setCnp("8980505017464");
        artist.setConsults(new ArrayList<>());
        artist.setFirstName("Adele");
        artist.setLastName("Adkins");
        artist.setStageName("Adele");

        Address address = new Address();
        address.setCity("Bucuresti");
        address.setNo(2);
        address.setStreet("Bulevardul Iuliu Maniu");

        artist.setAddress(address);

        entityManager.persist(artist);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void oneToOne_update() {
        Artist artist = entityManager.find(Artist.class, 1L);

        Address address = artist.getAddress();
        address.setStreet("Bulevardul Energeticienilor");
        address.setNo(13);
        address.setCity("Bucuresti");

        artist.setAddress(address);

        entityManager.merge(artist);
        entityManager.flush();

        artist = entityManager.find(Artist.class, 1L);
        assertEquals("Bulevardul Energeticienilor", artist.getAddress().getStreet());
        assertEquals("Bucuresti", artist.getAddress().getCity());
        assertEquals(13, artist.getAddress().getNo());
        assertNotNull(artist.getAddress().getId());
    }

    @Test
    public void oneToOne_remove() {
        Artist artist = entityManager.find(Artist.class, 1L);
        Long addressId = artist.getAddress().getId();

        entityManager.remove(artist);
        Address address = entityManager.find(Address.class, addressId);
        assertNull(address);
    }

    @Test
    public void oneToMany_save() {
        RecordLabel recordLabel = entityManager.find(RecordLabel.class, 1L);

        Artist artist = new Artist();
        artist.setFirstName("Adele");
        artist.setLastName("Adkins");
        artist.setStageName("Adele");
        artist.setCnp("8980505017464");

        recordLabel.getArtists().add(artist);

        entityManager.persist(recordLabel);

        assertNotNull(recordLabel.getArtists().get(recordLabel.getArtists().size() - 1).getId());
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void oneToMany_delete() {
        RecordLabel recordLabel = entityManager.find(RecordLabel.class, 1L);
        List<Long> artistIds = recordLabel.getArtists().stream().map(Artist::getId).collect(Collectors.toList());
        if (artistIds.size() == 0) {
            fail();
        }
        for (Artist artist : recordLabel.getArtists()) {
            List<Album> albums = artist.getAlbums();
            for (Album album : albums) {
                entityManager.remove(album);
            }
            entityManager.remove(artist);
        }
        entityManager.remove(recordLabel);
        entityManager.flush();

        artistIds.forEach(id -> assertNull(entityManager.find(Artist.class, id)));
    }


}
