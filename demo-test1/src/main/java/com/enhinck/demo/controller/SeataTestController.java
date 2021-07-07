package com.enhinck.demo.controller;


import com.enhinck.demo.service.OrderService;
import com.enhinck.demo.service.TransformService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * <p>消息中心接口
 *
 * @author xiaomi（huenbin）
 * @since 5/8/21 3:26 PM
 */
@Api("Seata测试")
@RestController
@Slf4j
public class SeataTestController {

    @Resource
    OrderService orderService;
    @Resource
    TransformService transformService;

    @ApiOperation("Seata AT模式")
    @PostMapping("/atTest")
    public String saveOrder(@RequestParam String param) {
        orderService.save(param);
        return "success";
    }
    @ApiOperation("Seata TCC模式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "src",value = "源账号",required = true,dataType = "long",type = "query"),
            @ApiImplicitParam(name = "dest",value = "目标账号",required = true,dataType = "long",type = "query"),
            @ApiImplicitParam(name = "amount",value = "金额（元）",required = true,dataType = "int",type = "query")
    })
    @PostMapping("/tccTest")
    public Boolean transform(@RequestParam("src") Long src, @RequestParam("dest") Long dest, @RequestParam("amount") Integer amount) {
        return transformService.transform(src, dest, amount);
    }

}