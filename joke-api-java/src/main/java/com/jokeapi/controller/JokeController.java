package com.jokeapi.controller;

import com.jokeapi.model.Joke;
import com.jokeapi.service.JokeService;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class JokeController {

    private final JokeService service;

    public JokeController(JokeService service) {
        this.service = service;
    }

    @GetMapping("/joke")
    public Map<String, String> getJoke() {
        return Map.of("joke", service.getRandomJoke());
    }

    @GetMapping("/joke/audio")
    public Map<String, String> getJokeAudio() throws IOException, InterruptedException {
        service.clearAudioDir();
        String joke = service.getRandomJoke();
        String filename = service.generateAudio(joke);
        return Map.of("url", "api/audio/" + filename);
    }

    @GetMapping("/audio/{filename}")
    public ResponseEntity<Resource> getAudio(@PathVariable String filename) {
        Path path = service.getAudioPath(filename);
        Resource resource = new PathResource(path);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mpeg"))
            .body(resource);
    }

    @GetMapping("/all")
    public List<Joke> getAll() {
        return service.getAll();
    }

    @PostMapping(value = "/joke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, String> addJoke(@RequestParam String text) {
        service.add(text);
        return Map.of("status", "ok");
    }

    @DeleteMapping("/joke/{id}")
    public Map<String, String> deleteJoke(@PathVariable Long id) {
        service.delete(id);
        return Map.of("status", "deleted");
    }

    @DeleteMapping("/clear")
    public Map<String, String> clearAll() {
        service.clearAll();
        return Map.of("status", "cleared");
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> importJokes(@RequestParam("file") MultipartFile file) throws IOException {
        service.importFromFile(file);
        return Map.of("status", "imported");
    }
}
