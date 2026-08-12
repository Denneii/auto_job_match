package com.auto_job_match.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AvaliadorVagaService {

    private final ChatClient chatClient;

    // O Spring injeta o Builder automaticamente graças ao starter
    public AvaliadorVagaService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String avaliarMatch(String perfilCandidato, String descricaoVaga) {
        String prompt = String.format(
            "Você é um recrutador técnico especialista. Avalie o quão bem o seguinte perfil se encaixa na vaga.\n\n" +
            "PERFIL DO CANDIDATO:\n%s\n\n" +
            "DESCRIÇÃO DA VAGA:\n%s\n\n" +
            "Por favor, responda estruturando em: Pontos Fortes, Pontos a Desenvolver e uma Nota Final de Fit (0 a 100).",
            perfilCandidato, descricaoVaga
        );

        // Envia o prompt para o Ollama e retorna a resposta
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}