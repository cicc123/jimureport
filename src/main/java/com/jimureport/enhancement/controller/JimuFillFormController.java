package com.jimureport.enhancement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jimureport.enhancement.entity.JimuFillFormConfig;
import com.jimureport.enhancement.entity.JimuFillDraft;
import com.jimureport.enhancement.entity.JimuFillSubmitRecord;
import com.jimureport.enhancement.service.JimuFillFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 填报表单控制器
 */
@Tag(name = "填报表单", description = "填报表单相关接口")
@RestController
@RequestMapping("/api/fill")
@RequiredArgsConstructor
public class JimuFillFormController {

    private final JimuFillFormService fillFormService;

    @Operation(summary = "获取表单配置")
    @GetMapping("/forms/{reportId}")
    public JimuFillFormConfig getFormConfig(@PathVariable String reportId) {
        return fillFormService.getFormConfig(reportId);
    }

    @Operation(summary = "获取表单列表")
    @GetMapping("/forms")
    @SaCheckPermission("report:fill:list")
    public IPage<JimuFillFormConfig> getFormList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return fillFormService.getFormConfigPage(pageNum, pageSize, keyword);
    }

    @Operation(summary = "创建表单配置")
    @PostMapping("/forms")
    @SaCheckPermission("report:fill:add")
    public JimuFillFormConfig createFormConfig(@RequestBody Map<String, Object> params) {
        JimuFillFormConfig config = new JimuFillFormConfig();
        config.setReportId((String) params.get("reportId"));
        config.setFormName((String) params.get("formName"));
        config.setFormDesc((String) params.get("formDesc"));
        config.setSubmitType((String) params.get("submitType"));
        config.setSubmitConfig((String) params.get("submitConfig"));
        config.setAllowDraft((String) params.get("allowDraft"));
        config.setAllowDuplicate((String) params.get("allowDuplicate"));
        config.setDuplicateCheckFields((String) params.get("duplicateCheckFields"));
        config.setSuccessMessage((String) params.get("successMessage"));
        config.setRedirectUrl((String) params.get("redirectUrl"));

        return fillFormService.createFormConfig(config, null);
    }

    @Operation(summary = "更新表单配置")
    @PutMapping("/forms")
    @SaCheckPermission("report:fill:edit")
    public JimuFillFormConfig updateFormConfig(@RequestBody Map<String, Object> params) {
        JimuFillFormConfig config = new JimuFillFormConfig();
        config.setId((String) params.get("id"));
        config.setReportId((String) params.get("reportId"));
        config.setFormName((String) params.get("formName"));
        config.setFormDesc((String) params.get("formDesc"));
        config.setSubmitType((String) params.get("submitType"));
        config.setSubmitConfig((String) params.get("submitConfig"));
        config.setAllowDraft((String) params.get("allowDraft"));
        config.setAllowDuplicate((String) params.get("allowDuplicate"));
        config.setDuplicateCheckFields((String) params.get("duplicateCheckFields"));
        config.setSuccessMessage((String) params.get("successMessage"));
        config.setRedirectUrl((String) params.get("redirectUrl"));

        return fillFormService.updateFormConfig(config, null);
    }

    @Operation(summary = "删除表单配置")
    @DeleteMapping("/forms/{id}")
    @SaCheckPermission("report:fill:remove")
    public boolean deleteFormConfig(@PathVariable String id) {
        return fillFormService.deleteFormConfig(id);
    }

    @Operation(summary = "提交表单数据")
    @PostMapping("/submit")
    public JimuFillSubmitRecord submitFormData(@RequestBody Map<String, Object> params, @RequestAttribute String userId) {
        String formId = (String) params.get("formId");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) params.get("data");
        return fillFormService.submitFormData(formId, userId, data);
    }

    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public JimuFillDraft saveDraft(@RequestBody Map<String, Object> params, @RequestAttribute String userId) {
        String formId = (String) params.get("formId");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) params.get("data");
        return fillFormService.saveDraft(formId, userId, data);
    }

    @Operation(summary = "获取草稿")
    @GetMapping("/draft/{formId}")
    public JimuFillDraft getDraft(@PathVariable String formId, @RequestAttribute String userId) {
        return fillFormService.getDraft(formId, userId);
    }

    @Operation(summary = "删除草稿")
    @DeleteMapping("/draft/{formId}")
    public boolean deleteDraft(@PathVariable String formId, @RequestAttribute String userId) {
        return fillFormService.deleteDraft(formId, userId);
    }

    @Operation(summary = "查询提交记录")
    @GetMapping("/records")
    @SaCheckPermission("report:fill:list")
    public IPage<JimuFillSubmitRecord> getSubmitRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String formId,
            @RequestParam(required = false) String userId) {
        return fillFormService.getSubmitRecords(pageNum, pageSize, formId, userId);
    }

    @Operation(summary = "导出填报数据")
    @GetMapping("/export/{formId}")
    @SaCheckPermission("report:fill:export")
    public List<Map<String, Object>> exportFormData(@PathVariable String formId) {
        return fillFormService.exportFormData(formId);
    }
}
