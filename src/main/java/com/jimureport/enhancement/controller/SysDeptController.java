package com.jimureport.enhancement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jimureport.enhancement.entity.SysDepartment;
import com.jimureport.enhancement.mapper.SysDepartmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理控制器
 */
@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDepartmentMapper deptMapper;

    @Operation(summary = "获取部门列表")
    @GetMapping("/list")
    @SaCheckPermission("system:dept:list")
    public List<SysDepartment> getDeptList() {
        List<SysDepartment> allDepts = deptMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getStatus, "0")
                        .orderByAsc(SysDepartment::getOrderNum)
        );
        return buildDeptTree(allDepts, "0");
    }

    @Operation(summary = "获取部门树")
    @GetMapping("/tree")
    public List<SysDepartment> getDeptTree() {
        List<SysDepartment> allDepts = deptMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getStatus, "0")
                        .orderByAsc(SysDepartment::getOrderNum)
        );
        return buildDeptTree(allDepts, "0");
    }

    /**
     * 构建部门树
     */
    private List<SysDepartment> buildDeptTree(List<SysDepartment> depts, String parentId) {
        List<SysDepartment> tree = new ArrayList<>();
        for (SysDepartment dept : depts) {
            if (parentId.equals(dept.getParentId())) {
                dept.setChildren(buildDeptTree(depts, dept.getId()));
                tree.add(dept);
            }
        }
        return tree;
    }
}
