package com.challenge.caleta.repository;

import com.challenge.caleta.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}

