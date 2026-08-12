package com.auto_job_match.dto;

import java.util.List;

// 1. DTO para a IA devolver cada experiência reescrita
public record ExperienciaCustomizada(
    String empresa,
    String cargo,
    String periodo,
    String local,
    List<String> bulletPoints // Os pontos reescritos pela IA
) {}