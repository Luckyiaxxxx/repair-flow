package com.repair.service;

import com.repair.entity.OrderTransfer;

import java.util.List;

public interface OrderTransferService {

    /**
     * 维修工发起转单/协助申请
     * @param type 1-转单 2-申请协助
     */
    OrderTransfer requestTransfer(Integer orderId, Integer workerId, Integer type, String reason);

    /** 客服：待处理申请列表 */
    List<OrderTransfer> getPendingTransfers();

    /** 维修工：我的申请列表 */
    List<OrderTransfer> getMyTransfers(Integer workerId);

    /** 客服同意：转单->转派，协助->加派 */
    void approveTransfer(Integer transferId, Integer dispatcherId, Integer newWorkerId);

    /** 客服拒绝 */
    void rejectTransfer(Integer transferId, Integer dispatcherId, String note);
}