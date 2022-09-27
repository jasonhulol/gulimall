package com.bootstudy.gulimall.order.dao;

import com.bootstudy.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author hhd
 * @email 1017193846@qq.com
 * @date 2022-09-26 09:54:55
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
