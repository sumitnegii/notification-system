package com.indigold.notification_system.controller;

import com.indigold.notification_system.exception.GlobalExceptionHandler;
import com.indigold.notification_system.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        NotificationController.class,
        GlobalExceptionHandler.class
})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void shouldReturnOkWhenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification processed successfully"));
    }

    @Test
    void shouldReturnBadRequestWhenUserIdIsNull() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": null,
                                  "title": "Test Notification",
                                  "body": "Hello from test",
                                  "channels": ["EMAIL"]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsEmpty() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "",
                                  "body": "Hello from test",
                                  "channels": ["EMAIL"]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "Test Notification",
                                  "body": "",
                                  "channels": ["EMAIL"]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenChannelsAreEmpty() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "Test Notification",
                                  "body": "Hello from test",
                                  "channels": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        doThrow(new IllegalArgumentException("User not found: 999"))
                .when(notificationService)
                .sendNotification(any());

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 999,
                                  "title": "Test Notification",
                                  "body": "Hello from test",
                                  "channels": ["EMAIL"]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: 999"))
                .andExpect(jsonPath("$.timestamp").value(containsString("T")));
    }

    private String validRequest() {
        return """
                {
                  "userId": 1,
                  "title": "Test Notification",
                  "body": "Hello from test",
                  "channels": ["EMAIL", "PUSH"]
                }
                """;
    }
}
