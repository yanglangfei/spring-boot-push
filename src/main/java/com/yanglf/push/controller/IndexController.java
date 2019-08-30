package com.yanglf.push.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@Controller
@RequestMapping("/web")
public class IndexController {


    @RequestMapping("/sse")
    public String index() {
        return "index";
    }


    @RequestMapping("/socket")
    public String socket() {
        return "socket";
    }
}
