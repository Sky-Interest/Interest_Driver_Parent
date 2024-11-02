package com.gec.interest.mgr.service;


import com.gec.interest.model.entity.system.SysUser;
import com.gec.interest.model.query.system.SysUserQuery;
import com.gec.interest.model.vo.base.PageVo;

public interface SysUserService {

    SysUser getById(Long id);

    void save(SysUser sysUser);

    void update(SysUser sysUser);

    void remove(Long id);

    PageVo<SysUser> findPage(Long page, Long limit, SysUserQuery sysUserQuery);

    void updateStatus(Long id, Integer status);


}
