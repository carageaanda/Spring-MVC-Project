package com.example.project.controller;

import com.example.project.exception.CustomException;
import com.example.project.model.Deals;
import com.example.project.service.DealsServiceImpl;
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
@RequestMapping("/deals")
@RequiredArgsConstructor
@Slf4j
public class DealsController {

    private final static String ALL_DEALS = "deals";
    private final static String VIEW_DEAL = "deal_info";
    private final static String ADD_EDIT_DEAL = "deal_form";

    private final DealsServiceImpl dealsService;

    @GetMapping
    public String getAll(Model model) {
        var deals = dealsService.getAllDeals();
        model.addAttribute("deals", deals);
        return ALL_DEALS;
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable("id") String dealId, Model model) {
        var deal = dealsService.getDealsById(Long.valueOf(dealId));
        model.addAttribute("deal", deal);
        return VIEW_DEAL;
    }

    @GetMapping("/new")
    public String addDeals(Model model) {
        if (!model.containsAttribute("deal")) {
            model.addAttribute("deal", new Deals());
        }

        return ADD_EDIT_DEAL;
    }

    @GetMapping("/{id}/edit")
    public String editDeal(@PathVariable("id") String dealId, Model model) {
        var deal = dealsService.getDealsById(Long.valueOf(dealId));

        if (!model.containsAttribute("deal")) {
            model.addAttribute("deal", deal);
        }

        return ADD_EDIT_DEAL;
    }

    @PostMapping
    public String saveOrUpdate(@ModelAttribute("deal") @Valid Deals deal, BindingResult bindingResult,
                               RedirectAttributes attr) {
        if (bindingResult.hasErrors()) {
            log.info("Model binding has errors!");

            attr.addFlashAttribute(BINDING_RESULT_PATH + "deal", bindingResult);
            attr.addFlashAttribute("deal", deal);
            return ADD_EDIT_DEAL;
        }

        try {
            dealsService.saveDeals(deal);
        } catch (CustomException e) {
            log.info("Error when saving into database! Error message = " + e.getMessage());

            attr.addFlashAttribute(BINDING_RESULT_PATH + "deal", bindingResult);
            attr.addFlashAttribute("deal", deal);
            attr.addFlashAttribute("error_deal", e.getMessage());

            if (deal.getId() == null) {
                log.info(String.format("Redirected back to endpoint %s", ALL_DEALS + "/new"));
                return REDIRECT + ALL_DEALS + "/new";
            } else {
                log.info(String.format("Redirected back to endpoint %s", ALL_DEALS + "/" + deal.getId() + "/edit"));
                return REDIRECT + ALL_DEALS + "/" + deal.getId() + "/edit";
            }
        }

        return REDIRECT + ALL_DEALS;
    }

    @GetMapping("/{id}/delete")
    public String deleteDeal(@PathVariable Long id) {
        dealsService.deleteDealsById(id);
        return REDIRECT + ALL_DEALS;
    }
}
