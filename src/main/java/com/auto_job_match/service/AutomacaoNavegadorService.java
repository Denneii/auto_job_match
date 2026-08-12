package com.auto_job_match.service;

import com.auto_job_match.dto.VagaRequest;
import com.auto_job_match.model.VagaAnalisada;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Service
public class AutomacaoNavegadorService {

    private static final Logger log = LoggerFactory.getLogger(AutomacaoNavegadorService.class);
    
    // 1. Defina aqui o limite máximo de vagas que o bot deve analisar por execução
    private static final int LIMITE_MAXIMO_VAGAS = 10; 
    
    private final MatchService matchService;
    private static final Random RANDOM = new Random();

    public AutomacaoNavegadorService(MatchService matchService) {
        this.matchService = matchService;
    }

    // 2. Agora o método recebe a palavra-chave que veio do frontend
    public String buscarVagaLinkedIn(String palavraChave) {
        log.info("Iniciando busca no LinkedIn para a vaga: {}", palavraChave);

        // Se a palavra chave for nula ou vazia, define um padrão de segurança
        if (palavraChave == null || palavraChave.trim().isEmpty()) {
            palavraChave = "Desenvolvedor"; 
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setStorageStatePath(Paths.get("linkedin-session.json"))
            );

            Page page = context.newPage();

            // 3. Monta a URL dinâmica convertendo espaços e caracteres especiais (ex: "Desenvolvedor Junior" vira "Desenvolvedor%20Junior")
            String palavraChaveCodificada = URLEncoder.encode(palavraChave, StandardCharsets.UTF_8.toString());
            String url = "https://www.linkedin.com/jobs/search/?keywords=" + palavraChaveCodificada;
            
            page.navigate(url);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            Locator vagasIniciais = page.locator("li[data-occludable-job-id]");
            vagasIniciais.first().waitFor(new Locator.WaitForOptions().setTimeout(15000));

            Set<String> vagasProcessadas = new HashSet<>();
            int processadas = 0;
            int tentativasScrollSemNovasVagas = 0;

            // 4. Nova condição de parada no While: Continua se tiver scroll E se NÃO atingiu o limite
            while (tentativasScrollSemNovasVagas < 3 && processadas < LIMITE_MAXIMO_VAGAS) {
                Locator vagas = page.locator("li[data-occludable-job-id]");
                boolean encontrouNovaNestaPagina = false;

                for (int i = 0; i < vagas.count(); i++) {
                    // 5. Verificação imediata no For: Se bateu a meta, quebra o laço de repetição
                    if (processadas >= LIMITE_MAXIMO_VAGAS) {
                        log.info("Limite de {} vagas alcançado. Parando a busca.", LIMITE_MAXIMO_VAGAS);
                        break; 
                    }

                    Locator vaga = vagas.nth(i);
                    String jobId = vaga.getAttribute("data-occludable-job-id");

                    if (jobId == null || jobId.isBlank() || vagasProcessadas.contains(jobId)) {
                        continue;
                    }

                    encontrouNovaNestaPagina = true;
                    tentativasScrollSemNovasVagas = 0; 

                    try {
                        log.info("\n==============================");
                        log.info("Processando vaga ID: {} ({} de {})", jobId, (processadas + 1), LIMITE_MAXIMO_VAGAS);
                        
                        processarVaga(page, vaga, jobId);
                        
                        vagasProcessadas.add(jobId);
                        processadas++;
                        log.info("Vaga ID {} processada com sucesso. Total: {}", jobId, processadas);

                    } catch (Exception ex) {
                        log.warn("Erro na vaga ID {} - Pulando... Motivo: {}", jobId, ex.getMessage());
                    }
                }

                // Verifica se já não bateu a meta antes de tentar fazer scroll de novo
                if (processadas >= LIMITE_MAXIMO_VAGAS) {
                    break;
                }

                if (!encontrouNovaNestaPagina) {
                    log.info("Nenhuma vaga nova visível, tentando scroll...");
                    scrollLista(page);
                    tentativasScrollSemNovasVagas++;
                }
            }

            return "Sucesso! " + processadas + " vagas únicas analisadas.";

        } catch (Exception e) {
            log.error("Erro fatal durante a busca", e);
            return "Erro: " + e.getMessage();
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void processarVaga(Page page, Locator vaga, String jobId) {
        String titulo = vaga.locator(".job-card-list__title--link").innerText().trim();
        String empresa = vaga.locator(".artdeco-entity-lockup__subtitle").innerText().trim();

        log.info("Título : {}", titulo);
        log.info("Empresa: {}", empresa);

        Locator cardContainer = vaga.locator(".job-card-container");
        cardContainer.scrollIntoViewIfNeeded();
        cardContainer.hover();
        page.waitForTimeout(250);
        
        try {
            cardContainer.click(new Locator.ClickOptions().setTimeout(3000));
        } catch (Exception e) {
            log.warn("Clique suave falhou, forçando clique no card {}", jobId);
            cardContainer.click(new Locator.ClickOptions().setForce(true));
        }

        String descricao = extrairDescricaoComValidacao(page);
        String linkVaga = "https://www.linkedin.com/jobs/view/" + jobId;

        VagaRequest request = new VagaRequest(titulo, empresa, descricao, linkVaga);
        enviarParaIAComRetry(request);
    }

    private String extrairDescricaoComValidacao(Page page) {
        Locator descricaoContainer = page.locator("#job-details").first();
        descricaoContainer.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        
        String texto = descricaoContainer.innerText();
        
        if (texto.length() < 300) {
            log.info("Descrição curta ({} chars). Aguardando estabilização...", texto.length());
            page.waitForTimeout(1500);
            texto = descricaoContainer.innerText();
        }
        
        return texto;
    }

    private void scrollLista(Page page) {
        Locator listaContainer = page.locator(".jobs-search-results-list").first();
        if (listaContainer.count() > 0) {
            listaContainer.evaluate("e => e.scrollBy(0, 1500)");
        }
        page.mouse().wheel(0, 2000);
        page.waitForTimeout(1500); 
    }

    private void enviarParaIAComRetry(VagaRequest request) {
        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            try {
                VagaAnalisada resultado = matchService.analisarCompatibilidade(request);
                
                if (resultado.getPorcentagemMatch() > 70) {
                    salvarCandidaturaPronta(resultado);
                }
                
                break; 
                
            } catch (Exception e) {
                log.error("Erro ao comunicar com a IA (Tentativa {}/3). {}", tentativa, e.getMessage());
                if (tentativa == 3) {
                    log.error("Falha definitiva na análise da IA.");
                    throw new RuntimeException("Falha na IA após 3 tentativas", e);
                } else {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    private void salvarCandidaturaPronta(VagaAnalisada vaga) {
        try {
            Files.createDirectories(Paths.get("candidaturas_prontas"));
            
            String nomeEmpresaLimpo = vaga.getEmpresa().replaceAll("[^a-zA-Z0-9]", "");
            String nomeArquivo = "candidaturas_prontas/" + nomeEmpresaLimpo + ".txt";
            
            String conteudo = String.format(
                    "Vaga: %s\nLINK PARA APLICAÇÃO: %s\n\n" +
                    "=========================================\n" +
                    "COVER LETTER\n" +
                    "=========================================\n%s\n\n" +
                    "=========================================\n" +
                    "CURRÍCULO (CÓDIGO LATEX)\n" +
                    "=========================================\n%s",
                    vaga.getEmpresa(), 
                    vaga.getLinkVaga(),
                    vaga.getCoverLetterGerada(),
                    vaga.getCurriculoGerado()
            );
            
            Files.write(Paths.get(nomeArquivo), conteudo.getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    
            log.info("Material de candidatura salvo com o Link!");
            
        } catch (Exception e) {
            log.error("Erro ao salvar arquivo da vaga {}", vaga.getEmpresa(), e);
        }
    }
}