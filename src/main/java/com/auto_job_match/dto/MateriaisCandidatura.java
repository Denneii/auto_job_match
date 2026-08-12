package com.auto_job_match.dto;

import java.util.List;

public record MateriaisCandidatura(
    String coverLetter,
    String resumoProfissional,
    List<ExperienciaCustomizada> experiencias,
    List<String> formacoesAcad,    // Ex: "IFS - Análise de Sistemas (2024-2027)"
    String competenciasLatex       // A IA vai formatar as habilidades para o LaTeX
) {}