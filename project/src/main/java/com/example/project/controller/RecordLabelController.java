package com.example.project.controller;

import com.example.project.exception.CustomException;
import com.example.project.model.RecordLabel;
import com.example.project.service.RecordLabelServiceImpl;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@Setter
@RequestMapping("/recordlabels")
@NoArgsConstructor
@Slf4j
public class RecordLabelController {
    public final static String REDIRECT = "redirect:/";
    public final static String BINDING_RESULT_PATH = "org.springframework.validation.BindingResult.";

    public final static String ALL_RECORDLABELS = "recordlabels";
    private final static String VIEW_RECORDLABEL = "recordlabel_info";
    private final static String ADD_EDIT_RECORDLABEL = "recordlabel_form";

    @Autowired
    private RecordLabelServiceImpl recordLabelService;

    @GetMapping(value = {"", "/", "/index"})
    public String getAll(Model model) {
        model.addAttribute("recordLabels", recordLabelService.getAllRecordLabels());
        return ALL_RECORDLABELS;
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable("id") String recordLabelId, Model model) {
        var recordLabel = recordLabelService.getRecordLabelById(Long.valueOf(recordLabelId));
        model.addAttribute("recordLabel", recordLabel);

        return VIEW_RECORDLABEL;
    }

    @GetMapping("/new")
    public String addRecordLabel(Model model) {
        if (!model.containsAttribute("recordLabel")) {
            model.addAttribute("recordLabel", new RecordLabel());
        }
        return ADD_EDIT_RECORDLABEL;
    }

    @GetMapping("/{id}/edit")
    public String editRecordLabel(@PathVariable("id") String recordLabelId, Model model) {
        var recordLabel = recordLabelService.getRecordLabelById(Long.valueOf(recordLabelId));

        if (!model.containsAttribute("recordLabel")) {
            model.addAttribute("recordLabel", recordLabel);
        }

        return ADD_EDIT_RECORDLABEL;
    }

    @PostMapping
    public String saveOrUpdateRecordLabel(@ModelAttribute("recordLabel") @Valid RecordLabel recordLabel, BindingResult bindingResult, RedirectAttributes attr) {
        if (bindingResult.hasErrors()) {
            log.info("Model binding has errors!");

            attr.addFlashAttribute(BINDING_RESULT_PATH + "recordLabel", bindingResult);
            attr.addFlashAttribute("recordLabel", recordLabel);

            if (recordLabel.getId() != null) {
                log.info(String.format("Redirected back to endpoint %s", ALL_RECORDLABELS + "/" + recordLabel.getId() + "/edit"));
                return REDIRECT + ALL_RECORDLABELS + "/" + recordLabel.getId() + "/edit";
            } else {
                log.info(String.format("Redirected back to endpoint %s", ALL_RECORDLABELS + "/new"));
                return REDIRECT + ALL_RECORDLABELS + "/new";
            }
        }

        try {
            recordLabelService.saveRecordLabel(recordLabel);
        } catch (CustomException e) {
            log.info("Error when saving into database! Error message = " + e.getMessage());

            attr.addFlashAttribute(BINDING_RESULT_PATH + "recordLabel", bindingResult);
            attr.addFlashAttribute("recordLabel", recordLabel);
            attr.addFlashAttribute("error_recordLabel", e.getMessage());

            if (recordLabel.getId() == null) {
                log.info(String.format("Redirected back to endpoint %s", ALL_RECORDLABELS + "/new"));
                return REDIRECT + ALL_RECORDLABELS + "/new";
            } else {
                log.info(String.format("Redirected back to endpoint %s", ALL_RECORDLABELS + "/" + recordLabel.getId() + "/edit"));
                return REDIRECT + ALL_RECORDLABELS + "/" + recordLabel.getId() + "/edit";
            }
        }

        return REDIRECT + ALL_RECORDLABELS;
    }

    @GetMapping("/{id}/delete")
    public String deleteRecordLabel(@PathVariable Long id) {
        recordLabelService.deleteRecordLabelById(id);
        return REDIRECT + ALL_RECORDLABELS;
    }
}
