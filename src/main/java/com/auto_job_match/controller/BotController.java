package com.auto_job_match.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auto_job_match.service.AutomacaoNavegadorService;

@RestController
@RequestMapping("/api/bot")
public class BotController {

    private final AutomacaoNavegadorService automacaoService;

    public BotController(AutomacaoNavegadorService automacaoService) {
        this.automacaoService = automacaoService;
    }

    // 1. Alterado de @GetMapping para @PostMapping
    // 2. Adicionado o @RequestParam para receber o dado do Frontend sem quebrar
    @PostMapping("/buscar")
    public String buscarVaga(@RequestParam String palavraChave) {
        // Agora você envia a palavra recebida da requisição direto para o bot!
        return automacaoService.buscarVagaLinkedIn(palavraChave);
    }
}