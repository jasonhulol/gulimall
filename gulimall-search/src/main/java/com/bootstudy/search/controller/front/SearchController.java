package com.bootstudy.search.controller.front;

import com.bootstudy.search.service.EsSearchService;
import com.bootstudy.search.vo.SearchParam;
import com.bootstudy.search.vo.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/4 6:47 下午
 * @Version 1.0
 */
@Controller
@Slf4j
public class SearchController {

    @Autowired
    EsSearchService searchService;

    @RequestMapping("/search.html")
    public String index(SearchParam searchParam, Model model, HttpServletRequest request) {
        SearchResponse response = searchService.buildSearch(searchParam);
        model.addAttribute("result", response);
        return "search";
    }
}
