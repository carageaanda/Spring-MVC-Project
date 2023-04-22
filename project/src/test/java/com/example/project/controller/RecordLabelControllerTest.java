package com.example.project.controller;

import com.example.project.model.RecordLabel;
import com.example.project.service.RecordLabelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.Model;

import static com.example.project.controller.RecordLabelController.ALL_RECORDLABELS;
import static com.example.project.controller.RecordLabelController.REDIRECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test for {@link com.example.project.controller.RecordLabelController}
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("h2")
public class RecordLabelControllerTest {

    @Mock
    Model model;

    @Mock
    RecordLabelServiceImpl recordLabelService;

    @InjectMocks
    RecordLabelController recordLabelController;

    @BeforeEach
    void setUp() {
        recordLabelController = new RecordLabelController();
        recordLabelController.setRecordLabelService(recordLabelService);
    }

    @Test
    public void deleteRecordLabel() {
        doNothing().when(recordLabelService).deleteRecordLabelById(1L);

        // test that the correct view name is displayed
        String view = recordLabelController.deleteRecordLabel(1L);
        assertEquals(REDIRECT + ALL_RECORDLABELS, view);
        ArgumentCaptor<RecordLabel> argumentCaptor = ArgumentCaptor.forClass(RecordLabel.class);
    }

    @Test
    public void getById() {
        RecordLabel recordLabel = new RecordLabel();
        recordLabel.setId(1L);
        recordLabel.setName("Test");
        when(recordLabelService.getRecordLabelById(1L)).thenReturn(recordLabel);

        // test that the correct view name is displayed
        String view = recordLabelController.getById("1", model);
        assertEquals("recordlabel_info", view);
        ArgumentCaptor<RecordLabel> argumentCaptor = ArgumentCaptor.forClass(RecordLabel.class);

        // test that the object was added with the "department" attribute
        verify(model, times(1)).addAttribute(eq("recordLabel"), argumentCaptor.capture());
        RecordLabel recordLabelCaptured = argumentCaptor.getValue();
        assertEquals(1L, recordLabelCaptured.getId());
        assertEquals("Test", recordLabelCaptured.getName());
    }
}
