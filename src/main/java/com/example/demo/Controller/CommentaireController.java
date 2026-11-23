package com.example.demo.Controller;

import com.example.demo.Model.Commentaire;
import com.example.demo.Model.Joueur;
import com.example.demo.Repository.CommentaireRepository;
import com.example.demo.Repository.JoueurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/joueurs/{joueurId}/commentaires")
public class CommentaireController {

    public record CommentaireRequest(String texte) {}

    private final CommentaireRepository commentaireRepository;
    private final JoueurRepository joueurRepository;

    public CommentaireController(CommentaireRepository commentaireRepository,
                                 JoueurRepository joueurRepository) {
        this.commentaireRepository = commentaireRepository;
        this.joueurRepository = joueurRepository;
    }

    // Historique complet, du plus récent au plus ancien
    @GetMapping
    public List<Commentaire> historique(@PathVariable Long joueurId) {
        trouverJoueur(joueurId);
        return commentaireRepository.findByJoueurIdOrderByIdDesc(joueurId);
    }

    // Le commentaire "actuel" = le dernier ajouté
    @GetMapping("/actuel")
    public Commentaire actuel(@PathVariable Long joueurId) {
        trouverJoueur(joueurId);
        return commentaireRepository.findFirstByJoueurIdOrderByIdDesc(joueurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ce joueur n'a encore aucun commentaire"));
    }

    // Ajoute un nouveau commentaire (les précédents sont conservés dans l'historique)
    @PostMapping
    public Commentaire ajouter(@PathVariable Long joueurId, @RequestBody CommentaireRequest request) {
        Joueur joueur = trouverJoueur(joueurId);

        if (request.texte() == null || request.texte().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le commentaire ne peut pas être vide");
        }
        if (request.texte().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le commentaire ne peut pas dépasser 1000 caractères");
        }

        Commentaire commentaire = new Commentaire();
        commentaire.setJoueur(joueur);
        commentaire.setTexte(request.texte().trim());
        commentaire.setDateCommentaire(LocalDateTime.now());
        return commentaireRepository.save(commentaire);
    }

    private Joueur trouverJoueur(Long joueurId) {
        return joueurRepository.findById(joueurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur introuvable"));
    }
}