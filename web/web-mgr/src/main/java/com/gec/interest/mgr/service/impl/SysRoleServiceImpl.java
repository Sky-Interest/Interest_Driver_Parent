package com.gec.interest.mgr.service.impl;

import com.gec.interest.mgr.service.SysRoleService;
import com.gec.interest.model.entity.system.SysRole;
import com.gec.interest.model.query.system.SysRoleQuery;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.system.AssginRoleVo;
import com.gec.interest.system.client.SysRoleFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleFeignClient sysRoleFeignClient;

    @Override
    public SysRole getById(Long id) {
        return sysRoleFeignClient.getById(id).getData();
    }

    @Override
    public void save(SysRole sysRole) {
        sysRoleFeignClient.save(sysRole);
    }

    @Override
    public void update(SysRole sysRole) {
        sysRoleFeignClient.update(sysRole);
    }

    @Override
    public void remove(Long id) {
        sysRoleFeignClient.remove(id);
    }

    @Override
    public PageVo<SysRole> findPage(Long page, Long limit, SysRoleQuery roleQuery) {
        return sysRoleFeignClient.findPage(page,limit, roleQuery).getData();
    }

    @Override
    public void batchRemove(List<Long> idList) {
        sysRoleFeignClient.batchRemove(idList);
    }

    @Override
    public Map<String, Object> toAssign(Long userId) {
        return sysRoleFeignClient.toAssign(userId).getData();
    }

    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        sysRoleFeignClient.doAssign(assginRoleVo);
    }

    @Override
    public List<SysRole> findAll() {
        return sysRoleFeignClient.findAll().getData();
    }
}
