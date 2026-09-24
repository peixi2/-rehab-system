package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.entity.AssessmentItem;
import com.rehab.system.repository.AssessmentItemRepository;
import com.rehab.system.repository.AssessmentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentItemService {

    private final AssessmentItemRepository itemRepository;
    private final AssessmentRecordRepository recordRepository;

    public List<AssessmentItem> list() {
        return itemRepository.findAll();
    }

    public AssessmentItem get(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评估项目不存在"));
    }

    public AssessmentItem create(AssessmentItem item) {
        checkName(item.getName());
        item.setId(null);
        return itemRepository.save(item);
    }

    public AssessmentItem update(Long id, AssessmentItem item) {
        checkName(item.getName());
        AssessmentItem db = get(id);
        db.setName(item.getName());
        db.setCode(item.getCode());
        db.setVersion(item.getVersion());
        db.setStatus(item.getStatus());
        db.setDescription(item.getDescription());
        db.setInstructions(item.getInstructions());
        db.setGradingRules(item.getGradingRules());
        return itemRepository.save(db);
    }

    public void delete(Long id) {
        get(id);
        if (recordRepository.existsByItemId(id)) {
            throw new BusinessException("该评估项目已有评估记录，无法删除");
        }
        itemRepository.deleteById(id);
    }

    private void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("项目名称不能为空");
        }
    }
}
