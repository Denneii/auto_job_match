package com.auto_job_match.controller;

import com.auto_job_match.model.Perfil;
import com.auto_job_match.repository.PerfilRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfis") // Rota base para este controller
@CrossOrigin(origins = "*") // Permite que o frontend React acesse a API sem erros de CORS
public class PerfilController {

    private final PerfilRepository perfilRepository;

    public PerfilController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @PostMapping
    public Perfil salvarPerfil(@RequestBody Perfil perfil) {
        return perfilRepository.save(perfil);
    }

    @GetMapping
    public List<Perfil> listarPerfis() {
        return perfilRepository.findAll();
    }
}