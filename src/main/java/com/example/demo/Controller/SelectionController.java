package com.example.demo.Controller;

import com.example.demo.Model.Joueur;
import com.example.demo.Model.Match;
import com.example.demo.Model.Selection;
import com.example.demo.Repository.JoueurRepository;
import com.example.demo.Repository.MatchRepository;
import com.example.demo.Repository.SelectionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/matchs/{matchId}/selections")
public class SelectionController {

    public record SelectionRequest(Long joueurId, String poste, Boolean titulaire) {}
    public record EvaluationRequest(Integer note) {}

    private final SelectionRepository selectionRepository;
    private final MatchRepository matchRepository;
    private final JoueurRepository joueurRepository;

    @Value("${sport.nombre-titulaires}")
    private int nombreTitulairesRequis;

    @Value("${sport.nombre-remplacants-max}")
    private int nombreRemplacantsMax;

    public SelectionController(SelectionRepository selectionRepository,
                                MatchRepository matchRepository,
                                JoueurRepository joueurRepository) {
        this.selectionRepository = selectionRepository;
        this.matchRepository = matchRepository;
        this.joueurRepository = joueurRepository;
    }

    @GetMapping
    public List<Selection> getAll(@PathVariable Long matchId) {
        trouverMatch(matchId);
        return selectionRepository.findByMatchId(matchId);
    }

    @PostMapping
    public Selection ajouter(@PathVariable Long matchId, @RequestBody SelectionRequest request) {
        Match match = trouverMatch(matchId);
        exigerMatchAVenir(match);

        if (request.joueurId() == null || request.poste() == null || request.poste().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "joueurId et poste sont obligatoires");
        }
        Joueur joueur = joueurRepository.findById(request.joueurId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur introuvable"));

        if (!Boolean.TRUE.equals(joueur.getActif())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seuls les joueurs actifs peuvent être sélectionnés");
        }
        if (selectionRepository.existsByMatchIdAndJoueurId(matchId, joueur.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce joueur est déjà sur la feuille de match");
        }

        boolean titulaire = Boolean.TRUE.equals(request.titulaire());
        exigerPlaceDisponible(matchId, titulaire);

        Selection selection = new Selection();
        selection.setMatch(match);
        selection.setJoueur(joueur);
        selection.setPoste(request.poste());
        selection.setTitulaire(titulaire);
        return selectionRepository.save(selection);
    }

    @PutMapping("/{selectionId}")
    public Selection modifier(@PathVariable Long matchId, @PathVariable Long selectionId,
                            @RequestBody SelectionRequest request) {
        Selection selection = trouverSelection(matchId, selectionId);
        exigerMatchAVenir(selection.getMatch());

        if (request.poste() == null || request.poste().isBlank() || request.titulaire() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "poste et titulaire sont obligatoires");
        }
        changerStatut(matchId, selection, request.titulaire());
        selection.setPoste(request.poste());
        return selectionRepository.save(selection);
    }

    @PatchMapping("/{selectionId}")
    public Selection modifierPartiellement(@PathVariable Long matchId, @PathVariable Long selectionId,
                                            @RequestBody SelectionRequest request) {
        Selection selection = trouverSelection(matchId, selectionId);
        exigerMatchAVenir(selection.getMatch());

        if (request.poste() != null) {
            if (request.poste().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le poste ne peut pas être vide");
            }
            selection.setPoste(request.poste());
        }
        if (request.titulaire() != null) {
            changerStatut(matchId, selection, request.titulaire());
        }
        return selectionRepository.save(selection);
    }

    @DeleteMapping("/{selectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirer(@PathVariable Long matchId, @PathVariable Long selectionId) {
        Selection selection = trouverSelection(matchId, selectionId);
        exigerMatchAVenir(selection.getMatch());
        selectionRepository.delete(selection);
    }

    @PutMapping("/{selectionId}/evaluation")
    public Selection evaluer(@PathVariable Long matchId, @PathVariable Long selectionId,
                              @RequestBody EvaluationRequest evaluation) {
        Selection selection = trouverSelection(matchId, selectionId);
        if (!dejaJoue(selection.getMatch())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Impossible d'évaluer un joueur avant que le match ait eu lieu");
        }
        if (evaluation.note() == null || evaluation.note() < 0 || evaluation.note() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note doit être comprise entre 0 et 10");
        }
        selection.setNote(evaluation.note());
        return selectionRepository.save(selection);
    }

    // Méthodes utilitaires

    private Match trouverMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match introuvable"));
    }

    private Selection trouverSelection(Long matchId, Long selectionId) {
        Selection selection = selectionRepository.findById(selectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sélection introuvable"));
        if (!selection.getMatch().getId().equals(matchId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sélection introuvable pour ce match");
        }
        return selection;
    }

    private boolean dejaJoue(Match match) {
        return !match.getDateMatch().isAfter(LocalDateTime.now());
    }

    private void exigerMatchAVenir(Match match) {
        if (dejaJoue(match)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La feuille de match ne peut plus être modifiée : le match a déjà eu lieu");
        }
    }

    // Ne vérifie le quota que si le statut change
    private void changerStatut(Long matchId, Selection selection, boolean titulaire) {
        if (titulaire == selection.isTitulaire()) {
            return;
        }
        exigerPlaceDisponible(matchId, titulaire);
        selection.setTitulaire(titulaire);
    }

    private void exigerPlaceDisponible(Long matchId, boolean titulaire) {
        if (titulaire) {
            if (selectionRepository.countByMatchIdAndTitulaireTrue(matchId) >= nombreTitulairesRequis) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Le nombre de titulaires requis (" + nombreTitulairesRequis + ") est déjà atteint");
            }
        } else {
            if (selectionRepository.countByMatchIdAndTitulaireFalse(matchId) >= nombreRemplacantsMax) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Le nombre maximum de remplaçants (" + nombreRemplacantsMax + ") est déjà atteint");
            }
        }
    }
}