

package com.enhinck.demo.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>消息中心接口
 *
 * @author xiaomi（huenbin）
 * @since 5/8/21 3:26 PM
 */
@RestController
@Slf4j
public class MessageCenterController {


    /**
     * 消息列表v1.0
     *
     * @return
     */
    @GetMapping("/test")
    public String msgCenterPage() {
        log.info("test log");
        return "111";
    }

}