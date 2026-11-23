package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commentaires")
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "joueur_id")
    private Joueur joueur;

    @Column(nullable = false, length = 1000)
    private String texte;

    private LocalDateTime dateCommentaire;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Joueur getJoueur() { return joueur; }
    public void setJoueur(Joueur joueur) { this.joueur = joueur; }
    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }
    public LocalDateTime getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(LocalDateTime dateCommentaire) { this.dateCommentaire = dateCommentaire; }
}