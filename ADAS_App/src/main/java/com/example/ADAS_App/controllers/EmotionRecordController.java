package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.EmotionRecordDTO;
import com.example.ADAS_App.service.IEmotionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/emotions")
@RequiredArgsConstructor
public class EmotionRecordController {

    private final IEmotionRecordService emotionRecordService;

    // === Create a new emotion record ===
    @PostMapping
    public ResponseEntity<EmotionRecordDTO> createEmotionRecord(
            @RequestBody EmotionRecordDTO emotionRecordDTO) {
        EmotionRecordDTO created = emotionRecordService.createEmotionRecord(emotionRecordDTO);
        return ResponseEntity.ok(created);
    }

    // === Get all emotion records ===
    @GetMapping
    public ResponseEntity<List<EmotionRecordDTO>> getAllEmotionRecords() {
        return ResponseEntity.ok(emotionRecordService.getAllEmotionRecords());
    }

    // === Get emotion records for a specific session ===
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<EmotionRecordDTO>> getEmotionsBySession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(emotionRecordService.getEmotionRecordsBySession(sessionId));
    }

    // === Delete emotion record ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmotionRecord(@PathVariable UUID id) {
        emotionRecordService.deleteEmotionRecord(id);
        return ResponseEntity.noContent().build();
    }
}
