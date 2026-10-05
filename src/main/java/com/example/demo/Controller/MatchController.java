package com.example.demo.Controller;

import com.example.demo.Model.Match;
import com.example.demo.Repository.MatchRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matchs")
public class MatchController {

    private final MatchRepository matchRepository;

    public MatchController(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @GetMapping
    public List<Match> getAll() {
        return matchRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getById(@PathVariable Long id) {
        return matchRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Match create(@RequestBody Match match) {
        return matchRepository.save(match);
    }

    // PUT : remplacement complet
    @PutMapping("/{id}")
    public ResponseEntity<Match> update(@PathVariable Long id, @RequestBody Match updated) {
        return matchRepository.findById(id)
                .map(match -> {
                    match.setDateMatch(updated.getDateMatch());
                    match.setAdversaire(updated.getAdversaire());
                    match.setLieu(updated.getLieu());
                    match.setScoreEquipe(updated.getScoreEquipe());
                    match.setScoreAdversaire(updated.getScoreAdversaire());
                    return ResponseEntity.ok(matchRepository.save(match));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Match> patch(@PathVariable Long id, @RequestBody Match partial) {
        return matchRepository.findById(id)
                .map(match -> {
                    if (partial.getDateMatch() != null) match.setDateMatch(partial.getDateMatch());
                    if (partial.getAdversaire() != null) match.setAdversaire(partial.getAdversaire());
                    if (partial.getLieu() != null) match.setLieu(partial.getLieu());
                    if (partial.getScoreEquipe() != null) match.setScoreEquipe(partial.getScoreEquipe());
                    if (partial.getScoreAdversaire() != null) match.setScoreAdversaire(partial.getScoreAdversaire());
                    return ResponseEntity.ok(matchRepository.save(match));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!matchRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        matchRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}