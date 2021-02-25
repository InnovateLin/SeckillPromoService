package com.miaoshaProject.service;

import com.miaoshaProject.error.BusinessException;
import com.miaoshaProject.service.model.OrderModel;

/**
 * @author zelin zhang
 * @create 2020-12-21 11:48 上午
 */
public interface OrderService {
    //使用1,通过前端url上传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已开始
    //2.直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException;
}
