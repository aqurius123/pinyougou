package cn.itcast.core.controller;

import cn.itcast.core.service.SearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemsearch")
public class SearchController {

    @Reference
    SearchService searchService;
    /**
     * 关键字搜索
     * @return
     */
    @RequestMapping("/search")
    public Map<String, Object> search(@RequestBody Map<String,String> searchMap){
        return searchService.search(searchMap);
    }

}
