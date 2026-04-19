package com.jokeapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jokes")
public class Joke {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    public Joke() {}

    public Joke(String text) {
        this.text = text;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
