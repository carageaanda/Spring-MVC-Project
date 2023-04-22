package com.example.project.service;

import com.example.project.exception.CustomException;
import com.example.project.model.Artist;
import com.example.project.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArtistServiceTest {

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Mock
    private ArtistRepository artistRepository;

    private static Artist artist1, artist2, artist3;

    @BeforeAll
    static void createPatientObjects() {
        artist1 = new Artist();
        artist2 = new Artist();
        artist3 = new Artist();

        artist1.setId(1L);
        artist1.setCnp("1781123282920");
        artist1.setLastName("NumeTest1");
        artist1.setFirstName("PrenumeTest1");

        artist2.setId(2L);
        artist2.setCnp("1980023082920");
        artist2.setLastName("NumeTest2");
        artist2.setFirstName("PrenumeTest2");

        artist3.setId(3L);
        artist3.setCnp("2934255082920");
        artist3.setLastName("NumeTest3");
        artist3.setFirstName("PrenumeTest3");
    }

    @Test
    public void findAllArtists() {
        when(artistRepository.findAll()).thenReturn(List.of(artist1, artist2, artist3));
        List<Artist> result = artistService.getAllArtists();

        assertEquals(3, result.size());
        verify(artistRepository, times(1)).findAll();
    }

    @Test
    public void findById() {
        when(artistRepository.findById(1L)).thenReturn(Optional.ofNullable(artist1));
        Artist result = artistService.getArtistById(1L);

        assertNotNull(result);
        verify(artistRepository, times(1)).findById(1L);
    }

    @Test
    public void saveArtist_cnpNotFound() {
        when(artistRepository.getByCnp("1781123282920")).thenReturn(Optional.empty());
        when(artistRepository.save(artist1)).thenReturn(artist1);

        Artist result = artistService.saveArtist(artist1);
        assertNotNull(result);
        assertEquals("1781123282920", result.getCnp());

        verify(artistRepository, times(0)).findById(1L);
        verify(artistRepository, times(1)).save(artist1);
    }

    @Test
    public void saveArtist_cnpFound_Edit() {
        when(artistRepository.getByCnp("1781123282920")).thenReturn(Optional.ofNullable(artist1));
        when(artistRepository.save(artist1)).thenReturn(artist1);

        Artist result = artistService.saveArtist(artist1);
        assertNotNull(result);
        assertEquals("1781123282920", result.getCnp());

        verify(artistRepository, times(0)).findById(1L);
        verify(artistRepository, times(1)).save(artist1);
    }

    @Test
    public void saveArtist_cnpFound_cnpAlreadyTaken() {
        artist1.setId(null);
        when(artistRepository.getByCnp("1781123282920")).thenReturn(Optional.ofNullable(artist1));

        try {
            artistService.saveArtist(artist1);
        }catch (CustomException e) {
            assertEquals("CNP 1781123282920 already taken!", e.getMessage());
        }

        verify(artistRepository, times(0)).findById(1L);
        verify(artistRepository, times(0)).save(artist1);
    }

    @Test
    public void deleteArtist() {
        Long id = 1L;
        doNothing().when(artistRepository).deleteById(id);
        artistService.deleteArtistById(id);
        verify(artistRepository, times(1)).deleteById(id);
    }
}
