package com.example.demo.Controller;

import com.example.demo.Model.Joueur;
import com.example.demo.Model.Match;
import com.example.demo.Model.Selection;
import com.example.demo.Repository.JoueurRepository;
import com.example.demo.Repository.MatchRepository;
import com.example.demo.Repository.SelectionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/statistiques")
public class StatistiqueController {

    public record StatistiquesEquipe(long matchsJoues, long matchsAvecResultat,
                                     long victoires, long nuls, long defaites,
                                     double pourcentageVictoires, double pourcentageNuls,
                                     double pourcentageDefaites) {}

    public record StatistiquesJoueur(Long joueurId, String nom, String prenom, String statut,
                                     String posteprefere, long titularisations, long remplacements,
                                     Double moyenneEvaluations, Double pourcentageVictoires,
                                     int selectionsConsecutives) {}

    private final MatchRepository matchRepository;
    private final SelectionRepository selectionRepository;
    private final JoueurRepository joueurRepository;

    public StatistiqueController(MatchRepository matchRepository,
                                 SelectionRepository selectionRepository,
                                 JoueurRepository joueurRepository) {
        this.matchRepository = matchRepository;
        this.selectionRepository = selectionRepository;
        this.joueurRepository = joueurRepository;
    }

    @GetMapping("/matchs")
    public StatistiquesEquipe statistiquesMatchs() {
        List<Match> matchsJoues = matchRepository
                .findByDateMatchLessThanEqualOrderByDateMatchAsc(LocalDateTime.now());

        long victoires = 0;
        long nuls = 0;
        long defaites = 0;
        for (Match match : matchsJoues) {
            if (match.getScoreEquipe() == null || match.getScoreAdversaire() == null) {
                continue; // match joué mais résultat pas encore saisi : on l'ignore
            }
            if (match.getScoreEquipe() > match.getScoreAdversaire()) {
                victoires++;
            } else if (match.getScoreEquipe() < match.getScoreAdversaire()) {
                defaites++;
            } else {
                nuls++;
            }
        }
        long avecResultat = victoires + nuls + defaites;

        return new StatistiquesEquipe(matchsJoues.size(), avecResultat, victoires, nuls, defaites,
                pourcentage(victoires, avecResultat),
                pourcentage(nuls, avecResultat),
                pourcentage(defaites, avecResultat));
    }

    @GetMapping("/joueurs")
    public List<StatistiquesJoueur> statistiquesJoueurs() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<Match> matchsJoues = matchRepository.findByDateMatchLessThanEqualOrderByDateMatchAsc(maintenant);
        List<StatistiquesJoueur> tableau = new ArrayList<>();

        for (Joueur joueur : joueurRepository.findAll()) {
            long titularisations = 0;
            long remplacements = 0;
            int sommeNotes = 0;
            int nombreNotes = 0;
            long matchsAvecResultat = 0;
            long victoires = 0;

            // On passe en revue toutes les sélections du joueur
            for (Selection selection : selectionRepository.findByJoueurId(joueur.getId())) {
                Match match = selection.getMatch();
                if (match.getDateMatch().isAfter(maintenant)) {
                    continue; // match à venir : rien à compter
                }
                if (selection.isTitulaire()) {
                    titularisations++;
                } else {
                    remplacements++;
                }
                if (selection.getNote() != null) {
                    sommeNotes += selection.getNote();
                    nombreNotes++;
                }
                if (match.getScoreEquipe() != null && match.getScoreAdversaire() != null) {
                    matchsAvecResultat++;
                    if (match.getScoreEquipe() > match.getScoreAdversaire()) {
                        victoires++;
                    }
                }
            }

            // null = "pas de donnée"
            Double moyenne = null;
            if (nombreNotes > 0) {
                moyenne = Math.round(sommeNotes * 100.0 / nombreNotes) / 100.0;
            }
            Double pourcentageVictoires = null;
            if (matchsAvecResultat > 0) {
                pourcentageVictoires = pourcentage(victoires, matchsAvecResultat);
            }

            // Sélections consécutives : on parcourt les matchs joués du plus ancien au plus récent.
            // Absent de la feuille = la série repart à 0. À la fin, "serie" = série en cours.
            int serie = 0;
            for (Match match : matchsJoues) {
                if (selectionRepository.existsByMatchIdAndJoueurId(match.getId(), joueur.getId())) {
                    serie++;
                } else {
                    serie = 0;
                }
            }

            tableau.add(new StatistiquesJoueur(
                    joueur.getId(), joueur.getNom(), joueur.getPrenom(),
                    Boolean.TRUE.equals(joueur.getActif()) ? "Actif" : "Inactif",
                    joueur.getPoste(),
                    titularisations, remplacements, moyenne, pourcentageVictoires, serie));
        }
        return tableau;
    }

    // Pourcentage arrondi à 1 décimale (0.0 si rien à diviser)
    private double pourcentage(long partie, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(partie * 1000.0 / total) / 10.0;
    }
}