package id.ac.ui.cs.advprog.liga.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import id.ac.ui.cs.advprog.liga.service.ClanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClanController.class)
class ClanControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClanService clanService;

  @Test
  void testListClansPage() throws Exception {
    mockMvc.perform(get("/clan/list"))
            .andExpect(status().isOk())
            .andExpect(view().name("ClanList"));
  }

  @Test
  void testCreateClanPost() throws Exception {
    mockMvc.perform(post("/clan/create")
                    .param("clanName", "Test Clan"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("list"));
  }
}