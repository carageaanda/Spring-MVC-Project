package com.example.project.controller;

import com.example.project.exception.CustomException;
import com.example.project.model.Address;
import com.example.project.model.Artist;
import com.example.project.service.ArtistServiceImpl;
import com.example.project.service.RecordLabelServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import static com.example.project.controller.RecordLabelController.BINDING_RESULT_PATH;
import static com.example.project.controller.RecordLabelController.REDIRECT;

@Controller
@RequestMapping("/artists")
@RequiredArgsConstructor
@Slf4j
public class ArtistController {

    private final static String ALL_ARTISTS = "artists";
    private final static String VIEW_ARTIST = "artist_info";
    private final static String ADD_EDIT_ARTIST = "artist_form";

    private final ArtistServiceImpl artistService;
    private final RecordLabelServiceImpl recordLabelService;

    @GetMapping
    public String getAll(Model model) {
        var artists = artistService.getAllArtists();
        model.addAttribute("artists", artists);
        return ALL_ARTISTS;
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable("id") String artistId, Model model) {
        var artist = artistService.getArtistById(Long.valueOf(artistId));
        model.addAttribute("artist", artist);
        return VIEW_ARTIST;
    }

    @GetMapping("/new")
    public String addArtist(Model model) {
        if (!model.containsAttribute("artist")) {
            model.addAttribute("artist", new Artist());
        }
        if (!model.containsAttribute("address")) {
            model.addAttribute("address", new Address());
        }
        model.addAttribute("recordLabelAll", recordLabelService.getAllRecordLabels());

        return ADD_EDIT_ARTIST;
    }

    @GetMapping("/{id}/edit")
    public String editArtist(@PathVariable("id") String artistId, Model model) {
        var artist = artistService.getArtistById(Long.valueOf(artistId));
        var recordLabels = recordLabelService.getAllRecordLabels();

        if (!model.containsAttribute("artist")) {
            model.addAttribute("artist", artist);
        }
        if (!model.containsAttribute("address")) {
            model.addAttribute("address", artist.getAddress());
        }
        model.addAttribute("recordLabelsAll", recordLabels);

        return ADD_EDIT_ARTIST;
    }

    @PostMapping
    public String saveOrUpdate(@ModelAttribute("artist") @Valid Artist artist, BindingResult bindingResultArtist,
                               @ModelAttribute("address") @Valid Address address, BindingResult bindingResultAddress,
                               RedirectAttributes attr) {
        if (bindingResultArtist.hasErrors() || bindingResultAddress.hasErrors()) {
            log.info("Model binding has errors!");

            attr.addFlashAttribute(BINDING_RESULT_PATH + "artist", bindingResultArtist);
            attr.addFlashAttribute(BINDING_RESULT_PATH + "address", bindingResultAddress);
            attr.addFlashAttribute("artist", artist);
            attr.addFlashAttribute("address", address);

            if (artist.getId() != null) {
                log.info(String.format("Redirected back to endpoint %s", ALL_ARTISTS + "/" + artist.getId() + "/edit"));
                return REDIRECT + ALL_ARTISTS + "/" + artist.getId() + "/edit";
            } else {
                log.info(String.format("Redirected back to endpoint %s", ALL_ARTISTS + "/new"));
                return REDIRECT + ALL_ARTISTS + "/new";
            }
        }

        address.setArtist(artist);
        artist.setAddress(address);

        try {
            artistService.saveArtist(artist);
        } catch (CustomException e) {
            log.info("Error when saving into database! Error message = " + e.getMessage());

            attr.addFlashAttribute(BINDING_RESULT_PATH + "artist", bindingResultArtist);
            attr.addFlashAttribute("artist", artist);
            attr.addFlashAttribute("error_cnp", e.getMessage());

            if (artist.getId() == null) {
                log.info(String.format("Redirected back to endpoint %s", ALL_ARTISTS + "/new"));
                return REDIRECT + ALL_ARTISTS + "/new";
            } else {
                log.info(String.format("Redirected back to endpoint %s", ALL_ARTISTS + "/" + artist.getId() + "/edit"));
                return REDIRECT + ALL_ARTISTS + "/" + artist.getId() + "/edit";
            }
        }

        return REDIRECT + ALL_ARTISTS;
    }

    @GetMapping("/{id}/delete")
    public String deleteArtist(@PathVariable Long id) {
        artistService.deleteArtistById(id);
        return REDIRECT + ALL_ARTISTS;
    }
}
