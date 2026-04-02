package com.jimureport.enhancement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jimureport.enhancement.entity.*;
import com.jimureport.enhancement.mapper.*;
import com.jimureport.enhancement.service.impl.JimuFillFormServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JimuFillFormServiceImplTest {

    @Mock private JimuFillFormConfigMapper formConfigMapper;
    @Mock private JimuFillFormFieldMapper formFieldMapper;
    @Mock private JimuFillDraftMapper draftMapper;
    @Mock private JimuFillSubmitRecordMapper submitRecordMapper;

    private ObjectMapper objectMapper = new ObjectMapper();
    private JimuFillFormServiceImpl fillFormService;

    private JimuFillFormConfig testFormConfig;
    private List<JimuFillFormField> testFields;

    @BeforeEach
    void setUp() {
        fillFormService = new JimuFillFormServiceImpl(
                formConfigMapper, formFieldMapper, draftMapper, submitRecordMapper, objectMapper);

        testFormConfig = new JimuFillFormConfig();
        testFormConfig.setId("form-001");
        testFormConfig.setFormName("员工信息表");
        testFormConfig.setReportId("report-001");
        testFormConfig.setStatus("0");
        testFormConfig.setAllowDuplicate("0");
        testFormConfig.setDuplicateCheckFields("name,age");
        testFormConfig.setDuplicateCheckFields("name,age");

        testFields = new ArrayList<>();

        JimuFillFormField nameField = new JimuFillFormField();
        nameField.setFieldName("name");
        nameField.setFieldLabel("姓名");
        nameField.setFieldType("text");
        nameField.setIsRequired("1");
        nameField.setMaxLength(50);
        testFields.add(nameField);

        JimuFillFormField ageField = new JimuFillFormField();
        ageField.setFieldName("age");
        ageField.setFieldLabel("年龄");
        ageField.setFieldType("number");
        ageField.setIsRequired("1");
        ageField.setMinValue(new BigDecimal(18));
        ageField.setMaxValue(new BigDecimal(100));
        testFields.add(ageField);
    }

    @Nested
    @DisplayName("表单配置测试")
    class FormConfigTests {

        @Test
        @DisplayName("TC-F001: 获取表单配置")
        void getFormConfig_Success() {
            when(formConfigMapper.selectByReportId("report-001")).thenReturn(testFormConfig);

            JimuFillFormConfig result = fillFormService.getFormConfig("report-001");

            assertNotNull(result);
            assertEquals("员工信息表", result.getFormName());
        }

        @Test
        @DisplayName("TC-F002: 创建表单配置（含字段）")
        void createFormConfig_WithFields() {
            when(formConfigMapper.insert(any(JimuFillFormConfig.class))).thenReturn(1);
            when(formFieldMapper.insert(any(JimuFillFormField.class))).thenReturn(1);

            JimuFillFormConfig result = fillFormService.createFormConfig(testFormConfig, testFields);

            assertNotNull(result);
            verify(formConfigMapper).insert(any(JimuFillFormConfig.class));
            verify(formFieldMapper, times(2)).insert(any(JimuFillFormField.class));
        }

        @Test
        @DisplayName("TC-F003: 删除表单配置及其关联数据")
        void deleteFormConfig_Cascading() {
            when(formFieldMapper.deleteByFormId("form-001")).thenReturn(2);
            when(draftMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
            when(formConfigMapper.deleteById("form-001")).thenReturn(1);

            boolean result = fillFormService.deleteFormConfig("form-001");

            assertTrue(result);
            verify(formFieldMapper).deleteByFormId("form-001");
            verify(formConfigMapper).deleteById("form-001");
        }
    }

    @Nested
    @DisplayName("填报提交测试")
    class SubmitTests {

        @Test
        @DisplayName("TC-F004: 提交到不存在的表单")
        void submitFormData_FormNotFound() {
            when(formConfigMapper.selectById("nonexistent")).thenReturn(null);

            assertThrows(RuntimeException.class,
                    () -> fillFormService.submitFormData("nonexistent", "user-001", new HashMap<>()));
        }

        @Test
        @DisplayName("TC-F005: 提交到已停用表单")
        void submitFormData_FormDisabled() {
            JimuFillFormConfig disabled = new JimuFillFormConfig();
            disabled.setId("form-002");
            disabled.setStatus("1");

            when(formConfigMapper.selectById("form-002")).thenReturn(disabled);

            assertThrows(RuntimeException.class,
                    () -> fillFormService.submitFormData("form-002", "user-001", new HashMap<>()));
        }

        @Test
        @DisplayName("TC-F006: 必填字段为空")
        void submitFormData_RequiredFieldMissing() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "");
            data.put("age", 28);

            when(formConfigMapper.selectById("form-001")).thenReturn(testFormConfig);
            when(formFieldMapper.selectByFormId("form-001")).thenReturn(testFields);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> fillFormService.submitFormData("form-001", "user-001", data));
            assertTrue(ex.getMessage().contains("不能为空"));
        }

        @Test
        @DisplayName("TC-F007: 数值超最大值")
        void submitFormData_NumberTooHigh() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "张三");
            data.put("age", 150);

            when(formConfigMapper.selectById("form-001")).thenReturn(testFormConfig);
            when(formFieldMapper.selectByFormId("form-001")).thenReturn(testFields);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> fillFormService.submitFormData("form-001", "user-001", data));
            assertTrue(ex.getMessage().contains("不能大于"));
        }

        @Test
        @DisplayName("TC-F008: 数值低于最小值")
        void submitFormData_NumberTooLow() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "张三");
            data.put("age", 10);

            when(formConfigMapper.selectById("form-001")).thenReturn(testFormConfig);
            when(formFieldMapper.selectByFormId("form-001")).thenReturn(testFields);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> fillFormService.submitFormData("form-001", "user-001", data));
            assertTrue(ex.getMessage().contains("不能小于"));
        }

        @Test
        @DisplayName("TC-F009: 非法数字格式")
        void submitFormData_InvalidNumber() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "张三");
            data.put("age", "abc");

            when(formConfigMapper.selectById("form-001")).thenReturn(testFormConfig);
            when(formFieldMapper.selectByFormId("form-001")).thenReturn(testFields);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> fillFormService.submitFormData("form-001", "user-001", data));
            assertTrue(ex.getMessage().contains("必须为数字"));
        }

        @Test
        @DisplayName("TC-F010: 重复提交检测")
        void submitFormData_DuplicateDetected() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "张三");
            data.put("age", 28);

            JimuFillSubmitRecord existing = new JimuFillSubmitRecord();
            existing.setSubmitData("{\"name\":\"张三\",\"age\":28}");

            when(formConfigMapper.selectById("form-001")).thenReturn(testFormConfig);
            when(formFieldMapper.selectByFormId("form-001")).thenReturn(testFields);
            when(submitRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(existing));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> fillFormService.submitFormData("form-001", "user-001", data));
            assertTrue(ex.getMessage().contains("重复提交"));
        }
    }

    @Nested
    @DisplayName("草稿测试")
    class DraftTests {

        @Test
        @DisplayName("TC-F011: 保存新草稿")
        void saveDraft_New() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "李四");

            when(draftMapper.selectByFormAndUser("form-001", "user-001")).thenReturn(null);
            when(draftMapper.insert(any(JimuFillDraft.class))).thenReturn(1);

            JimuFillDraft result = fillFormService.saveDraft("form-001", "user-001", data);

            assertNotNull(result);
            assertEquals("form-001", result.getFormId());
            verify(draftMapper).insert(any(JimuFillDraft.class));
        }

        @Test
        @DisplayName("TC-F012: 更新已有草稿")
        void saveDraft_UpdateExisting() {
            JimuFillDraft existing = new JimuFillDraft();
            existing.setId("draft-001");
            existing.setFormId("form-001");
            existing.setUserId("user-001");

            Map<String, Object> data = new HashMap<>();
            data.put("name", "新数据");

            when(draftMapper.selectByFormAndUser("form-001", "user-001")).thenReturn(existing);
            when(draftMapper.updateById(any(JimuFillDraft.class))).thenReturn(1);

            fillFormService.saveDraft("form-001", "user-001", data);

            verify(draftMapper).updateById(any(JimuFillDraft.class));
            verify(draftMapper, never()).insert(any(JimuFillDraft.class));
        }

        @Test
        @DisplayName("TC-F013: 删除草稿")
        void deleteDraft_Success() {
            JimuFillDraft draft = new JimuFillDraft();
            draft.setId("draft-001");
            when(draftMapper.selectByFormAndUser("form-001", "user-001")).thenReturn(draft);

            boolean result = fillFormService.deleteDraft("form-001", "user-001");

            assertTrue(result);
            verify(draftMapper).deleteById("draft-001");
        }

        @Test
        @DisplayName("TC-F014: 删除不存在的草稿")
        void deleteDraft_NotFound() {
            when(draftMapper.selectByFormAndUser("form-001", "user-001")).thenReturn(null);

            boolean result = fillFormService.deleteDraft("form-001", "user-001");

            assertTrue(result); // 不存在也返回 true
            verify(draftMapper, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("查询测试")
    class QueryTests {

        @Test
        @DisplayName("TC-F015: 分页查询提交记录")
        void getSubmitRecords_Success() {
            Page<JimuFillSubmitRecord> page = new Page<>(1, 10);
            JimuFillSubmitRecord record = new JimuFillSubmitRecord();
            record.setFormId("form-001");
            page.setRecords(Collections.singletonList(record));
            page.setTotal(1);

            when(submitRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            IPage<JimuFillSubmitRecord> result = fillFormService.getSubmitRecords(1, 10, "form-001", null);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("TC-F016: 导出填报数据")
        void exportFormData_Success() {
            JimuFillSubmitRecord record = new JimuFillSubmitRecord();
            record.setFormId("form-001");
            record.setSubmitStatus("success");
            record.setSubmitData("{\"name\":\"张三\",\"age\":28}");

            when(submitRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(record));

            List<Map<String, Object>> result = fillFormService.exportFormData("form-001");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("张三", result.get(0).get("name"));
        }
    }
}
