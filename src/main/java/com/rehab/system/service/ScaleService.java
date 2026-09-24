package com.rehab.system.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rehab.system.common.BusinessException;
import com.rehab.system.dto.ScaleAnswerRequest;
import com.rehab.system.entity.AssessmentItem;
import com.rehab.system.entity.ScaleItem;
import com.rehab.system.entity.ScaleOption;
import com.rehab.system.repository.AssessmentItemRepository;
import com.rehab.system.repository.ScaleItemRepository;
import com.rehab.system.repository.ScaleOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScaleService {

    private final AssessmentItemRepository itemRepository;
    private final ScaleItemRepository scaleItemRepository;
    private final ScaleOptionRepository scaleOptionRepository;
    private final ObjectMapper objectMapper;

    /** 量表详情：量表信息 + 条目（含选项），按 sort 排序 */
    public Map<String, Object> detail(Long scaleId) {
        AssessmentItem scale = itemRepository.findById(scaleId)
                .orElseThrow(() -> new BusinessException("量表不存在"));
        List<ScaleItem> items = scaleItemRepository.findByScale_IdOrderBySortAsc(scaleId);
        List<Map<String, Object>> itemList = new ArrayList<>();
        if (!items.isEmpty()) {
            List<Long> itemIds = items.stream().map(ScaleItem::getId).toList();
            List<ScaleOption> options = scaleOptionRepository.findByItem_IdInOrderBySortAsc(itemIds);
            Map<Long, List<ScaleOption>> optionMap = new LinkedHashMap<>();
            for (ScaleOption option : options) {
                optionMap.computeIfAbsent(option.getItem().getId(), k -> new ArrayList<>()).add(option);
            }
            for (ScaleItem item : items) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", item.getId());
                map.put("itemNo", item.getItemNo());
                map.put("question", item.getQuestion());
                map.put("type", item.getType());
                map.put("options", optionMap.getOrDefault(item.getId(), List.of()));
                itemList.add(map);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scale", scale);
        result.put("items", itemList);
        result.put("gradingRules", parseGradingRules(scale.getGradingRules()));
        return result;
    }

    /** 自动算分：按条目类型汇总得分，并判定功能分级 */
    public Map<String, Object> score(Long scaleId, ScaleAnswerRequest request) {
        AssessmentItem scale = itemRepository.findById(scaleId)
                .orElseThrow(() -> new BusinessException("量表不存在"));
        List<ScaleItem> items = scaleItemRepository.findByScale_IdOrderBySortAsc(scaleId);
        if (items.isEmpty()) {
            throw new BusinessException("该量表尚未配置条目，无法自动算分");
        }
        Map<Long, ScaleAnswerRequest.Answer> answerMap = new LinkedHashMap<>();
        for (ScaleAnswerRequest.Answer answer : request.getAnswers()) {
            answerMap.put(answer.getItemId(), answer);
        }

        double total = 0;
        List<Map<String, Object>> itemResults = new ArrayList<>();
        for (ScaleItem item : items) {
            ScaleAnswerRequest.Answer answer = answerMap.get(item.getId());
            if (answer == null) {
                throw new BusinessException("第 " + item.getItemNo() + " 条「" + item.getQuestion() + "」未作答");
            }
            double itemScore = 0;
            Map<String, Object> itemResult = new LinkedHashMap<>();
            itemResult.put("itemId", item.getId());
            itemResult.put("itemNo", item.getItemNo());
            switch (item.getType()) {
                case ScaleItem.TYPE_SINGLE, ScaleItem.TYPE_MULTI -> {
                    List<Long> optionIds = answer.getOptionIds() == null ? List.of() : answer.getOptionIds();
                    List<ScaleOption> selected = scaleOptionRepository.findAllById(optionIds);
                    if (selected.isEmpty()) {
                        throw new BusinessException("第 " + item.getItemNo() + " 条「" + item.getQuestion() + "」未选择选项");
                    }
                    itemScore = selected.stream().mapToInt(ScaleOption::getScore).sum();
                }
                case ScaleItem.TYPE_NUMBER -> {
                    if (answer.getNumberValue() == null) {
                        throw new BusinessException("第 " + item.getItemNo() + " 条「" + item.getQuestion() + "」未填写数值");
                    }
                    itemScore = answer.getNumberValue();
                }
                case ScaleItem.TYPE_TEXT -> itemScore = 0;
                default -> throw new BusinessException("未知条目类型：" + item.getType());
            }
            total += itemScore;
            itemResult.put("score", itemScore);
            itemResults.add(itemResult);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalScore", total);
        result.put("itemResults", itemResults);
        result.put("grade", computeGrade(scale.getGradingRules(), total));
        return result;
    }

    /** 保存/覆盖量表题库（条目 + 选项），一次保存整体生效 */
    @Transactional
    public void saveItems(Long scaleId, List<ScaleItem> items) {
        AssessmentItem scale = itemRepository.findById(scaleId)
                .orElseThrow(() -> new BusinessException("量表不存在"));
        scaleItemRepository.deleteByScale_Id(scaleId);
        for (ScaleItem item : items) {
            item.setId(null);
            item.setScale(scale);
            if (item.getSort() == null) {
                item.setSort(0);
            }
            scaleItemRepository.save(item);
        }
    }

    private String computeGrade(String gradingRulesJson, double total) {
        List<Map<String, Object>> rules = parseGradingRules(gradingRulesJson);
        for (Map<String, Object> rule : rules) {
            double min = ((Number) rule.get("min")).doubleValue();
            double max = ((Number) rule.get("max")).doubleValue();
            if (total >= min && total <= max) {
                return (String) rule.get("grade");
            }
        }
        return null;
    }

    private List<Map<String, Object>> parseGradingRules(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new BusinessException("量表分级规则格式错误");
        }
    }
}
