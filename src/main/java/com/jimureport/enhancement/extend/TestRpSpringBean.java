package com.jimureport.enhancement.extend;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.jmreport.api.data.IDataSetFactory;
import org.jeecg.modules.jmreport.desreport.model.JmPage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义报表数据源示例（参考 jeecgboot/jimureport 原始示例）
 *
 * 实现 IDataSetFactory 接口，可以在积木报表中通过 Java Bean 方式自定义数据源。
 * 使用方式：在积木报表数据集配置中选择"自定义 Java Bean"，输入 bean 名称 "testDataJavaBean"
 */
@Slf4j
@Component("testDataJavaBean")
public class TestRpSpringBean implements IDataSetFactory {

    /**
     * 不分页时返回 List
     * @param param 参数（包括浏览器地址栏参数和查询条件）
     * @return 数据列表
     */
    @Override
    public List<Map<String, Object>> createData(Map<String, Object> param) {
        log.info("自定义 Java Bean，无分页：{}", param);
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> row1 = new HashMap<>();
        row1.put("name", "张三");
        row1.put("age", "14");
        list.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("name", "李四");
        row2.put("age", "15");
        list.add(row2);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("name", "王五");
        row3.put("age", "16");
        list.add(row3);

        return list;
    }

    /**
     * 分页时返回 JmPage
     * @param param 参数（包括 pageNo、pageSize 和查询条件）
     * @return 分页数据
     */
    @Override
    public JmPage createPageData(Map<String, Object> param) {
        if (!param.containsKey("pageSize")) {
            param.put("pageSize", 10);
        }

        log.info("自定义 Java Bean，有分页：{}", param);

        JmPage page = new JmPage();
        List<Map<String, Object>> list = new ArrayList<>();
        Object pageSizeObj = param.get("pageSize");
        int pageSize = 10;
        try { pageSize = Integer.parseInt(pageSizeObj.toString()); } catch (Exception e) {}

        Map<String, Object> row1 = new HashMap<>();
        row1.put("name", "张三");
        row1.put("age", "14");
        row1.put("sex", "1");
        list.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("name", "李四");
        row2.put("age", "15");
        row2.put("sex", "2");
        list.add(row2);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("name", "王五");
        row3.put("age", "16");
        row3.put("sex", "2");
        list.add(row3);

        page.setPageSize(pageSize);
        page.setTotal(20);
        page.setRecords(list);
        return page;
    }
}
