package com.auto_job_match.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.auto_job_match.dto.AnaliseMatch;
import com.auto_job_match.dto.ExperienciaCustomizada;
import com.auto_job_match.dto.MateriaisCandidatura;
import com.auto_job_match.dto.VagaRequest;
import com.auto_job_match.repository.PerfilRepository;
import com.auto_job_match.repository.VagaAnalisadaRepository;
import com.auto_job_match.model.Perfil;
import com.auto_job_match.model.VagaAnalisada;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class MatchService {

    private final ChatClient chatClient;
    private final PerfilRepository perfilRepository;
    private final VagaAnalisadaRepository vagaRepository;

    public MatchService(ChatClient.Builder chatClientBuilder, 
                        PerfilRepository perfilRepository,
                        VagaAnalisadaRepository vagaRepository) {
        this.chatClient = chatClientBuilder.build();
        this.perfilRepository = perfilRepository;
        this.vagaRepository = vagaRepository;
    }

    public VagaAnalisada analisarCompatibilidade(VagaRequest vaga) {

        Perfil meuPerfil = (Perfil) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // ==========================================
        // ETAPA 1: ANÁLISE DE COMPATIBILIDADE (MATCH)
        // ==========================================
        String promptAnalise = """
            Você é um especialista em recrutamento de tecnologia.

            Analise a compatibilidade entre o candidato e a vaga abaixo.

            =========================
            PERFIL DO CANDIDATO
            =========================
            Nome: %s
            Senioridade: %s
            Resumo: %s
            LinkedIn: %s
            Currículo: %s

            =========================
            VAGA
            =========================
            Título: %s
            Empresa: %s
            Descrição completa: %s

            =========================
            INSTRUÇÕES
            =========================
            Retorne uma análise objetiva contendo:
            - porcentagemMatch: percentual de compatibilidade de 0 a 100
            - justificativa: explique o motivo do percentual
            - valeApenaAplicar: informe se o candidato deve se candidatar
            - requisitosAtendidos: lista dos requisitos que o candidato possui
            - habilidadesFaltantes: lista das habilidades que faltam

            Seja criterioso. Não invente experiências que não existem no currículo.
            """.formatted(
                meuPerfil.getNome(),
                meuPerfil.getSenioridade(),
                meuPerfil.getResumo(),
                meuPerfil.getPerfilLinkedin(),
                meuPerfil.getCurriculo(),
                vaga.titulo(),
                vaga.empresa(),
                vaga.descricaoCompleta()
            );

        AnaliseMatch analiseDaIA = chatClient.prompt()
            .user(promptAnalise)
            .call()
            .entity(AnaliseMatch.class);

        VagaAnalisada vagaSalva = new VagaAnalisada();
        vagaSalva.setTitulo(vaga.titulo());
        vagaSalva.setEmpresa(vaga.empresa());
        vagaSalva.setDescricaoCompleta(vaga.descricaoCompleta());
        vagaSalva.setPorcentagemMatch(analiseDaIA.porcentagemMatch());
        vagaSalva.setJustificativa(analiseDaIA.justificativa());
        vagaSalva.setValeApenaAplicar(analiseDaIA.valeApenaAplicar());
        vagaSalva.setRequisitosAtendidos(analiseDaIA.requisitosAtendidos());
        vagaSalva.setHabilidadesFaltantes(analiseDaIA.habilidadesFaltantes());

        // ==========================================
        // ETAPA 2 (FASE 4): GERAÇÃO DE CV E COVER LETTER
        // ==========================================
        if (analiseDaIA.porcentagemMatch() > 70) {
            vagaSalva.setLinkVaga(vaga.linkVaga()); 
            
            // 1. Pega os textos genéricos da IA
            MateriaisCandidatura materiais = gerarMateriaisCandidatura(meuPerfil, vaga);
            
            // 2. Constrói os blocos de experiência em LaTeX dinamicamente
            StringBuilder blocosExperienciaLatex = new StringBuilder();
            for (ExperienciaCustomizada exp : materiais.experiencias()) {
                blocosExperienciaLatex.append(String.format("\\entry{%s}{%s}{%s}{%s}\n", 
                        exp.empresa(), exp.periodo(), exp.cargo(), exp.local()));
                blocosExperienciaLatex.append("\\vspace{-1.2em}\n\\begin{itemize}\n\\setlength\\itemsep{-0.3em}\n");
                
                for (String bullet : exp.bulletPoints()) {
                    blocosExperienciaLatex.append("    \\item ").append(bullet).append("\n");
                }
                blocosExperienciaLatex.append("\\end{itemize}\n\n");
            }

            // 3. Substitui no Template Base (Variáveis genéricas)
            String curriculoFinal = TEMPLATE_LATEX_BASE
                .replace("{{NOME_USUARIO}}", meuPerfil.getNome().toUpperCase())
                .replace("{{TELEFONE}}", meuPerfil.getTelefone() != null ? meuPerfil.getTelefone() : "")
                .replace("{{EMAIL}}", meuPerfil.getEmail() != null ? meuPerfil.getEmail() : "")
                .replace("{{LINKEDIN}}", meuPerfil.getPerfilLinkedin() != null ? meuPerfil.getPerfilLinkedin() : "")
                .replace("{{GITHUB}}", meuPerfil.getGithub() != null ? meuPerfil.getGithub() : "")
                .replace("{{RESUMO_PROFISSIONAL}}", materiais.resumoProfissional())
                .replace("{{BLOCO_EXPERIENCIAS}}", blocosExperienciaLatex.toString());
            
            vagaSalva.setCurriculoGerado(curriculoFinal);
            vagaSalva.setCoverLetterGerada(materiais.coverLetter());
        }

        return vagaRepository.save(vagaSalva);
    }

    public List<VagaAnalisada> listarHistorico() {
        return vagaRepository.findAll();
    }
    
    // Método auxiliar para a Fase 4 (Geração Estruturada)
    private MateriaisCandidatura gerarMateriaisCandidatura(Perfil perfil, VagaRequest vaga) {
        String promptGeracao = """
            Você é um Especialista em Carreira e Copywriting. O candidato deu "match" nesta vaga. 

            =========================
            DADOS DO CANDIDATO
            =========================
            Nome: %s
            Resumo Original: %s
            Histórico Profissional Completo: %s

            =========================
            DADOS DA VAGA
            =========================
            Título: %s
            Empresa: %s
            Descrição: %s

            =========================
            INSTRUÇÕES E SAÍDA (TUDO EM PORTUGUÊS DO BRASIL)
            =========================
            Retorne um objeto estruturado contendo exatos 3 campos. 
            
            ATENÇÃO: NÃO SEJA PREGUIÇOSO! NÃO COPIE O TEXTO ORIGINAL!
            
            1. coverLetter: Carta de apresentação persuasiva e vendedora.
            2. resumoProfissional: Reescreva completamente o resumo. Se a vaga pede Java, Spring Boot e APIs (exemplo), destaque logo na primeira linha que o candidato domina isso. Use as exatas palavras-chave da descrição da vaga.
            3. experiencias: Uma lista de objetos. VOCÊ DEVE OBRIGATORIAMENTE DEVOLVER AS 3 EXPERIÊNCIAS (Zênit, EducAZ e SECOM). 
               - Para Zênit e EducAZ: REESCREVA os "bulletPoints". Mude o foco das frases para dar destaque absoluto às tecnologias e entregas que têm relação direta com o que a empresa da vaga está pedindo. Seja vendedor, mas não invente habilidades que não estão no currículo original.
               - Para a SECOM: mantenha o texto original. 
               - Mantenha os nomes de empresa, cargo, período e local idênticos aos originais.
            """.formatted(
                perfil.getNome(),
                perfil.getResumo(),
                perfil.getCurriculo(),
                vaga.titulo(),
                vaga.empresa(),
                vaga.descricaoCompleta()
            );

        return chatClient.prompt()
                .user(promptGeracao)
                .call()
                .entity(MateriaisCandidatura.class);
    }

    private static final String TEMPLATE_LATEX_BASE = """
        \\documentclass[a4paper,10pt]{article}
        \\usepackage[utf8]{inputenc}
        \\usepackage[T1]{fontenc}
        \\usepackage[margin=0.5in,nofoot]{geometry}
        \\usepackage{hyperref}
        \\usepackage{titlesec}
        \\usepackage{xcolor}

        \\hypersetup{
            colorlinks=true,
            linkcolor=blue,
            filecolor=blue,
            urlcolor=blue,
            citecolor=blue
        }

        \\titleformat{\\section}{\\large\\bfseries}{\\thesection}{1em}{}[\\titlerule]
        \\titlespacing*{\\section}{0pt}{*1}{*1}

        \\newcommand{\\entry}[4]{
            \\noindent\\textbf{#1} \\hfill #2 \\\\
            \\noindent\\textit{#3} \\hfill \\textit{#4} \\\\
            \\vspace{2pt}
        }

        \\begin{document}

        \\pagenumbering{gobble}

        \\noindent
        \\begin{minipage}[t]{0.5\\textwidth}
        \\textbf{\\Large {{NOME_USUARIO}}}
        
        \\vspace{0.4em}
        \\noindent Aracaju, Sergipe, Brasil
        \\end{minipage}%%
        \\begin{minipage}[t]{0.5\\textwidth}
        \\raggedleft
        Telefone: {{TELEFONE}} \\\\
        \\href{mailto:{{EMAIL}}}{{{EMAIL}}}
        
        \\vspace{0.2em}
        \\href{{{LINKEDIN}}}{LinkedIn} \\\\
        \\href{{{GITHUB}}}{GitHub}
        \\end{minipage}

        \\vspace{0.5em}

        \\section*{Resumo Profissional}
        \\vspace{0.4em}
        \\noindent {{RESUMO_PROFISSIONAL}}

        \\vspace{0.5em}

        \\section*{Experiência Profissional}
        \\vspace{0.6em}

        {{BLOCO_EXPERIENCIAS}}

        \\section*{Formação Acadêmica}
        \\vspace{0.6em}
        \\entry{IFS - Instituto Federal de Sergipe}{2024 -- 2027}{Curso Superior de Tecnologia (CST) em Análise e Desenvolvimento de Sistemas (5º período)}{Brasil}

        \\section*{Competências Técnicas}
        \\vspace{0.6em}
        \\noindent \\textbf{Linguagens:} Java, JavaScript, PHP, SQL, HTML, CSS \\\\
        \\noindent \\textbf{Frameworks \\& Tecnologias:} Spring Boot, React, Laravel, Livewire, Node.js, Docker \\\\
        \\noindent \\textbf{Backend \\& Infraestrutura:} APIs REST, Integração entre Sistemas, Modelagem de Dados, MySQL, Apache \\\\
        \\noindent \\textbf{Ferramentas:} Git, GitHub \\\\
        \\noindent \\textbf{Metodologias:} Ágeis (Scrum/Kanban)

        \\section*{Habilidades Interpessoais}
        \\vspace{0.6em}
        \\noindent Trabalho em equipe, Resolução de problemas, Proatividade, Aprendizado rápido.

        \\end{document}
        """;
}