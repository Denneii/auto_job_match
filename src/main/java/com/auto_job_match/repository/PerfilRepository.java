package com.auto_job_match.repository;

import com.auto_job_match.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    UserDetails findByEmail(String email);
}