package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.EmotionRecordBatchDTO;
import com.example.ADAS_App.util.EmotionUtils;
import com.example.ADAS_App.DTOs.EmotionRecordDTO;
import com.example.ADAS_App.Mappers.EmotionRecordMapper;
import com.example.ADAS_App.entity.EmotionRecord;
import com.example.ADAS_App.entity.Session;
import com.example.ADAS_App.repository.EmotionRecordRepository;
import com.example.ADAS_App.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmotionRecordServiceImpl implements IEmotionRecordService {

    private final EmotionRecordRepository emotionRecordRepo;
    private final SessionRepository sessionRepo;
    private final AlertEngine alertEngine;


    @Override
    public EmotionRecordDTO createEmotionRecord(EmotionRecordDTO dto) {
        Session session = sessionRepo.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + dto.getSessionId()));

        dto.setEmotions(EmotionUtils.normalizeLabels(dto.getEmotions()));

        EmotionRecord emotionRecord = EmotionRecordMapper.toEntity(dto, session);
        EmotionRecord saved = emotionRecordRepo.save(emotionRecord);
        return EmotionRecordMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmotionRecordDTO> getEmotionRecordsBySession(UUID sessionId) {
        return emotionRecordRepo.findBySessionId(sessionId)
                .stream()
                .map(EmotionRecordMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmotionRecordDTO> getAllEmotionRecords() {
        return emotionRecordRepo.findAll()
                .stream()
                .map(EmotionRecordMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEmotionRecord(UUID id) {
        EmotionRecord emotionRecord = emotionRecordRepo.findById(id).orElseThrow(() -> new RuntimeException("emotion not found"));
        emotionRecordRepo.delete(emotionRecord);
    }

    @Override
    public List<EmotionRecordDTO> saveBatch(EmotionRecordBatchDTO batchDTO) {
        Session session = sessionRepo.findById(batchDTO.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<EmotionRecord> records = batchDTO.getRecords().stream()
                .map(dto -> {
                    // normaliser les labels AVANT le mapping
                    dto.setEmotions(EmotionUtils.normalizeLabels(dto.getEmotions()));
                    return EmotionRecordMapper.toEntity(dto, session);
                })
                .toList();

        List<EmotionRecord> saved  = emotionRecordRepo.saveAllAndFlush(records);

        saved.sort(Comparator.comparing(EmotionRecord::getDetectedAt));


        alertEngine.evaluateAndBroadcast(session, saved);

        return records.stream()
                .map(EmotionRecordMapper::toDTO)
                .toList();
    }

}
