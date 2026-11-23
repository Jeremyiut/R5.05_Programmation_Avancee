package com.example.demo.Repository;

import com.example.demo.Model.Selection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SelectionRepository extends JpaRepository<Selection, Long> {
    List<Selection> findByMatchId(Long matchId);
    boolean existsByMatchIdAndJoueurId(Long matchId, Long joueurId);
    boolean existsByJoueurId(Long joueurId);
    long countByMatchIdAndTitulaireTrue(Long matchId);
    long countByMatchIdAndTitulaireFalse(Long matchId);
    List<Selection> findByJoueurId(Long joueurId);
}