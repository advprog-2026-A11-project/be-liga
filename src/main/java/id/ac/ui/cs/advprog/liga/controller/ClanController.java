package id.ac.ui.cs.advprog.liga.controller;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.service.ClanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clan")
public class ClanController {
    @Autowired
    private ClanService service;

    @GetMapping("/list")
    public String listClans(Model model) {
        model.addAttribute("clans", service.findAll());
        return "ClanList";
    }

    @GetMapping("/create")
    public String createClanPage(Model model) {
        model.addAttribute("clan", new Clan());
        return "CreateClan";
    }

    @PostMapping("/create")
    public String createClan(@ModelAttribute Clan clan) {
        service.create(clan);
        return "redirect:list";
    }

    @GetMapping("/detail/{id}")
    public String detailClan(@PathVariable String id, Model model) {
        model.addAttribute("clan", service.findById(id));
        return "ClanDetail";
    }

    // --- Member Management ---

    @GetMapping("/{id}/add-member")
    public String addMemberPage(@PathVariable String id, Model model) {
        model.addAttribute("clanId", id);
        return "AddMember";
    }

    @PostMapping("/{id}/add-member")
    public String addMember(@PathVariable String id, @RequestParam int score) {
        service.addMember(id, score);
        return "redirect:/clan/detail/" + id;
    }

    @GetMapping("/{id}/edit-member/{index}")
    public String editMemberPage(@PathVariable String id, @PathVariable int index, Model model) {
        Clan clan = service.findById(id);
        model.addAttribute("clanId", id);
        model.addAttribute("memberIndex", index);
        model.addAttribute("score", clan.getMemberScores().get(index));
        return "EditMember";
    }

    @PostMapping("/{id}/edit-member/{index}")
    public String editMember(@PathVariable String id, @PathVariable int index, @RequestParam int score) {
        service.editMember(id, index, score);
        return "redirect:/clan/detail/" + id;
    }

    @GetMapping("/{id}/delete-member/{index}")
    public String deleteMember(@PathVariable String id, @PathVariable int index) {
        service.deleteMember(id, index);
        return "redirect:/clan/detail/" + id;
    }
}