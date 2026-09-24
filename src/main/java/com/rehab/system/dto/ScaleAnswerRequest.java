package com.rehab.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScaleAnswerRequest {

    private List<Answer> answers;

    @Data
    public static class Answer {

        private Long itemId;

        private List<Long> optionIds;

        private Double numberValue;
    }
}
