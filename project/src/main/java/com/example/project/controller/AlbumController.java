package com.example.project.controller;

import com.example.project.model.dto.AlbumSongDTO;
import com.example.project.service.AlbumServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import static com.example.project.controller.RecordLabelController.*;

@Controller
@RequestMapping("/albums")
@RequiredArgsConstructor
@Slf4j
public class AlbumController {

    private final static String ALL_ALBUMS = "albums";
    private final static String VIEW_ALBUMS = "album_info";

    private final AlbumServiceImpl albumService;

    @GetMapping
    public String getAll(Model model) {
        var albums = albumService.getAllAlbums();
        model.addAttribute("albums", albums);
        return ALL_ALBUMS;
    }


    @GetMapping("/{id}")
    public String getById(@PathVariable("id") Long albumId, Model model) {
        AlbumSongDTO albumSongDTO = albumService.getAlbumDTOById(albumId);
        model.addAttribute("album", albumSongDTO);
        return VIEW_ALBUMS;
    }


    @GetMapping("/{id}/delete")
    public String deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbumById(id);
        return REDIRECT + ALL_ALBUMS;
    }
}
