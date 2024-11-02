package com.gec.interest.mgr.service;

import com.gec.interest.model.entity.system.SysOperLog;
import com.gec.interest.model.query.system.SysOperLogQuery;
import com.gec.interest.model.vo.base.PageVo;

public interface SysOperLogService {

    PageVo<SysOperLog> findPage(Long page, Long limit, SysOperLogQuery sysOperLogQuery);

    /**
     * 保存系统日志记录
     */
    void saveSysLog(SysOperLog sysOperLog);

    SysOperLog getById(Long id);
}
