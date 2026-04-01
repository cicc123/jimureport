package com.jimureport.enhancement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jimureport.enhancement.entity.*;
import com.jimureport.enhancement.mapper.*;
import com.jimureport.enhancement.service.JimuFillFormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 填报表单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JimuFillFormServiceImpl implements JimuFillFormService {

    private final JimuFillFormConfigMapper formConfigMapper;
    private final JimuFillFormFieldMapper formFieldMapper;
    private final JimuFillDraftMapper draftMapper;
    private final JimuFillSubmitRecordMapper submitRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public JimuFillFormConfig getFormConfig(String reportId) {
        return formConfigMapper.selectByReportId(reportId);
    }

    @Override
    public List<JimuFillFormField> getFormFields(String formId) {
        return formFieldMapper.selectByFormId(formId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JimuFillFormConfig createFormConfig(JimuFillFormConfig config, List<JimuFillFormField> fields) {
        // 保存表单配置
        formConfigMapper.insert(config);

        // 保存字段配置
        if (fields != null && !fields.isEmpty()) {
            for (JimuFillFormField field : fields) {
                field.setFormId(config.getId());
                formFieldMapper.insert(field);
            }
        }

        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JimuFillFormConfig updateFormConfig(JimuFillFormConfig config, List<JimuFillFormField> fields) {
        // 更新表单配置
        formConfigMapper.updateById(config);

        // 删除原有字段
        formFieldMapper.deleteByFormId(config.getId());

        // 保存新字段
        if (fields != null && !fields.isEmpty()) {
            for (JimuFillFormField field : fields) {
                field.setId(null);
                field.setFormId(config.getId());
                formFieldMapper.insert(field);
            }
        }

        return formConfigMapper.selectById(config.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFormConfig(String formId) {
        // 删除字段
        formFieldMapper.deleteByFormId(formId);

        // 删除草稿
        draftMapper.delete(
                new LambdaQueryWrapper<JimuFillDraft>()
                        .eq(JimuFillDraft::getFormId, formId)
        );

        // 删除表单配置
        formConfigMapper.deleteById(formId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JimuFillSubmitRecord submitFormData(String formId, String userId, Map<String, Object> data) {
        // 获取表单配置
        JimuFillFormConfig config = formConfigMapper.selectById(formId);
        if (config == null) {
            throw new RuntimeException("表单不存在");
        }

        if ("1".equals(config.getStatus())) {
            throw new RuntimeException("表单已停用");
        }

        // 获取表单字段
        List<JimuFillFormField> fields = formFieldMapper.selectByFormId(formId);

        // 校验数据
        validateFormData(fields, data);

        // 检查重复提交
        if ("0".equals(config.getAllowDuplicate())) {
            checkDuplicateSubmit(formId, userId, data, config.getDuplicateCheckFields());
        }

        // 创建提交记录
        JimuFillSubmitRecord record = new JimuFillSubmitRecord();
        record.setFormId(formId);
        record.setUserId(userId);
        record.setSubmitData(convertDataToJson(data));
        record.setSubmitStatus("success");

        submitRecordMapper.insert(record);

        // 删除草稿
        deleteDraft(formId, userId);

        return record;
    }

    @Override
    public JimuFillDraft saveDraft(String formId, String userId, Map<String, Object> data) {
        // 查询已有草稿
        JimuFillDraft draft = draftMapper.selectByFormAndUser(formId, userId);

        if (draft != null) {
            // 更新草稿
            draft.setDraftData(convertDataToJson(data));
            draftMapper.updateById(draft);
        } else {
            // 创建草稿
            draft = new JimuFillDraft();
            draft.setFormId(formId);
            draft.setUserId(userId);
            draft.setDraftData(convertDataToJson(data));
            draftMapper.insert(draft);
        }

        return draft;
    }

    @Override
    public JimuFillDraft getDraft(String formId, String userId) {
        return draftMapper.selectByFormAndUser(formId, userId);
    }

    @Override
    public boolean deleteDraft(String formId, String userId) {
        JimuFillDraft draft = draftMapper.selectByFormAndUser(formId, userId);
        if (draft != null) {
            draftMapper.deleteById(draft.getId());
        }
        return true;
    }

    @Override
    public IPage<JimuFillSubmitRecord> getSubmitRecords(int pageNum, int pageSize, String formId, String userId) {
        Page<JimuFillSubmitRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<JimuFillSubmitRecord> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(formId)) {
            wrapper.eq(JimuFillSubmitRecord::getFormId, formId);
        }
        if (StringUtils.hasText(userId)) {
            wrapper.eq(JimuFillSubmitRecord::getUserId, userId);
        }

        wrapper.orderByDesc(JimuFillSubmitRecord::getCreateTime);

        return submitRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Map<String, Object>> exportFormData(String formId) {
        // 查询提交记录
        List<JimuFillSubmitRecord> records = submitRecordMapper.selectList(
                new LambdaQueryWrapper<JimuFillSubmitRecord>()
                        .eq(JimuFillSubmitRecord::getFormId, formId)
                        .eq(JimuFillSubmitRecord::getSubmitStatus, "success")
                        .orderByDesc(JimuFillSubmitRecord::getCreateTime)
        );

        // 获取字段配置
        List<JimuFillFormField> fields = formFieldMapper.selectByFormId(formId);

        // 转换数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (JimuFillSubmitRecord record : records) {
            Map<String, Object> row = convertJsonToData(record.getSubmitData());
            row.put("submitTime", record.getCreateTime());
            result.add(row);
        }

        return result;
    }

    @Override
    public IPage<JimuFillFormConfig> getFormConfigPage(int pageNum, int pageSize, String keyword) {
        Page<JimuFillFormConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<JimuFillFormConfig> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(JimuFillFormConfig::getFormName, keyword)
                    .or()
                    .like(JimuFillFormConfig::getReportId, keyword);
        }

        wrapper.orderByDesc(JimuFillFormConfig::getCreateTime);

        return formConfigMapper.selectPage(page, wrapper);
    }

    /**
     * 校验表单数据
     */
    private void validateFormData(List<JimuFillFormField> fields, Map<String, Object> data) {
        for (JimuFillFormField field : fields) {
            String fieldName = field.getFieldName();
            Object value = data.get(fieldName);

            // 必填校验
            if ("1".equals(field.getIsRequired()) && (value == null || "".equals(value))) {
                throw new RuntimeException(field.getFieldLabel() + "不能为空");
            }

            if (value == null) {
                continue;
            }

            // 数值范围校验
            if ("number".equals(field.getFieldType())) {
                try {
                    double numValue = Double.parseDouble(value.toString());
                    if (field.getMinValue() != null && numValue < field.getMinValue().doubleValue()) {
                        throw new RuntimeException(field.getFieldLabel() + "不能小于" + field.getMinValue());
                    }
                    if (field.getMaxValue() != null && numValue > field.getMaxValue().doubleValue()) {
                        throw new RuntimeException(field.getFieldLabel() + "不能大于" + field.getMaxValue());
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException(field.getFieldLabel() + "必须为数字");
                }
            }

            // 长度校验
            String strValue = value.toString();
            if (field.getMinLength() != null && strValue.length() < field.getMinLength()) {
                throw new RuntimeException(field.getFieldLabel() + "长度不能小于" + field.getMinLength());
            }
            if (field.getMaxLength() != null && strValue.length() > field.getMaxLength()) {
                throw new RuntimeException(field.getFieldLabel() + "长度不能大于" + field.getMaxLength());
            }
        }
    }

    /**
     * 检查重复提交
     */
    private void checkDuplicateSubmit(String formId, String userId, Map<String, Object> data, String checkFields) {
        if (!StringUtils.hasText(checkFields)) {
            return;
        }

        String[] fields = checkFields.split(",");
        List<JimuFillSubmitRecord> records = submitRecordMapper.selectList(
                new LambdaQueryWrapper<JimuFillSubmitRecord>()
                        .eq(JimuFillSubmitRecord::getFormId, formId)
                        .eq(JimuFillSubmitRecord::getUserId, userId)
                        .eq(JimuFillSubmitRecord::getSubmitStatus, "success")
        );

        for (JimuFillSubmitRecord record : records) {
            Map<String, Object> oldData = convertJsonToData(record.getSubmitData());
            boolean isDuplicate = true;

            for (String field : fields) {
                Object newValue = data.get(field.trim());
                Object oldValue = oldData.get(field.trim());

                if (newValue == null && oldValue == null) {
                    continue;
                }
                if (newValue == null || oldValue == null || !newValue.equals(oldValue)) {
                    isDuplicate = false;
                    break;
                }
            }

            if (isDuplicate) {
                throw new RuntimeException("存在重复提交记录");
            }
        }
    }

    /**
     * 将数据转换为JSON字符串
     */
    private String convertDataToJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("数据转JSON失败", e);
            throw new RuntimeException("数据序列化失败", e);
        }
    }

    /**
     * 将JSON字符串转换为Map
     */
    private Map<String, Object> convertJsonToData(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON转数据失败", e);
            return new HashMap<>();
        }
    }
}
