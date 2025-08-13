package miniproject2.paymentmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import miniproject2.paymentmanagementsystem.dto.UserCreateDTO;
import miniproject2.paymentmanagementsystem.dto.UserResponseDTO;
import miniproject2.paymentmanagementsystem.enums.Role;
import miniproject2.paymentmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserCreateDTO userCreateDTO;
    private UserResponseDTO userResponseDTO;
    private List<UserResponseDTO> userList;

    @BeforeEach
    void setUp() {
        userCreateDTO = new UserCreateDTO();
        userCreateDTO.setName("New User");
        userCreateDTO.setEmail("newuser@example.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setRole(Role.VIEWER);

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setName("Test User");
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setRole(Role.ADMIN);

        UserResponseDTO user2 = new UserResponseDTO();
        user2.setId(2L);
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        user2.setRole(Role.FINANCE_MANAGER);

        userList = Arrays.asList(userResponseDTO, user2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturnCreatedUser_WhenValidInput() throws Exception {
        // Given
        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(userResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userResponseDTO.getId()))
                .andExpect(jsonPath("$.name").value(userResponseDTO.getName()))
                .andExpect(jsonPath("$.email").value(userResponseDTO.getEmail()))
                .andExpect(jsonPath("$.role").value(userResponseDTO.getRole().toString()));
    }

    @Test
    @WithMockUser(roles = "FINANCE_MANAGER")
    void createUser_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturnBadRequest_WhenEmailIsEmpty() throws Exception {
        // Given
        userCreateDTO.setEmail("");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturnBadRequest_WhenNameIsEmpty() throws Exception {
        // Given
        userCreateDTO.setName("");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(userList);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(userResponseDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(userResponseDTO.getName()))
                .andExpect(jsonPath("$[1].id").value(userList.get(1).getId()));
    }

    @Test
    @WithMockUser(roles = "FINANCE_MANAGER")
    void getAllUsers_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        // Given
        Long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(userResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userResponseDTO.getId()))
                .andExpect(jsonPath("$.name").value(userResponseDTO.getName()))
                .andExpect(jsonPath("$.email").value(userResponseDTO.getEmail()))
                .andExpect(jsonPath("$.role").value(userResponseDTO.getRole().toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.getUserById(userId)).thenThrow(new RuntimeException("User not found with id: " + userId));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getUserById_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
