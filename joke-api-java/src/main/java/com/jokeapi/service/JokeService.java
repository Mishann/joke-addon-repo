package com.jokeapi.service;

import com.jokeapi.model.Joke;
import com.jokeapi.repository.JokeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class JokeService {

    private final JokeRepository repo;

    @Value("${joke.audio-dir:/data/audio}")
    private String audioDir;

    public JokeService(JokeRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void seedIfEmpty() throws IOException {
        if (repo.count() > 0) return;
        ClassPathResource res = new ClassPathResource("jokes.txt");
        if (!res.exists()) return;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(Joke::new)
                .forEach(repo::save);
        }
    }

    public String getRandomJoke() {
        return repo.findRandom()
            .map(Joke::getText)
            .orElse("No jokes found");
    }

    public List<Joke> getAll() {
        return repo.findAll();
    }

    public void add(String text) {
        repo.save(new Joke(text));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public void clearAll() {
        repo.deleteAll();
    }

    public void importFromFile(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(Joke::new)
                .forEach(repo::save);
        }
    }

    public void clearAudioDir() throws IOException {
        Path dir = Path.of(audioDir);
        if (!Files.exists(dir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) Files.deleteIfExists(file);
        }
    }

    // Replicates gTTS behaviour using Google Translate's TTS endpoint
    public String generateAudio(String text) throws IOException, InterruptedException {
        Files.createDirectories(Path.of(audioDir));
        String fileId = UUID.randomUUID().toString();
        Path filePath = Path.of(audioDir, fileId + ".mp3");

        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = "https://translate.google.com/translate_tts?ie=UTF-8&q="
                     + encoded + "&tl=uk&client=tw-ob";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0")
            .GET()
            .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        Files.write(filePath, response.body());

        return fileId + ".mp3";
    }

    public Path getAudioPath(String filename) {
        return Path.of(audioDir, filename);
    }
}
