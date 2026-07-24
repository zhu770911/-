package com.smartparking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
