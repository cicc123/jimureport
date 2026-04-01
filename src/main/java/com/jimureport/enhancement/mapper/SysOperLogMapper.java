package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
