package com.gec.interest.system.mapper;

import com.gec.interest.model.entity.system.SysOperLog;
import com.gec.interest.model.query.system.SysOperLogQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    IPage<SysOperLog> selectPage(Page<SysOperLog> page, @Param("query") SysOperLogQuery sysOperLogQuery);

}