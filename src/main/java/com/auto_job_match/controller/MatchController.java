package com.auto_job_match.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.auto_job_match.dto.VagaRequest;
import com.auto_job_match.model.VagaAnalisada;
import com.auto_job_match.service.MatchService;
import com.auto_job_match.service.VagaBuscadorService;


@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;
    private final VagaBuscadorService buscadorService;

    public MatchController(MatchService matchService, VagaBuscadorService buscadorService) {
        this.matchService = matchService;
        this.buscadorService = buscadorService;
    }

    @PostMapping
    public VagaAnalisada calcularMatch(@RequestBody VagaRequest vaga) {
        return matchService.analisarCompatibilidade(vaga);
    }

    @GetMapping
    public List<VagaAnalisada> listarHistorico() {
        return matchService.listarHistorico();
    }

    @PostMapping("/buscar-web")
    public String acionarBuscaWeb() {
        buscadorService.buscarEAnalisarVagasDaInternet();
        return "Busca finalizada! Acesse o GET /api/match para ver os resultados salvos no banco.";
    }
}