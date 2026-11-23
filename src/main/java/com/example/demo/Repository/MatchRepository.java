package com.example.demo.Repository;

import com.example.demo.Model.Match;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByDateMatchLessThanEqualOrderByDateMatchAsc(LocalDateTime date);
}