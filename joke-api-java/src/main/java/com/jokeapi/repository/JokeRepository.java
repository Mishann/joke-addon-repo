package com.jokeapi.repository;

import com.jokeapi.model.Joke;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JokeRepository extends JpaRepository<Joke, Long> {

    @Query(value = "SELECT * FROM jokes ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Joke> findRandom();
}
