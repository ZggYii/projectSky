package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 自定义定时任务类
 */
@Component
@Slf4j
public class MyTask {

    /**
     * 超时订单处理办法
     */
    // @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("nihao");
    }
}
