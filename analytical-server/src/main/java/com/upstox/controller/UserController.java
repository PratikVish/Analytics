package com.upstox.controller;

import com.upstox.model.UserSubscription;
import com.upstox.service.TradeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class UserController {

    @Autowired
    private TradeProcessor processor;

    @MessageMapping("/as.newUser")
    private UserSubscription addUser(@Payload UserSubscription subscription, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", subscription.getSymbol());
        log.debug("User added " + subscription.toString());
        processor.constructBarChartData();
        return subscription;
    }

}
