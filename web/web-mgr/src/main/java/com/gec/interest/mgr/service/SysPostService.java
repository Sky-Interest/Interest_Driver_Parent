package com.gec.interest.mgr.service;

import com.gec.interest.model.entity.system.SysPost;
import com.gec.interest.model.query.system.SysPostQuery;
import com.gec.interest.model.vo.base.PageVo;

import java.util.List;

public interface SysPostService {

    SysPost getById(Long id);

    void save(SysPost sysPost);

    void update(SysPost sysPost);

    void remove(Long id);

    PageVo<SysPost> findPage(Long page, Long limit, SysPostQuery sysPostQuery);

    void updateStatus(Long id, Integer status);

    List<SysPost> findAll();
}
