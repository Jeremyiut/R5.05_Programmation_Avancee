package com.example.demo.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "selections", uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "joueur_id"}))
public class Selection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(optional = false)
    @JoinColumn(name = "joueur_id")
    private Joueur joueur;

    private String poste;       // poste occupé pour CE match
    private boolean titulaire;
    private Integer note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    public Joueur getJoueur() { return joueur; }
    public void setJoueur(Joueur joueur) { this.joueur = joueur; }
    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }
    public boolean isTitulaire() { return titulaire; }
    public void setTitulaire(boolean titulaire) { this.titulaire = titulaire; }
    public Integer getNote() { return note; }
    public void setNote(Integer note) { this.note = note; }
}