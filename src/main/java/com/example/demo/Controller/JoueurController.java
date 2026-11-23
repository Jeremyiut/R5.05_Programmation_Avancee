package com.example.demo.Controller;

import com.example.demo.Model.Joueur;
import com.example.demo.Repository.CommentaireRepository;
import com.example.demo.Repository.JoueurRepository;
import com.example.demo.Repository.SelectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/joueurs")
public class JoueurController {

    private final JoueurRepository joueurRepository;
    private final SelectionRepository selectionRepository;
    private final CommentaireRepository commentaireRepository;


    public JoueurController(JoueurRepository joueurRepository, SelectionRepository selectionRepository, CommentaireRepository commentaireRepository) {
        this.joueurRepository = joueurRepository;
        this.selectionRepository = selectionRepository;
        this.commentaireRepository = commentaireRepository;
    }

    @GetMapping
    public List<Joueur> getAll() {
        return joueurRepository.findAll();
    }

    @GetMapping("/{id}")
    public Joueur getById(@PathVariable Long id) {
        return trouver(id);
    }

    @PostMapping
    public Joueur create(@RequestBody Joueur joueur) {
        return joueurRepository.save(joueur);
    }

    // PUT : remplacement complet
    @PutMapping("/{id}")
    public Joueur update(@PathVariable Long id, @RequestBody Joueur updated) {
        Joueur joueur = trouver(id);
        joueur.setNom(updated.getNom());
        joueur.setPrenom(updated.getPrenom());
        joueur.setPoste(updated.getPoste());
        joueur.setNumero(updated.getNumero());
        joueur.setActif(updated.getActif());
        return joueurRepository.save(joueur);
    }

    // PATCH : modification partielle
    @PatchMapping("/{id}")
    public Joueur patch(@PathVariable Long id, @RequestBody Joueur partial) {
        Joueur joueur = trouver(id);
        if (partial.getNom() != null) joueur.setNom(partial.getNom());
        if (partial.getPrenom() != null) joueur.setPrenom(partial.getPrenom());
        if (partial.getPoste() != null) joueur.setPoste(partial.getPoste());
        if (partial.getNumero() != null) joueur.setNumero(partial.getNumero());
        if (partial.getActif() != null) joueur.setActif(partial.getActif());
        return joueurRepository.save(joueur);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Joueur joueur = trouver(id);
        if (selectionRepository.existsByJoueurId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Impossible de supprimer ce joueur : il a déjà participé à un match");
        }
        // Suppression de tous les commentaires assignés au joueur
        commentaireRepository.deleteAll(commentaireRepository.findByJoueurIdOrderByIdDesc(id));
        joueurRepository.delete(joueur);
    }

    private Joueur trouver(Long id) {
        return joueurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur introuvable"));
    }
}