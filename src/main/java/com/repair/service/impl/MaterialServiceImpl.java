package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.Material;
import com.repair.mapper.MaterialMapper;
import com.repair.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @description:
 * @author: 徐家豪
 * @date: 2026/8/5 16:08
 * @version: 1.0
 */
@Service
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private MaterialMapper materialMapper;

    @Override
    @Transactional
    public Material addMaterial(Material material){
        if(material.getName()==null||material.getName().trim().isEmpty()){
            throw new BusinessException("物料名称不能为空");
        }
        if(material.getCategory()==null||material.getCategory().trim().isEmpty()){
            throw new BusinessException("物料分类不能为空");
        }
        if(material.getUnit()==null||material.getUnit().isEmpty()){
            throw new BusinessException("计量单位不能为空");
        }
        if(material.getStock()==null){
            material.setStock(0);
        }
        if(material.getMinStock()==null){
            material.setMinStock(10);
        }

        materialMapper.insert(material);
        return material;
    }

    @Override
    @Transactional
    public void deleteMaterial(Integer id){
        if(id == null){
            throw new BusinessException("物料ID不能为空");
        }
        Material material  = materialMapper.selectById(id);
        if(material ==null){
            throw new BusinessException("物料不存在");
        }
        if(material.getStock()>0){
            throw new BusinessException("该物料还有库存，不能删除");
        }
        materialMapper.deletById(id);
    }
    
    @Override
    @Transactional
    public Material updateMaterial(Material material){
        if(material.getId()==null){
            throw new BusinessException("物料ID不能为空");
        }
        Material existing = materialMapper.selectById(material.getId());
        if(existing == null){
            throw new BusinessException("物料不存在");
        }
        materialMapper.updateById(material);
        return material;
    }

    @Override
    public Material getMaterialById(Integer id){
        if(id == null){
            throw new BusinessException("物料ID不能为空");
        }
        Material material = materialMapper.selectById(id);
        if(material == null){
            throw new BusinessException("物料不存在");
        }
        return material;
    }

    @Override
    public List<Material> listAllMaterials(){
        return materialMapper.selectAll();
    }

    @Override
    public List<Material> getLowStockMaterials(){
        return materialMapper.selectLowStock();
    }

    @Override
    @Transactional
    public void addStock(Integer materialId,Integer quantity){
        if(materialId == null){
            throw new BusinessException("物料ID不能为空");
        }
        if(quantity ==null||quantity<=0){
            throw new BusinessException("入库数量必须大于0");
        }
        Material material = materialMapper.selectById(materialId);
        if(material==null){
            throw new BusinessException("物料不存在");
        }
        int rows = materialMapper.addStock(materialId,quantity);
        if(rows <= 0){
            throw new BusinessException("入库失败");
        }
    }
    @Override
    @Transactional
    public void deductStock(Integer materialId,Integer quantity){
        if(materialId ==null){
            throw new BusinessException("物料ID不能为空");
        }
        if(quantity==null||quantity<=0){
            throw new BusinessException("出库数量必须大于0");
        }
        Material material =materialMapper.selectById(materialId);
        if(material == null){
            throw new BusinessException("物料不存在");
        }
        if(material.getStock()<quantity){
            throw new BusinessException("库存不足，当前库存："+material.getStock());
        }
        int rows = materialMapper.deductStock(materialId,quantity);
        if(rows<=0){
            throw new BusinessException("出库失败");
        }
    }
}
