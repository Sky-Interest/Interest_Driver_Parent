package com.gec.interest.mgr.service;

import com.gec.interest.model.entity.system.SysLoginLog;
import com.gec.interest.model.query.system.SysLoginLogQuery;
import com.gec.interest.model.vo.base.PageVo;

public interface SysLoginLogService {

    PageVo<SysLoginLog> findPage(Long page, Long limit, SysLoginLogQuery sysLoginLogQuery);

    /**
     * 记录登录信息
     *
     * @param sysLoginLog
     * @return
     */
    void recordLoginLog(SysLoginLog sysLoginLog);

    SysLoginLog getById(Long id);
}
