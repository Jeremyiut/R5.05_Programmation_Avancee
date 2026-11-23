package com.example.demo.Repository;

import com.example.demo.Model.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    List<Commentaire> findByJoueurIdOrderByIdDesc(Long joueurId);
    Optional<Commentaire> findFirstByJoueurIdOrderByIdDesc(Long joueurId);
}