package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);


    /**
     * 根据订单号获取整个订单
     * @param outTradeNo
     * @return
     */
    @Select("select * from sky_take_out.orders where number = #{outTradeNo} and user_id = #{userId}")
    Orders getByNumberAndUserId(String outTradeNo, Long userId);


    /**
     * 更新订单状态
     * @param orders
     */
    void update(Orders orders);


    /**
     * 历史订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     * 根据订单id查询
     * @param id
     * @return
     */
    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getById(Long id);


    /**
     * 各订单状态数量统计
     * @return
     */
    @Select("select count(id) from sky_take_out.orders where status = #{status}")
    Integer countStatus(Integer status);


    /**
     * 根据订单状态查询时间
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);


    /**
     * 动态返回对应营业额数据
     * @param map
     * @return
     */
    Double sumByMap(Map map);


    /**
     * 获取每日的新增订单数量
     * @param map
     * @return
     */
    Integer getOrderCount(Map map);


    /**
     * 获取总的有效订单数
     * @param status
     * @return
     */
    @Select("select count(id) from sky_take_out.orders where status = #{status}")
    Integer getVaildOrder(Integer status);


    /**
     * 获取总的订单数
     * @return
     */
    @Select("select count(id) from sky_take_out.orders")
    Integer getTotalOrder();


    /**
     * 指定时间区间内销量前十
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

}
