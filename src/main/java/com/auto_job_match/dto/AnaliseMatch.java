package com.auto_job_match.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record AnaliseMatch(
    @JsonPropertyDescription("A porcentagem de compatibilidade entre o candidato e a vaga, de 0 a 100.")
    Integer porcentagemMatch,
    
    @JsonPropertyDescription("Uma justificativa de até 3 linhas explicando o porquê dessa porcentagem baseada no currículo.")
    String justificativa,
    
    @JsonPropertyDescription("Lista de tecnologias ou exigências da vaga que o candidato JÁ POSSUI no currículo ou habilidades.")
    List<String> requisitosAtendidos,
    
    @JsonPropertyDescription("Lista de tecnologias ou exigências da vaga que FALTAM no perfil do candidato.")
    List<String> habilidadesFaltantes,
    
    @JsonPropertyDescription("Verdadeiro se a porcentagem de match for maior ou igual a 70.")
    Boolean valeApenaAplicar
) {}