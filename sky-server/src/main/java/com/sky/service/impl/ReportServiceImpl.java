package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        // 当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while(!begin.equals(end)) {
            // 计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 日期对应的营业额，状态“已完成”的订单的金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }


    /**
     * 用户数量统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        // dataList的制作
        // 当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while(!begin.equals(end)) {
            // 计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        // newUserList的制作
        List<Integer> newUserList = new ArrayList<>();
        // select count(id) from user where create_time < ? && create_time > ?
        for (LocalDate date : dateList) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer dateUserNumber = userMapper.getBetweenCreateTime(startTime, endTime);
            dateUserNumber = dateUserNumber == null ? 0 : dateUserNumber;
            newUserList.add(dateUserNumber);
        }

        // totalUserList的制作
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer dateUserNumberTotal = userMapper.getBeforeCreateTime(endTime);
            dateUserNumberTotal = dateUserNumberTotal == null ? 0 : dateUserNumberTotal;
            totalUserList.add(dateUserNumberTotal);
        }

        UserReportVO reportVO = UserReportVO.builder()
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .dateList(StringUtils.join(dateList,","))
                .build();

        return reportVO;
    }



    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {

        // 日期
        // dataList的制作
        // 当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while(!begin.equals(end)) {
            // 计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        // 每日有效订单
        List<Integer> vaildOrdersNumber = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map1 = new HashMap();
            map1.put("start", startTime);
            map1.put("end", endTime);
            map1.put("status", Orders.COMPLETED);
            Integer newOrdersNumber = orderMapper.getOrderCount(map1);
            newOrdersNumber = newOrdersNumber == null ? 0 : newOrdersNumber;
            vaildOrdersNumber.add(newOrdersNumber);
        }

        // 当前订单总数
        List<Integer> totalOrderNumber = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map2 = new HashMap();
            map2.put("start", startTime);
            map2.put("end", endTime);
            Integer OrdersNumber = orderMapper.getOrderCount(map2);
            OrdersNumber = OrdersNumber == null ? 0 : OrdersNumber;
            totalOrderNumber.add(OrdersNumber);
        }

        // 有效订单总数
        Integer vaildTotal = orderMapper.getVaildOrder(Orders.COMPLETED);

        // 总订单数
        Integer total = orderMapper.getTotalOrder();

        Double completedRate = 0.0;
        if(total != 0) {
            completedRate = vaildTotal.doubleValue() / total.doubleValue();
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(totalOrderNumber,","))
                .validOrderCountList(StringUtils.join(vaildOrdersNumber,","))
                .validOrderCount(vaildTotal)
                .totalOrderCount(total)
                .orderCompletionRate(completedRate)
                .build();
    }


    /**
     * 指定时间区间内销量前十
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime startTime = LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10List = orderMapper.getSalesTop10(startTime,endTime);

        // 把top10的菜品都放到一个list集合中去
        List<String> names = salesTop10List.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10List.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}
