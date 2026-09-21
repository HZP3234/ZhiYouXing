package com.zhiyouxing.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.entity.StoreupEntity;
import com.zhiyouxing.common.service.StoreupService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.NewsEntity;
import com.zhiyouxing.travel.entity.view.NewsView;
import com.zhiyouxing.travel.service.NewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 旅游资讯
 */
@RestController
@RequestMapping("/news")
public class NewsController {
    @Autowired
    private NewsService newsService;

    @Autowired
    private StoreupService storeUpService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,NewsEntity news,
		HttpServletRequest request){
        QueryWrapper<NewsEntity> ew = new QueryWrapper<NewsEntity>();

		PageUtils page = newsService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, news), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,NewsEntity news, 
		HttpServletRequest request){
        QueryWrapper<NewsEntity> ew = new QueryWrapper<NewsEntity>();

		PageUtils page = newsService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, news), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( NewsEntity news){
       	QueryWrapper<NewsEntity> ew = new QueryWrapper<NewsEntity>();
      	ew.allEq(MPUtil.allEQMapPre( news, "news")); 
        return R.ok().put("data", newsService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(NewsEntity news){
        QueryWrapper< NewsEntity> ew = new QueryWrapper< NewsEntity>();
 		ew.allEq(MPUtil.allEQMapPre( news, "news")); 
		NewsView newsView =  newsService.selectView(ew);
		return R.ok("查询旅游资讯成功").put("data", newsView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        NewsEntity news = newsService.getById(id);
		news.setClickNum(news.getClickNum()+1);
		news.setClickTime(new Date());
		newsService.updateById(news);
        news = newsService.selectView(new QueryWrapper<NewsEntity>().eq("id", id));
        return R.ok().put("data", news);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        NewsEntity news = newsService.getById(id);
		news.setClickNum(news.getClickNum()+1);
		news.setClickTime(new Date());
		newsService.updateById(news);
        news = newsService.selectView(new QueryWrapper<NewsEntity>().eq("id", id));
        return R.ok().put("data", news);
    }

    /**
     * 赞或踩
     */
    @RequestMapping("/thumbsup/{id}")
    public R vote(@PathVariable("id") String id,String type){
        NewsEntity news = newsService.getById(id);
        if(type.equals("1")) {
        	news.setThumbsUpNum(news.getThumbsUpNum()+1);
        } else {
        	news.setCrazilyNum(news.getCrazilyNum()+1);
        }
        newsService.updateById(news);
        return R.ok("投票成功");
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody NewsEntity news, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(news);
        newsService.save(news);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody NewsEntity news, HttpServletRequest request){
    	//ValidatorUtils.validateEntity(news);
        newsService.save(news);
        return R.ok();
    }

     /**
     * 获取用户密保
     */
    @RequestMapping("/security")
    @IgnoreAuth
    public R security(@RequestParam String username){
        NewsEntity news = newsService.getOne(new QueryWrapper<NewsEntity>().eq("", username));
        return R.ok().put("data", news);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody NewsEntity news, HttpServletRequest request){
        //ValidatorUtils.validateEntity(news);
        newsService.updateById(news);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        newsService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
    
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,NewsEntity news, HttpServletRequest request,String pre){
        QueryWrapper<NewsEntity> ew = new QueryWrapper<NewsEntity>();
        Map<String, Object> newMap = new HashMap<String, Object>();
        Map<String, Object> param = new HashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			String newKey = entry.getKey();
			if (pre.endsWith(".")) {
				newMap.put(pre + newKey, entry.getValue());
			} else if (StringUtils.isEmpty(pre)) {
				newMap.put(newKey, entry.getValue());
			} else {
				newMap.put(pre + "." + newKey, entry.getValue());
			}
		}
		params.put("sort", "click_num");
        params.put("order", "desc");
		PageUtils page = newsService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, news), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 协同算法（按收藏推荐）
     */
    @RequestMapping("/autoSort2")
    public R autoSort2(@RequestParam Map<String, Object> params,NewsEntity news, HttpServletRequest request){
        String userId = request.getSession().getAttribute("user_id").toString();
        String intelTypeColumn = "type_name";
        List<StoreupEntity> storeUps = storeUpService.list(new QueryWrapper<StoreupEntity>().eq("type", 1).eq("user_id", userId).eq("table_name", "news").orderBy(true, false, "add_time"));
        List<String> intelTypes = new ArrayList<String>();
        Integer limit = params.get("limit")==null?10:Integer.parseInt(params.get("limit").toString());
        List<NewsEntity> newsList = new ArrayList<NewsEntity>();
        //去重
        if(storeUps!=null && storeUps.size()>0) {
            for(StoreupEntity s : storeUps) {
                newsList.addAll(newsService.list(new QueryWrapper<NewsEntity>().eq(intelTypeColumn, s.getIntelType())));
            }
        }
        QueryWrapper<NewsEntity> ew = new QueryWrapper<NewsEntity>();
        params.put("sort", "id");
        params.put("order", "desc");
        PageUtils page = newsService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, news), params), params));
        List<NewsEntity> pageList = (List<NewsEntity>)page.getList();
        if(newsList.size()<limit) {
            int toAddNum = (limit-newsList.size())<=pageList.size()?(limit-newsList.size()):pageList.size();
            for(NewsEntity o1 : pageList) {
                boolean addFlag = true;
                for(NewsEntity o2 : newsList) {
                    if(o1.getId().intValue()==o2.getId().intValue()) {
                        addFlag = false;
                        break;
                    }
                }
                if(addFlag) {
                    newsList.add(o1);
                    if(--toAddNum==0) break;
                }
            }
        } else if(newsList.size()>limit) {
            newsList = newsList.subList(0, limit);
        }
        page.setList(newsList);
        return R.ok().put("data", page);
    }

}
