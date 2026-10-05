package com.example.demo.Controller;

import com.example.demo.Model.Match;
import com.example.demo.Repository.MatchRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/matchs")
public class MatchController {

    // Corps attendu pour la saisie du résultat
    public record ResultatRequest(Integer scoreEquipe, Integer scoreAdversaire) {}

    private final MatchRepository matchRepository;

    public MatchController(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @GetMapping
    public List<Match> getAll() {
        return matchRepository.findAll();
    }

    @GetMapping("/{id}")
    public Match getById(@PathVariable Long id) {
        return trouver(id);
    }

    @PostMapping
    public Match create(@RequestBody Match match) {
        if (match.getDateMatch() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date du match est obligatoire");
        }
        // Un match à venir ne peut pas avoir de score
        boolean aUnScore = match.getScoreEquipe() != null || match.getScoreAdversaire() != null;
        if (aUnScore && !dejaJoue(match)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un match à venir ne peut pas avoir de résultat");
        }
        return matchRepository.save(match);
    }

    // PUT : remplacement des infos (les scores éventuels du JSON sont ignorés, cf. /resultat)
    @PutMapping("/{id}")
    public Match update(@PathVariable Long id, @RequestBody Match updated) {
        Match match = trouver(id);
        exigerMatchAVenir(match);
        exigerDateFuture(updated.getDateMatch());

        match.setDateMatch(updated.getDateMatch());
        match.setAdversaire(updated.getAdversaire());
        match.setLieu(updated.getLieu());
        return matchRepository.save(match);
    }

    // PATCH : modification partielle des infos (scores ignorés)
    @PatchMapping("/{id}")
    public Match patch(@PathVariable Long id, @RequestBody Match partial) {
        Match match = trouver(id);
        exigerMatchAVenir(match);

        if (partial.getDateMatch() != null) {
            exigerDateFuture(partial.getDateMatch());
            match.setDateMatch(partial.getDateMatch());
        }
        if (partial.getAdversaire() != null) match.setAdversaire(partial.getAdversaire());
        if (partial.getLieu() != null) match.setLieu(partial.getLieu());
        return matchRepository.save(match);
    }

    // Saisie (ou correction) du résultat d'un match déjà joué
    @PutMapping("/{id}/resultat")
    public Match saisirResultat(@PathVariable Long id, @RequestBody ResultatRequest resultat) {
        Match match = trouver(id);
        if (!dejaJoue(match)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le match n'a pas encore eu lieu : impossible de saisir un résultat");
        }
        if (resultat.scoreEquipe() == null || resultat.scoreAdversaire() == null
                || resultat.scoreEquipe() < 0 || resultat.scoreAdversaire() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Les deux scores sont obligatoires et doivent être positifs ou nuls");
        }
        match.setScoreEquipe(resultat.scoreEquipe());
        match.setScoreAdversaire(resultat.scoreAdversaire());
        return matchRepository.save(match);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Match match = trouver(id);
        exigerMatchAVenir(match);
        matchRepository.delete(match);
    }

    // ---------- Méthodes utilitaires ----------

    private Match trouver(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match introuvable"));
    }

    private boolean dejaJoue(Match match) {
        return !match.getDateMatch().isAfter(LocalDateTime.now());
    }

    private void exigerMatchAVenir(Match match) {
        if (dejaJoue(match)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce match a déjà eu lieu : il ne peut plus être modifié ni supprimé");
        }
    }

    private void exigerDateFuture(LocalDateTime date) {
        if (date == null || !date.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date du match doit être dans le futur");
        }
    }
}