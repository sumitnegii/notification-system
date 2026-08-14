package com.indigold.notification_system.controller;

import com.indigold.notification_system.dto.CreateUserResponse;
import com.indigold.notification_system.dto.UserResponse;
import com.indigold.notification_system.exception.GlobalExceptionHandler;
import com.indigold.notification_system.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        UserController.class,
        GlobalExceptionHandler.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(new CreateUserResponse(1L, "User created successfully"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Rahul",
                                  "email": "rahul@example.com",
                                  "phone": "+919888888888",
                                  "pushToken": "test-push-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateUserRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "not-an-email",
                                  "phone": "",
                                  "pushToken": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetUser() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "Rahul",
                "rahul@example.com",
                "+919888888888",
                "test-push-token",
                new UserResponse.PreferencesResponse(true, true, true, true)
        );

        when(userService.getUser(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rahul"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"))
                .andExpect(jsonPath("$.phone").value("+919888888888"))
                .andExpect(jsonPath("$.pushToken").value("test-push-token"))
                .andExpect(jsonPath("$.preferences.email").value(true))
                .andExpect(jsonPath("$.preferences.sms").value(true))
                .andExpect(jsonPath("$.preferences.push").value(true))
                .andExpect(jsonPath("$.preferences.inApp").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.getUser(99L))
                .thenThrow(new IllegalArgumentException("User not found: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }
}
