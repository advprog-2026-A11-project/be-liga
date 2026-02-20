package id.ac.ui.cs.advprog.liga.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.service.ClanService;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClanController.class)
class ClanControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClanService clanService;

  @Test
  void testListClansReturnsJsonArray() throws Exception {
    Clan clan = new Clan();
    clan.setClanName("Alpha Clan");

    when(clanService.findAll()).thenReturn(Arrays.asList(clan));

    mockMvc.perform(get("/api/clan/list"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].clanName").value("Alpha Clan"));
  }

  @Test
  void testCreateClanReturnsCreatedObject() throws Exception {
    Clan clan = new Clan();
    clan.setClanName("New Clan");

    when(clanService.create(any(Clan.class))).thenReturn(clan);

    String clanJson = "{\"clanName\":\"New Clan\"}";

    mockMvc.perform(post("/api/clan/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(clanJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clanName").value("New Clan"));
  }

  @Test
  void testGetClanDetail() throws Exception {
    Clan clan = new Clan();
    clan.setClanName("Detail Clan");
    String id = clan.getClanId();

    when(clanService.findById(id)).thenReturn(clan);

    mockMvc.perform(get("/api/clan/detail/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clanName").value("Detail Clan"));
  }

  @Test
  void testDeleteClan() throws Exception {
    mockMvc.perform(delete("/api/clan/delete/some-id"))
            .andExpect(status().isOk());
  }

  @Test
  void testAddMember() throws Exception {
    mockMvc.perform(post("/api/clan/some-id/add-member")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("100"))
            .andExpect(status().isOk());
  }
}