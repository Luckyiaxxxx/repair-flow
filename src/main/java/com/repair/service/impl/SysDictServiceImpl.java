package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.SysDict;
import com.repair.mapper.SysDictMapper;
import com.repair.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysDictServiceImpl implements SysDictService {

    @Autowired
    private SysDictMapper sysDictMapper;

    private void checkStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException("状态值只能为0或1");
        }
    }

    private void checkDuplicate(String dictType, String dictValue, Integer excludeId) {
        SysDict same = sysDictMapper.selectByTypeAndValue(dictType, dictValue);
        if (same != null && !same.getId().equals(excludeId)) {
            throw new BusinessException("该字典类型下已存在相同的字典值");
        }
    }

    @Override
    @Transactional
    public SysDict addDict(SysDict dict) {
        if (dict.getDictType() == null || dict.getDictType().trim().isEmpty()) {
            throw new BusinessException("字典类型不能为空");
        }
        if (dict.getDictLabel() == null || dict.getDictLabel().trim().isEmpty()) {
            throw new BusinessException("显示名称不能为空");
        }
        if (dict.getDictValue() == null || dict.getDictValue().trim().isEmpty()) {
            throw new BusinessException("字典值不能为空");
        }
        checkStatus(dict.getStatus());
        dict.setDictType(dict.getDictType().trim());
        dict.setDictLabel(dict.getDictLabel().trim());
        dict.setDictValue(dict.getDictValue().trim());
        checkDuplicate(dict.getDictType(), dict.getDictValue(), null);
        if (dict.getSortOrder() == null) {
            dict.setSortOrder(0);
        }
        if (dict.getStatus() == null) {
            dict.setStatus(1);
        }
        sysDictMapper.insert(dict);
        return dict;
    }

    @Override
    @Transactional
    public void deleteDict(Integer id) {
        if (id == null) {
            throw new BusinessException("字典ID不能为空");
        }
        SysDict dict = sysDictMapper.selectById(id);
        if (dict == null) {
            throw new BusinessException("字典不存在");
        }
        sysDictMapper.deleteById(id);
    }

    @Override
    @Transactional
    public SysDict updateDict(SysDict dict) {
        if (dict.getId() == null) {
            throw new BusinessException("字典ID不能为空");
        }
        SysDict existing = sysDictMapper.selectById(dict.getId());
        if (existing == null) {
            throw new BusinessException("字典不存在");
        }
        checkStatus(dict.getStatus());
        String dictType = dict.getDictType() != null && !dict.getDictType().trim().isEmpty()
                ? dict.getDictType().trim() : existing.getDictType();
        String dictValue = dict.getDictValue() != null && !dict.getDictValue().trim().isEmpty()
                ? dict.getDictValue().trim() : existing.getDictValue();
        checkDuplicate(dictType, dictValue, dict.getId());
        dict.setDictType(dictType);
        dict.setDictValue(dictValue);
        if (dict.getDictLabel() == null || dict.getDictLabel().trim().isEmpty()) {
            dict.setDictLabel(existing.getDictLabel());
        } else {
            dict.setDictLabel(dict.getDictLabel().trim());
        }
        if (dict.getSortOrder() == null) {
            dict.setSortOrder(existing.getSortOrder());
        }
        if (dict.getStatus() == null) {
            dict.setStatus(existing.getStatus());
        }
        sysDictMapper.updateById(dict);
        return sysDictMapper.selectById(dict.getId());
    }

    @Override
    public SysDict getDictById(Integer id) {
        if (id == null) {
            throw new BusinessException("字典ID不能为空");
        }
        SysDict dict = sysDictMapper.selectById(id);
        if (dict == null) {
            throw new BusinessException("字典不存在");
        }
        return dict;
    }

    @Override
    public List<SysDict> listDicts(String dictType) {
        if (dictType != null && !dictType.trim().isEmpty()) {
            return sysDictMapper.selectByType(dictType.trim());
        }
        return sysDictMapper.selectAll();
    }

    @Override
    public List<String> listDictTypes() {
        return sysDictMapper.selectAllTypes();
    }

    @Override
    public List<SysDict> listEnabledDicts(String dictType) {
        if (dictType == null || dictType.trim().isEmpty()) {
            throw new BusinessException("字典类型不能为空");
        }
        return sysDictMapper.selectEnabledByType(dictType.trim());
    }
}