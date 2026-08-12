package com.auto_job_match.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.auto_job_match.dto.RemotiveJob;
import com.auto_job_match.dto.RemotiveResponse;
import com.auto_job_match.dto.VagaRequest;

@Service
public class VagaBuscadorService {

    private final MatchService matchService;
    private final RestTemplate restTemplate; // Cliente HTTP nativo do Spring

    public VagaBuscadorService(MatchService matchService) {
        this.matchService = matchService;
        this.restTemplate = new RestTemplate();
    }

    public void buscarEAnalisarVagasDaInternet() {
        // URL da API gratuita filtrando por desenvolvimento de software e pegando apenas 3 vagas para não sobrecarregar o Ollama
        String url = "https://remotive.com/api/remote-jobs?category=software-dev&limit=3";
        
        System.out.println("Buscando vagas na internet...");
        
        // Faz a requisição e converte o JSON para as nossas classes
        RemotiveResponse resposta = restTemplate.getForObject(url, RemotiveResponse.class);
        
        if (resposta != null && resposta.jobs() != null) {
            for (RemotiveJob jobExterno : resposta.jobs()) {
                System.out.println("Analisando vaga: " + jobExterno.title());
                
                // Converte a vaga da internet para o nosso DTO VagaRequest
                VagaRequest vagaParaIA = new VagaRequest(
                    jobExterno.title(),
                    jobExterno.company_name(),
                    jobExterno.description(), // A API manda um HTML gigante, o Ollama sabe ler!
                    jobExterno.vagaurl() // Link direto para a vaga
                );
                
                // Manda para a IA! Ela vai calcular o match e salvar no banco automaticamente
                matchService.analisarCompatibilidade(vagaParaIA);
            }
        }
        System.out.println("Busca e análise finalizadas!");
    }
}
