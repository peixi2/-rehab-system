package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.entity.TreatmentItem;
import com.rehab.system.repository.TreatmentItemRepository;
import com.rehab.system.repository.TreatmentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TreatmentItemService {

    private final TreatmentItemRepository itemRepository;
    private final TreatmentRecordRepository recordRepository;

    public List<TreatmentItem> list() {
        return itemRepository.findAll();
    }

    public TreatmentItem get(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("治疗项目不存在"));
    }

    public TreatmentItem create(TreatmentItem item) {
        checkName(item.getName());
        item.setId(null);
        return itemRepository.save(item);
    }

    public TreatmentItem update(Long id, TreatmentItem item) {
        checkName(item.getName());
        TreatmentItem db = get(id);
        db.setName(item.getName());
        db.setDescription(item.getDescription());
        db.setDurationMinutes(item.getDurationMinutes());
        return itemRepository.save(db);
    }

    public void delete(Long id) {
        get(id);
        if (recordRepository.existsByItemId(id)) {
            throw new BusinessException("该治疗项目已有治疗记录，无法删除");
        }
        itemRepository.deleteById(id);
    }

    private void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("项目名称不能为空");
        }
    }
}
