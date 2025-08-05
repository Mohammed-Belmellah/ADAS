package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.EmotionRecordDTO;
import com.example.ADAS_App.Mappers.EmotionRecordMapper;
import com.example.ADAS_App.entity.EmotionRecord;
import com.example.ADAS_App.entity.Session;
import com.example.ADAS_App.repository.EmotionRecordRepository;
import com.example.ADAS_App.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmotionRecordServiceImpl implements IEmotionRecordService {

    private final EmotionRecordRepository emotionRecordRepo;
    private final SessionRepository sessionRepo;

    @Override
    public EmotionRecordDTO createEmotionRecord(EmotionRecordDTO dto) {
        Session session = sessionRepo.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + dto.getSessionId()));

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
}
