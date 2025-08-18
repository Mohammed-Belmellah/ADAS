package com.example.ADAS_App.validators;

import com.example.ADAS_App.DTOs.EmotionRecordDTO;
import org.apache.coyote.BadRequestException;

import java.util.List;

public class EmotionValidators {
    private static void validateDto(EmotionRecordDTO dto) throws BadRequestException {
        if (dto.getDetectedAt() == null) {
            throw new BadRequestException("detectedAt is required");
        }
        if (dto.getEmotions() == null || dto.getConfidences() == null) {
            throw new BadRequestException("emotions and confidences are required");
        }
        if (dto.getEmotions().size() != dto.getConfidences().size()) {
            throw new BadRequestException("emotions and confidences must have same length");
        }
        for (Double c : dto.getConfidences()) {
            if (c == null || c < 0.0 || c > 1.0) {
                throw new BadRequestException("confidence must be in [0,1]");
            }
        }
    }

}
