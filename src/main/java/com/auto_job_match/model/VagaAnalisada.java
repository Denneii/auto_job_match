package com.auto_job_match.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class VagaAnalisada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String empresa;

    @Column(columnDefinition = "TEXT")
    private String descricaoCompleta;

    private Integer porcentagemMatch;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    private Boolean valeApenaAplicar;

    @ElementCollection
    private List<String> requisitosAtendidos;

    @ElementCollection
    private List<String> habilidadesFaltantes;

    // --- NOVOS CAMPOS DA FASE 4 ---

    @Column(columnDefinition = "TEXT")
    private String curriculoGerado;

    @Column(columnDefinition = "TEXT")
    private String coverLetterGerada;

    @Column(columnDefinition = "TEXT")
    private String linkVaga;

}