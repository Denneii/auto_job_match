package com.auto_job_match.dto;

public record VagaRequest(
    String titulo,
    String empresa,
    String descricaoCompleta,
    String linkVaga
) {}
