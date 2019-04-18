package com.kunfei.bookshelf.web.controller;

import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping(path = "/")
    public String index() {
        // Equivalent to [return "/index"].
        return "forward:/index.html";
    }
}
