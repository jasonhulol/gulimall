package com.bootstudy.search.service;

import com.bootstudy.search.vo.SearchParam;
import com.bootstudy.search.vo.SearchResponse;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/4 6:51 下午
 * @Version 1.0
 */
public interface EsSearchService {
    SearchResponse buildSearch(SearchParam searchParam);
}
