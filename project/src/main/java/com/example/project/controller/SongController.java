package com.example.project.controller;

import com.example.project.exception.CustomException;
import com.example.project.model.Song;
import com.example.project.model.dto.AlbumSongDTO;
import com.example.project.model.dto.ArtistSongDTO;
import com.example.project.service.SongServiceImpl;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@Setter
@RequestMapping("/songs")
@NoArgsConstructor
@Slf4j
public class SongController {
    public final static String REDIRECT = "redirect:/";

    public final static String ALL_SONGS = "songs";
    private final static String VIEW_SONGS = "songs_info";


    @Autowired
    private SongServiceImpl songService;

    @GetMapping
    public String getAllSongs(Model model,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "10") Integer size,
                              @RequestParam(value = "sortBy", defaultValue = "songId") String sortBy) {
        var songs = songService.getAllSongs(PageRequest.of(page - 1, size, Sort.by(sortBy)));
        model.addAttribute("songs", songs);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("page", page);
        return ALL_SONGS;
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable("id") Long songId, Model model) {
        ArtistSongDTO artistSongDTO = songService.geSongDTOById(songId);
        model.addAttribute("song", artistSongDTO);
        return VIEW_SONGS;
    }


    @GetMapping("/{id}/delete")
    public String deleteSong(@PathVariable Long id) {
        songService.deleteSongById(id);
        return REDIRECT + ALL_SONGS;
    }
}
