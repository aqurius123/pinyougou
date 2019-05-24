package cn.itcast.core.service;


import cn.itcast.core.pojo.item.Item;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;


@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    SolrTemplate solrTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 开始搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String,String> searchMap) {

        Map<String, Object> resultMap = new HashMap<>();
        //1:商品分类结果集
        List<String> categoryListByKeywords = findCategoryListByKeywords(searchMap);
        resultMap.put("categoryList", categoryListByKeywords);

        if(null != categoryListByKeywords && categoryListByKeywords.size() >0){

            Object typeId = redisTemplate.boundHashOps("itemCat").get(categoryListByKeywords.get(0));
            //2:品牌结果集
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            //3:规格结果集
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);

            resultMap.put("brandList",brandList);
            resultMap.put("specList",specList);
        }
        //4:查询结果集 总条数 总页数
        resultMap.putAll(search2(searchMap));
        //查询高亮结果集
        return resultMap;
    }

    /*
        根据关键词查询商品分类
        分析：（1）搜索面板的商品分类需要使用Spring Data Solr的分组查询来实现
     */
    public List<String> findCategoryListByKeywords(Map<String,String> searchMap){
        //获取关键词
//        $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
        List<String> list = new ArrayList<>();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query query = new SimpleQuery(criteria);
        /*
            设置分组选项，使用框架自带api方法，
            之所以无法用SQL语句来处理的原因是因为solr索引库不能直接操作mysql数据库
         */
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        for (GroupEntry<Item> entry : content) {
            list.add(entry.getGroupValue());
        }
        return list;
    }

    //关键字普通查询
    public Map<String, Object> search1(Map<String,String> searchMap) {
        Map<String,Object> map = new HashMap<>();
        //设置条件搜索
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query query = new SimpleQuery(criteria);

//        定义搜索对象的结构  category:商品分类
//        $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
//        获取当前页
        int pageNo = Integer.parseInt(searchMap.get("pageNo"));
        //获取没有数据条数
        int pageSize = Integer.parseInt(searchMap.get("pageSize"));
        //设置起始索引
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        ScoredPage<Item> items = solrTemplate.queryForPage(query, Item.class);
        //获取结果集
        List<Item> itemList = items.getContent();
        map.put("rows", itemList);
        //查询数据总条数
        map.put("total",items.getTotalElements());
        //查询数据总页数
        map.put("totalPages",items.getTotalPages());
        return map;
    }

    //关键字高亮查询
    public Map<String, Object> search2(Map<String,String> searchMap) {
        /**
         * 处理关键词是否带空格
         */
        searchMap.put("keywords",searchMap.get("keywords").replaceAll(" ",""));


        Map<String,Object> map = new HashMap<>();
        //关键词
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));


        HighlightQuery highlightQuery = new SimpleHighlightQuery(criteria);

        //过滤查询 $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
            //1.分类查询
        if (null != searchMap.get("category") && !"".equals(searchMap.get("category"))){//使用框架自带的分类过滤
            FilterQuery filterQuery = new SimpleFilterQuery();
            filterQuery.addCriteria(new Criteria("item_category").is(searchMap.get("category")));
            highlightQuery.addFilterQuery(filterQuery);
        }
            //2.品牌过滤
        if (null != searchMap.get("brand") && !"".equals(searchMap.get("brand"))){//使用框架自带的分类过滤
            FilterQuery filterQuery = new SimpleFilterQuery();
            filterQuery.addCriteria(new Criteria("item_brand").is(searchMap.get("brand")));
            highlightQuery.addFilterQuery(filterQuery);
        }
            //3.规格过滤spec':{"item_spec_网络": "联通3G","item_spec_机身内存": "16G"},
        if (null != searchMap.get("spec") && !"".equals(searchMap.get("spec"))){//使用框架自带的分类过滤
            //获取规格项
            Map<String,String> specMap = JSON.parseObject(searchMap.get("spec"), Map.class);
            FilterQuery filterQuery = new SimpleFilterQuery();
            //遍历map集合
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                //添加
                filterQuery.addCriteria(new Criteria("item_spec_" + entry.getKey()).is(entry.getValue()));
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

            //4.价格过滤
        if (null != searchMap.get("price") && !"".equals(searchMap.get("price"))){
            String minMaxPrice = searchMap.get("price");
            String[] price = minMaxPrice.split("-");
            FilterQuery filterQuery = new SimpleFilterQuery();
            //判断价格是否大于3000
            if ("*".equals(price[1])){
                filterQuery.addCriteria(new Criteria("item_price").greaterThanEqual(price[0]));
            }else {
                filterQuery.addCriteria(new Criteria("item_price").between(price[0],price[1],true,true));

            }

            highlightQuery.addFilterQuery(filterQuery);
        }
        //排序{'sort':'ASC或DESC','sortField':'price或updatetime'}
        if (null != searchMap.get("sort") && !"".equals(searchMap.get("sort"))){
            if ("DESC".equals(searchMap.get("sort"))){
                highlightQuery.addSort(new Sort(Sort.Direction.DESC, "item_" + searchMap.get("sortField")));
            }else {
                highlightQuery.addSort(new Sort(Sort.Direction.ASC, "item_" + searchMap.get("sortField")));
            }
        }

        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        highlightQuery.setOffset((Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
        highlightQuery.setRows(Integer.parseInt(pageSize));

        //高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置高亮的域
        highlightOptions.addField("item_title");
        //前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //后缀
        highlightOptions.setSimplePostfix("</em>");

        highlightQuery.setHighlightOptions(highlightOptions);

        //执行查询
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(highlightQuery, Item.class);

        //高亮的数据  entity=Item对象的值
        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {

            //Item对象的值
            Item entity = itemHighlightEntry.getEntity();


            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();

            if(null != highlights && highlights.size() >0){
                //有高亮名称
                entity.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }


        //分页结果集
        List<Item> content = page.getContent();

        map.put("rows", content);
        //查询总条数
        map.put("total", page.getTotalElements());
        //总页数
        map.put("totalPages",page.getTotalPages());
        return map;
    }

}
