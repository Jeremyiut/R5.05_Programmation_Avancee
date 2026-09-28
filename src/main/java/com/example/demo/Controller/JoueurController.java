package com.example.demo.Controller;

import com.example.demo.Model.Joueur;
import com.example.demo.Repository.JoueurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/joueurs")
public class JoueurController {

    private final JoueurRepository joueurRepository;

    public JoueurController(JoueurRepository joueurRepository) {
        this.joueurRepository = joueurRepository;
    }

    @GetMapping
    public List<Joueur> getAll() {
        return joueurRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Joueur> getById(@PathVariable Long id) {
        return joueurRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Joueur create(@RequestBody Joueur joueur) {
        return joueurRepository.save(joueur);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Joueur> update(@PathVariable Long id, @RequestBody Joueur updated) {
        return joueurRepository.findById(id)
                .map(joueur -> {
                    joueur.setNom(updated.getNom());
                    joueur.setPrenom(updated.getPrenom());
                    joueur.setPoste(updated.getPoste());
                    joueur.setNumero(updated.getNumero());
                    joueur.setActif(updated.getActif());
                    return ResponseEntity.ok(joueurRepository.save(joueur));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Joueur> patch(@PathVariable Long id, @RequestBody Joueur partial) {
        return joueurRepository.findById(id)
                .map(joueur -> {
                    if (partial.getNom() != null) joueur.setNom(partial.getNom());
                    if (partial.getPrenom() != null) joueur.setPrenom(partial.getPrenom());
                    if (partial.getPoste() != null) joueur.setPoste(partial.getPoste());
                    if (partial.getNumero() != null) joueur.setNumero(partial.getNumero());
                    if (partial.getActif() != null) joueur.setActif(partial.getActif());
                    return ResponseEntity.ok(joueurRepository.save(joueur));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!joueurRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        joueurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}