package com.example.project.controller;

import com.example.project.exception.CustomException;
import com.example.project.exception.ForbiddenException;
import com.example.project.model.Artist;
import com.example.project.model.Consult;
import com.example.project.model.Deals;
import com.example.project.model.Manager;
import com.example.project.model.dto.SelectedDeals;
import com.example.project.service.ArtistServiceImpl;
import com.example.project.service.ConsultServiceImpl;
import com.example.project.service.DealsServiceImpl;
import com.example.project.service.ManagerServiceImpl;
import com.example.project.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.project.controller.RecordLabelController.BINDING_RESULT_PATH;
import static com.example.project.controller.RecordLabelController.REDIRECT;

@Controller
@RequestMapping("/consults")
@RequiredArgsConstructor
@Slf4j
public class ConsultController {

    private final static String ALL_CONSULTS = "consults";
    private final static String MY_CONSULTS = ALL_CONSULTS + "_my_consults";
    private final static String VIEW_CONSULT = "consult_info";
    private final static String ADD_EDIT_CONSULT = "consult_form";

    private final ConsultServiceImpl consultService;
    private final ManagerServiceImpl managerService;
    private final DealsServiceImpl dealsService;
    private final ArtistServiceImpl artistService;
    private final UserService userService;

    @GetMapping
    public String getAll(Model model,
                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                         @RequestParam(value = "size", defaultValue = "10") Integer size,
                         @RequestParam(value = "sortBy", defaultValue = "id") String sortBy) {
        var consults = consultService.getAllConsults(PageRequest.of(page - 1, size, Sort.by(sortBy)));
        model.addAttribute("consults", consults);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("page", page);
        return ALL_CONSULTS;
    }

    @GetMapping("/my-consults")
    public String getMyConsults(Model model,
                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                @RequestParam(value = "size", defaultValue = "10") Integer size,
                                @RequestParam(value = "sortBy", defaultValue = "id") String sortBy) {
        var myManagerId = userService.getCurrentUser().getManagers().getId();
        var myConsults = consultService.getConsultsByManagerId(PageRequest.of(page - 1, size, Sort.by(sortBy)), myManagerId);
        model.addAttribute("consults", myConsults);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("page", page);
        return MY_CONSULTS;
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable("id") String consultId, Model model) {
        var consult = consultService.getConsultById(Long.valueOf(consultId));
        var selectedDealsIds = consult.getDeals().stream().map(Deals::getId).collect(Collectors.toList());
        var deals = consult.getDeals().stream()
                .sorted(Comparator.comparing(Deals::getSigningDate).thenComparing(Deals::getContractLength))
                .collect(Collectors.toList());
        var manager = consult.getManager();
        var artist = consult.getArtist();

        var managerName = manager.getLastName() + " " + manager.getFirstName();
        var artistName = artist.getLastName() + " " + artist.getFirstName();

        model.addAttribute("consult", consult);
        model.addAttribute("managerName", managerName);
        model.addAttribute("artistName", artistName);
        model.addAttribute("dealsAll", deals);
        model.addAttribute("selectedDealsIds", selectedDealsIds);

        return VIEW_CONSULT;
    }

    @GetMapping("/new")
    public String addConsult(Model model) {
        List<SelectedDeals> selectedDeals;
        var managers = managerService.getAllManagers();
        var artists = artistService.getAllArtists();
        var consult = new Consult();

        if (!model.containsAttribute("consult")) {
            consult.setManager(new Manager());
            consult.setArtist(new Artist());
            consult.setDeals(new ArrayList<>());
            selectedDeals = dealsService.getAllDeals().stream()
                    .map(med -> new SelectedDeals(med, false))
                    .collect(Collectors.toList());
            model.addAttribute("consult", consult);
        } else {
            consult = (Consult) model.getAttribute("consult");
            var containedDealsIds = consult.getDeals() == null ? new ArrayList<Long>() : consult.getDeals().stream().map(Deals::getId).collect(Collectors.toList());
            selectedDeals = dealsService.getAllDeals().stream().map(med -> {
                var isContained = containedDealsIds.contains(med.getId());
                return new SelectedDeals(med, isContained);
            }).collect(Collectors.toList());
            consult.setDeals(dealsService.findDealsByIdContains(containedDealsIds));
        }

        model.addAttribute("selectedDeals", selectedDeals);
        model.addAttribute("managersAll", managers);
        model.addAttribute("artistsAll", artists);

        /* Managers can add consults only on their behalf */
        if (UserService.isLoggedIn() && userService.isManager()) {
            consult.setManager(userService.getCurrentUser().getManagers());
            var managerName = consult.getManager().getLastName() + " " + consult.getManager().getFirstName();
            model.addAttribute("isManager", true);
            model.addAttribute("managerName", managerName);
        } else {
            model.addAttribute("isManager", false);
        }

        return ADD_EDIT_CONSULT;
    }

