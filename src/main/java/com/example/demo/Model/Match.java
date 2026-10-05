package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matchs")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateMatch;
    private String adversaire;
    private String lieu;
    private Integer scoreEquipe;
    private Integer scoreAdversaire;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDateMatch() { return dateMatch; }
    public void setDateMatch(LocalDateTime dateMatch) { this.dateMatch = dateMatch; }
    public String getAdversaire() { return adversaire; }
    public void setAdversaire(String adversaire) { this.adversaire = adversaire; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public Integer getScoreEquipe() { return scoreEquipe; }
    public void setScoreEquipe(Integer scoreEquipe) { this.scoreEquipe = scoreEquipe; }
    public Integer getScoreAdversaire() { return scoreAdversaire; }
    public void setScoreAdversaire(Integer scoreAdversaire) { this.scoreAdversaire = scoreAdversaire; }
}