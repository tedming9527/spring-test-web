package org.example.springtestweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA catch-all：所有非 /api/** 的路径都返回 index.html，
 * 由 React Router 在客户端处理路由。
 */
@Controller
public class SpaController {

    @RequestMapping(value = {"/", "/{path:[^\\.]*}", "/{path:[^\\.]*}/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
