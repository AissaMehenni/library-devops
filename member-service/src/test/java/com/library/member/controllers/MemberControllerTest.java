package com.library.member.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.member.data.Member;
import com.library.member.services.DuplicateEmailException;
import com.library.member.services.MemberNotFoundException;
import com.library.member.services.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member(1L, "Alice", "alice@example.com");
    }

    @Test
    void getAllShouldReturnList() throws Exception {
        given(memberService.findAll()).willReturn(List.of(member));

        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"));
    }

    @Test
    void getByIdShouldReturnMember() throws Exception {
        given(memberService.findById(1L)).willReturn(member);

        mockMvc.perform(get("/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getByIdShouldReturn404WhenMissing() throws Exception {
        given(memberService.findById(99L)).willThrow(new MemberNotFoundException(99L));

        mockMvc.perform(get("/members/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Member not found with id 99"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createShouldReturn201AndPersistedMember() throws Exception {
        Member input = new Member(null, "Bob", "bob@example.com");
        Member saved = new Member(2L, "Bob", "bob@example.com");
        given(memberService.create(any(Member.class))).willReturn(saved);

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    void createShouldReturn400WhenNameIsBlank() throws Exception {
        Member invalid = new Member(null, "", "bob@example.com");

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShouldReturn400WhenEmailIsInvalid() throws Exception {
        Member invalid = new Member(null, "Bob", "not-an-email");

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShouldReturn409WhenEmailDuplicated() throws Exception {
        Member input = new Member(null, "Bob", "bob@example.com");
        given(memberService.create(any(Member.class)))
                .willThrow(new DuplicateEmailException("bob@example.com"));

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Member with email bob@example.com already exists"));
    }

    @Test
    void updateShouldReturnUpdatedMember() throws Exception {
        Member input = new Member(null, "Alice Updated", "alice@example.com");
        Member updated = new Member(1L, "Alice Updated", "alice@example.com");
        given(memberService.update(eq(1L), any(Member.class))).willReturn(updated);

        mockMvc.perform(put("/members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice Updated"));
    }

    @Test
    void updateShouldReturn404WhenMissing() throws Exception {
        Member input = new Member(null, "X", "x@example.com");
        given(memberService.update(eq(99L), any(Member.class)))
                .willThrow(new MemberNotFoundException(99L));

        mockMvc.perform(put("/members/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        mockMvc.perform(delete("/members/1"))
                .andExpect(status().isNoContent());

        verify(memberService).delete(1L);
    }

    @Test
    void deleteShouldReturn404WhenMissing() throws Exception {
        willThrow(new MemberNotFoundException(99L)).given(memberService).delete(99L);

        mockMvc.perform(delete("/members/99"))
                .andExpect(status().isNotFound());
    }
}
