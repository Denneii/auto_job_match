package com.auto_job_match.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "perfis")
public class Perfil implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senha;

    private String nome;
    private String senioridade; 

    @Column(columnDefinition = "TEXT")
    private String resumo; 

    private String perfilLinkedin;
    
    // --- NOVOS CAMPOS PARA O CABEÇALHO DO CURRÍCULO ---
    private String email;
    private String telefone;
    private String github;

    @Column(columnDefinition = "TEXT")
    private String curriculo;

    @ElementCollection
    @CollectionTable(name = "perfil_habilidades", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "habilidade")
    private List<String> habilidades;

    public Perfil() {}

    // Getters e Setters Originais
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getSenioridade() { return senioridade; }
    public void setSenioridade(String senioridade) { this.senioridade = senioridade; }
    
    public String getResumo() { return resumo; }
    public void setResumo(String resumo) { this.resumo = resumo; }

    public String getPerfilLinkedin() { return perfilLinkedin; }
    public void setPerfilLinkedin(String perfilLinkedin) { this.perfilLinkedin = perfilLinkedin; }

    public String getCurriculo() { return curriculo; }
    public void setCurriculo(String curriculo) { this.curriculo = curriculo; }

    public List<String> getHabilidades() { return habilidades; }
    public void setHabilidades(List<String> habilidades) { this.habilidades = habilidades; }

    // Getters e Setters Novos
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Todo usuário criado terá o nível básico de "USER"
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email; // O e-mail será o "login" do usuário
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}