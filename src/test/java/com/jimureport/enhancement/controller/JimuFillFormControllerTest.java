package com.jimureport.enhancement.controller;

import com.jimureport.enhancement.entity.JimuFillFormConfig;
import com.jimureport.enhancement.entity.JimuFillDraft;
import com.jimureport.enhancement.entity.JimuFillSubmitRecord;
import com.jimureport.enhancement.service.JimuFillFormService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JimuFillFormController 逻辑测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JimuFillFormControllerTest {

    @Mock
    private JimuFillFormService fillFormService;

    @InjectMocks
    private JimuFillFormController controller;

    @Nested
    @DisplayName("表单配置操作")
    class FormConfigTests {

        @Test
        @DisplayName("TC-FC001: 获取表单配置")
        void getFormConfig_Success() {
            JimuFillFormConfig config = new JimuFillFormConfig();
            config.setId("form-001");
            config.setFormName("测试表单");
            config.setReportId("report-001");
            when(fillFormService.getFormConfig("report-001")).thenReturn(config);

            JimuFillFormConfig result = controller.getFormConfig("report-001");

            assertNotNull(result);
            assertEquals("测试表单", result.getFormName());
        }

        @Test
        @DisplayName("TC-FC002: 获取不存在的表单配置")
        void getFormConfig_NotFound() {
            when(fillFormService.getFormConfig("nonexist")).thenReturn(null);

            JimuFillFormConfig result = controller.getFormConfig("nonexist");

            assertNull(result);
        }

        @Test
        @DisplayName("TC-FC003: 创建表单配置")
        void createFormConfig_Success() {
            JimuFillFormConfig created = new JimuFillFormConfig();
            created.setId("form-new");
            created.setFormName("新表单");
            created.setReportId("report-002");
            when(fillFormService.createFormConfig(any(), isNull())).thenReturn(created);

            Map<String, Object> params = new HashMap<>();
            params.put("reportId", "report-002");
            params.put("formName", "新表单");

            JimuFillFormConfig result = controller.createFormConfig(params);

            assertNotNull(result);
            assertEquals("新表单", result.getFormName());
            verify(fillFormService).createFormConfig(any(), isNull());
        }

        @Test
        @DisplayName("TC-FC004: 删除表单配置")
        void deleteFormConfig_Success() {
            when(fillFormService.deleteFormConfig("form-001")).thenReturn(true);

            boolean result = controller.deleteFormConfig("form-001");

            assertTrue(result);
            verify(fillFormService).deleteFormConfig("form-001");
        }
    }

    @Nested
    @DisplayName("填报提交操作")
    class SubmitTests {

        @Test
        @DisplayName("TC-FC005: 提交表单数据")
        void submitFormData_Success() {
            JimuFillSubmitRecord record = new JimuFillSubmitRecord();
            record.setId("record-001");
            record.setFormId("form-001");
            record.setUserId("user-001");
            record.setSubmitStatus("success");
            when(fillFormService.submitFormData(eq("form-001"), eq("user-001"), anyMap()))
                    .thenReturn(record);

            Map<String, Object> params = new HashMap<>();
            params.put("formId", "form-001");
            Map<String, Object> data = new HashMap<>();
            data.put("name", "张三");
            params.put("data", data);

            JimuFillSubmitRecord result = controller.submitFormData(params, "user-001");

            assertNotNull(result);
            assertEquals("success", result.getSubmitStatus());
        }

        @Test
        @DisplayName("TC-FC006: 提交到不存在的表单")
        void submitFormData_FormNotFound() {
            when(fillFormService.submitFormData(eq("nonexist"), anyString(), anyMap()))
                    .thenThrow(new RuntimeException("表单不存在"));

            Map<String, Object> params = new HashMap<>();
            params.put("formId", "nonexist");
            params.put("data", new HashMap<>());

            assertThrows(RuntimeException.class,
                    () -> controller.submitFormData(params, "user-001"));
        }
    }

    @Nested
    @DisplayName("草稿操作")
    class DraftTests {

        @Test
        @DisplayName("TC-FC007: 保存草稿")
        void saveDraft_Success() {
            JimuFillDraft draft = new JimuFillDraft();
            draft.setId("draft-001");
            draft.setFormId("form-001");
            draft.setUserId("user-001");
            when(fillFormService.saveDraft(eq("form-001"), eq("user-001"), anyMap()))
                    .thenReturn(draft);

            Map<String, Object> params = new HashMap<>();
            params.put("formId", "form-001");
            Map<String, Object> data = new HashMap<>();
            data.put("name", "李四");
            params.put("data", data);

            JimuFillDraft result = controller.saveDraft(params, "user-001");

            assertNotNull(result);
            assertEquals("draft-001", result.getId());
        }

        @Test
        @DisplayName("TC-FC008: 获取草稿")
        void getDraft_Success() {
            JimuFillDraft draft = new JimuFillDraft();
            draft.setId("draft-001");
            when(fillFormService.getDraft("form-001", "user-001")).thenReturn(draft);

            JimuFillDraft result = controller.getDraft("form-001", "user-001");

            assertNotNull(result);
        }

        @Test
        @DisplayName("TC-FC009: 获取不存在的草稿")
        void getDraft_NotFound() {
            when(fillFormService.getDraft("form-001", "user-001")).thenReturn(null);

            JimuFillDraft result = controller.getDraft("form-001", "user-001");

            assertNull(result);
        }

        @Test
        @DisplayName("TC-FC010: 删除草稿")
        void deleteDraft_Success() {
            when(fillFormService.deleteDraft("form-001", "user-001")).thenReturn(true);

            boolean result = controller.deleteDraft("form-001", "user-001");

            assertTrue(result);
        }
    }
}