    @GetMapping("/{id}/edit")
    public String editConsult(@PathVariable("id") String consultId, Model model) {
        if (userService.isManager() && !consultService.isMyConsult(Long.valueOf(consultId))) {
            throw new ForbiddenException();
        }
        var managers = managerService.getAllManagers();
        var artist = artistService.getAllArtists();
        List<SelectedDeals> selectedDeals;
        Consult consult;

        /* First time display, no validation failed before */
        if (!model.containsAttribute("consult")) {
            consult = consultService.getConsultById(Long.valueOf(consultId));
            var containedDealsIds = consult.getDeals() == null ? new ArrayList<Long>() : consult.getDeals().stream().map(Deals::getId).collect(Collectors.toList());
            selectedDeals = dealsService.getAllDeals().stream().map(deal -> {
                var isContained = containedDealsIds.contains(deal.getId());
                return new SelectedDeals(deal, isContained);
            }).collect(Collectors.toList());
            model.addAttribute("consult", consult);
        } else {
            consult = (Consult) model.getAttribute("consult");
            var containedDealsIds = consult.getDeals() == null ? new ArrayList<Long>() : consult.getDeals().stream().map(Deals::getId).collect(Collectors.toList());
            selectedDeals = dealsService.getAllDeals().stream().map(deal -> {
                var isContained = containedDealsIds.contains(deal.getId());
                return new SelectedDeals(deal, isContained);
            }).collect(Collectors.toList());
            consult.setDeals(dealsService.findDealsByIdContains(containedDealsIds));
        }

        model.addAttribute("selectedDeals", selectedDeals);
        model.addAttribute("managersAll", managers);
        model.addAttribute("artistAll", artist);

        /* Managers can edit only their consults */
        if (UserService.isLoggedIn() && userService.isManager()) {
            consult.setManager(userService.getCurrentUser().getManagers());
            var managerName = consult.getManager().getLastName() + " " + consult.getManager().getFirstName();
            model.addAttribute("isManager", true);
            model.addAttribute("managerName", managerName);
        } else {
            model.addAttribute("isManager", false);
        }

        return ADD_EDIT_CONSULT;
    }

    @PostMapping
    public String saveOrUpdateConsult(@ModelAttribute("consult") @Valid Consult consult, BindingResult bindingResult, RedirectAttributes attr) {
        if (bindingResult.hasErrors()) {
            log.info("Model binding has errors!");

            attr.addFlashAttribute(BINDING_RESULT_PATH + "consult", bindingResult);
            attr.addFlashAttribute("consult", consult);

            if (consult.getId() != null) {
                return REDIRECT + ALL_CONSULTS + "/" + consult.getId() + "/edit";
            } else {
                return REDIRECT + ALL_CONSULTS + "/new";
            }
        }

        try {
            consultService.saveConsult(consult);
        } catch (CustomException e) {
            log.info("Error when saving into database! Error message = " + e.getMessage());

            attr.addFlashAttribute(BINDING_RESULT_PATH + "consult", bindingResult);
            attr.addFlashAttribute("consult", consult);
            attr.addFlashAttribute("error_date", e.getMessage());

            if (consult.getId() != null) {
                return REDIRECT + ALL_CONSULTS + "/" + consult.getId() + "/edit";
            } else {
                return REDIRECT + ALL_CONSULTS + "/new";
            }
        }
        return REDIRECT + ALL_CONSULTS;
    }

    @GetMapping("/{id}/delete")
    public String deleteConsult(@PathVariable Long id) {
        consultService.deleteConsultById(id);
        return REDIRECT + ALL_CONSULTS;
    }
}
