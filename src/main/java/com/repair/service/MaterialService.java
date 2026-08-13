package com.repair.service;


import com.repair.entity.Material;

import java.util.List;

public interface MaterialService {
    /**
     * 新增物料
     * @param material 物料信息
     * @return 新增成功的物料
     */
    Material addMaterial(Material material);

    /**
     * 删除物料
     * @param id  物料ID
     */
    void deleteMaterial(Integer id);

    /**
     * 更新物料信息
     * @param material 物料信息
     * @return 更新后的物料信息
     */
    Material updateMaterial(Material material);

    /**
     * 根据ID   查询物料
     * @param id  物料ID
     * @return  物料信息
     */
    Material getMaterialById(Integer id);

    /**
     * 查询所有物料
     * @return 物料列表
     */
    List<Material> listAllMaterials();

    /**
     * 查询库存不足的物料(库存<最低库存预警值)
     * @return 库存不足的物料列表
     */
    List<Material> getLowStockMaterials();

    /**
     * 入库
     * @param materialId 物料Id
     * @param quantity  入库数量
     */
    void addStock(Integer materialId,Integer quantity);


    /**
     * 出库
     * @param materiaId 物料ID
     * @param quantity  出库数量
     */
    void deductStock(Integer materiaId,Integer quantity);
}