package com.repair.service;

import com.repair.entity.SysDict;

import java.util.List;

public interface SysDictService {

    SysDict addDict(SysDict dict);

    void deleteDict(Integer id);

    SysDict updateDict(SysDict dict);

    SysDict getDictById(Integer id);

    List<SysDict> listDicts(String dictType);

    List<String> listDictTypes();

    List<SysDict> listEnabledDicts(String dictType);
}