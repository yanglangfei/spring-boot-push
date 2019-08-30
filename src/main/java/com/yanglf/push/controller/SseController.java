package com.yanglf.push.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@RestController
@RequestMapping("/sse")
public class SseController {

    private Map<Long, SseEmitter> sseEmitterMap = new HashMap<>();

    @RequestMapping(value = "/event/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handle(@PathVariable Long userId) {
        SseEmitter sseEmitter = new SseEmitter();
        this.sseEmitterMap.put(userId, sseEmitter);
        return sseEmitter;
    }


    @RequestMapping(value = "/send/{userId}", method = RequestMethod.GET)
    public String sendMsg(@PathVariable Long userId) {
        try {
            SseEmitter sseEmitter = this.sseEmitterMap.get(userId);
            sseEmitter.send("hello world" + userId);
            sseEmitter.complete();
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

}
