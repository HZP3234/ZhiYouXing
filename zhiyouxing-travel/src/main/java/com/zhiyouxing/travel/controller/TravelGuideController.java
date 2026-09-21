package com.zhiyouxing.travel.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.service.StoreupService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.dao.UserNameDao;
import com.zhiyouxing.travel.entity.TravelGuideEntity;
import com.zhiyouxing.travel.entity.model.TravelGuideForm;
import com.zhiyouxing.travel.entity.view.TravelGuideView;
import com.zhiyouxing.travel.service.TravelGuideService;
import com.zhiyouxing.travel.service.TravelGuideTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 旅游攻略
 *
 * <p>本轮把它从「代码生成器产物」改成了真的写作/查阅接口，三条规矩：
 * <ol>
 *   <li><b>浏览公开、写作要登录。</b> /search 与 /detail/{id} 是 @IgnoreAuth；
 *       /save /add /update /delete 不带注解，由 AuthorizationInterceptor 强制要 Token。</li>
 *   <li><b>作者与时间一律服务端钉死。</b> 写入收的是 TravelGuideForm，那个 DTO 里
 *       根本装不下 userAccount / userName / addTime / 四个计数器，
 *       请求体塞进来会被 Jackson 直接丢弃（下面仍然显式再写一次，双保险）。</li>
 *   <li><b>只能改自己的。</b> /update 与 /delete 都会比对库里那行的 user_account。
 *       注意这不影响管理端：AdminCrud.vue 走的是 /api/tour_guide/travel_guide/**，
 *       命中的是 TourGuideScopeController 自带的同名前缀接口（那里刻意不限归属），
 *       根本不经过本类。</li>
 * </ol>
 */
@RestController
@RequestMapping("/travel_guide")
public class TravelGuideController {
    @Autowired
    private TravelGuideService travelGuideService;

    @Autowired
    private TravelGuideTagService travelGuideTagService;

    @Autowired
    private UserNameDao userNameDao;

    @Autowired
    private StoreupService storeUpService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TravelGuideEntity travelGuide,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("table_name").toString();
		if(tableName.equals("user")) {
			travelGuide.setUserAccount((String)request.getSession().getAttribute("username"));
		}
        QueryWrapper<TravelGuideEntity> ew = new QueryWrapper<TravelGuideEntity>();

		PageUtils page = travelGuideService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelGuide), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TravelGuideEntity travelGuide, 
		HttpServletRequest request){
        QueryWrapper<TravelGuideEntity> ew = new QueryWrapper<TravelGuideEntity>();

		PageUtils page = travelGuideService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelGuide), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 列表页的入口：按标签 / 关键词（标题或正文）搜攻略，并把每篇的标签一起带回去。
     *
     * <p>为什么不复用 /list：tagId 和 keyword 都不是 travel_guide 的实体字段，
     * 落到 /list 上会被 MPUtil 静默忽略（它只认实体字段名）。
     *
     * <p>返回信封与 /list 逐字一致（PageUtils：list / totalCount / pageSize / totalPage / currPage），
     * 前端两条路径的解析代码不用分叉。
     *
     * <p>@IgnoreAuth：没登录也能翻攻略。
     */
    @IgnoreAuth
    @RequestMapping("/search")
    public R search(@RequestParam Map<String, Object> params, TravelGuideEntity travelGuide) {
        QueryWrapper<TravelGuideEntity> ew = new QueryWrapper<TravelGuideEntity>();

        Object tagIdRaw = params.get("tagId");
        if (tagIdRaw != null && !tagIdRaw.toString().trim().isEmpty()) {
            List<Long> guideIds = travelGuideTagService.selectGuideIdsByTag(Long.valueOf(tagIdRaw.toString().trim()));
            if (guideIds.isEmpty()) {
                // 必须在这里就返回：MyBatis-Plus 的 in() 不做空判，空集合会拼出
                // `id IN ()`，MySQL 直接报语法错（不是「查不到」而是 500）。
                Page<TravelGuideView> emptyPage = new Query<TravelGuideView>(params).getPage();
                return R.ok().put("data", new PageUtils(Collections.emptyList(), 0,
                        (int) emptyPage.getSize(), (int) emptyPage.getCurrent()));
            }
            ew.in("id", guideIds);
        }

        Object keywordRaw = params.get("keyword");
        if (keywordRaw != null && !keywordRaw.toString().trim().isEmpty()) {
            String keyword = keywordRaw.toString().trim();
            // and(...) 会给整组套一层括号，标题 / 正文的 OR 才不会被别的条件切开
            ew.and(w -> w.like("guide_title", keyword).or().like("guide_detail", keyword));
        }

        PageUtils page = travelGuideService.queryPage(params,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, travelGuide), params), params));
        if (page.getList() != null) {
            @SuppressWarnings("unchecked")
            List<TravelGuideView> guides = (List<TravelGuideView>) page.getList();
            travelGuideTagService.attachTagNames(guides);
        }
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TravelGuideEntity travelGuide){
       	QueryWrapper<TravelGuideEntity> ew = new QueryWrapper<TravelGuideEntity>();
      	ew.allEq(MPUtil.allEQMapPre( travelGuide, "travel_guide")); 
        return R.ok().put("data", travelGuideService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TravelGuideEntity travelGuide){
        QueryWrapper< TravelGuideEntity> ew = new QueryWrapper< TravelGuideEntity>();
 		ew.allEq(MPUtil.allEQMapPre( travelGuide, "travel_guide")); 
		TravelGuideView travelGuideView =  travelGuideService.selectView(ew);
		return R.ok("查询旅游攻略成功").put("data", travelGuideView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TravelGuideEntity travelGuide = travelGuideService.getById(id);
        return R.ok().put("data", travelGuide);
    }

    /**
     * 前端详情
     *
     * <p>返回 View 而不是实体，只为了多带一个 tagNames（详情页要那排可点的标签）。
     * 别的字段与实体逐一对应，前端拿到的形状跟以前一样。
     *
     * <p>@IgnoreAuth：攻略是公开内容，没登录也能读。
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TravelGuideEntity travelGuide = travelGuideService.getById(id);
        if (travelGuide == null) {
            return R.error("攻略不存在");
        }
        TravelGuideView view = new TravelGuideView(travelGuide);
        view.setTagNames(travelGuideTagService.selectTagNamesByGuide(id));
        return R.ok().put("data", view);
    }

    /**
     * 赞或踩
     */
    @RequestMapping("/thumbsup/{id}")
    public R vote(@PathVariable("id") String id,String type){
        TravelGuideEntity travelGuide = travelGuideService.getById(id);
        if(type.equals("1")) {
        	travelGuide.setThumbsUpNum(travelGuide.getThumbsUpNum()+1);
        } else {
        	travelGuide.setCrazilyNum(travelGuide.getCrazilyNum()+1);
        }
        travelGuideService.updateById(travelGuide);
        return R.ok("投票成功");
    }

    /**
     * 后端保存（写作页发布走这里）
     *
     * <p>注意参数从 TravelGuideEntity 换成了 TravelGuideForm：那个 DTO 里没有
     * 作者、时间、计数器这几个字段，所以「客户端伪造作者」在结构上就不可能。
     */
    @RequestMapping("/save")
    public R save(@RequestBody TravelGuideForm form, HttpServletRequest request){
        return createGuide(form, request);
    }

    /**
     * 前端保存。与 /save 同一个实现，代码生成器的两套入口在这里合流。
     */
    @RequestMapping("/add")
    public R add(@RequestBody TravelGuideForm form, HttpServletRequest request){
        return createGuide(form, request);
    }

    /**
     * 发布一篇新攻略。
     *
     * <p>服务端在这里做四件事，顺序不能换：
     * <ol>
     *   <li>从 session 取登录账号（AuthorizationInterceptor 已经校验过 Token，
     *       能走到这里就说明头部是有效的；取不到只可能是会话过期）。</li>
     *   <li>校验标题与正文 —— 前端也校验，但接口不能依赖前端。</li>
     *   <li>先把标签名单清洗并卡上限。**必须在落库之前做**：否则会出现
     *       「攻略已经存进去了、标签被拒了」这种半截状态，作者还得自己回去删。</li>
     *   <li>再写主表，最后挂标签。作者昵称从 user 表解析后冗余落库
     *       （口径同 ConsumptionController.comment，取不到就回落账号）。</li>
     * </ol>
     *
     * <p>addTime 显式置 null 让库里的默认值生效，避免前端塞一个时间进来。
     */
    private R createGuide(TravelGuideForm form, HttpServletRequest request) {
        String account = sessionAccount(request);
        if (account == null) {
            return R.error(401, "请先登录");
        }
        if (form == null || StrUtil.isBlank(form.getGuideTitle())) {
            return R.error("请填写攻略标题");
        }
        if (StrUtil.isBlank(form.getGuideDetail())) {
            return R.error("请填写攻略内容");
        }
        List<String> tagNames = travelGuideTagService.normalizeTagNames(form.getTagNames());
        if (tagNames != null && tagNames.size() > TravelGuideTagService.MAX_TAGS_PER_GUIDE) {
            return R.error("一篇攻略最多 " + TravelGuideTagService.MAX_TAGS_PER_GUIDE + " 个标签");
        }

        TravelGuideEntity entity = new TravelGuideEntity();
        BeanUtils.copyProperties(form, entity);
        entity.setId(null);
        entity.setUserAccount(account);
        entity.setUserName(authorName(account));
        entity.setAddTime(null);
        travelGuideService.save(entity);

        travelGuideTagService.replaceGuideTags(entity.getId(), tagNames);

        Map<String, Object> data = new HashMap<>();
        data.put("id", entity.getId());
        return R.ok().put("data", data);
    }

    /**
     * 修改
     *
     * <p>只能改自己发布的：比对的是**库里那一行**的 user_account，不是请求体里的
     * （请求体里根本没有这个字段）。改的时候作者与昵称再钉一次，时间不动。
     *
     * <p>MP 的 updateById 默认只更新非 null 字段，所以前端想清空某个可选框
     * 要发空串 '' 而不是 undefined —— 写作页统一把空值发成 ''。
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody TravelGuideForm form, HttpServletRequest request){
        String account = sessionAccount(request);
        if (account == null) {
            return R.error(401, "请先登录");
        }
        if (form == null || form.getId() == null) {
            return R.error("参数不完整");
        }
        TravelGuideEntity existing = travelGuideService.getById(form.getId());
        if (existing == null) {
            return R.error("攻略不存在");
        }
        if (!account.equals(existing.getUserAccount())) {
            return R.error("只能修改自己发布的攻略");
        }
        if (StrUtil.isBlank(form.getGuideTitle())) {
            return R.error("请填写攻略标题");
        }
        if (StrUtil.isBlank(form.getGuideDetail())) {
            return R.error("请填写攻略内容");
        }
        List<String> tagNames = travelGuideTagService.normalizeTagNames(form.getTagNames());
        if (tagNames != null && tagNames.size() > TravelGuideTagService.MAX_TAGS_PER_GUIDE) {
            return R.error("一篇攻略最多 " + TravelGuideTagService.MAX_TAGS_PER_GUIDE + " 个标签");
        }

        TravelGuideEntity entity = new TravelGuideEntity();
        BeanUtils.copyProperties(form, entity);
        entity.setUserAccount(account);
        entity.setUserName(authorName(account));
        travelGuideService.updateById(entity);

        travelGuideTagService.replaceGuideTags(form.getId(), tagNames);
        return R.ok();
    }


    /**
     * 删除
     *
     * <p>整批校验、整批通过才删：只要有一篇不是自己的，这一批一篇都不动
     * （不做「能删的删掉、不能删的跳过」，那样前端拿到 200 会以为全删了）。
     */
    @RequestMapping("/delete")
    @Transactional
    public R delete(@RequestBody Long[] ids, HttpServletRequest request){
        String account = sessionAccount(request);
        if (account == null) {
            return R.error(401, "请先登录");
        }
        if (ids == null || ids.length == 0) {
            return R.error("参数不完整");
        }
        List<Long> idList = Arrays.asList(ids);
        List<TravelGuideEntity> guides = travelGuideService.listByIds(idList);
        if (guides == null || guides.isEmpty()) {
            return R.error("攻略不存在");
        }
        for (TravelGuideEntity guide : guides) {
            if (!account.equals(guide.getUserAccount())) {
                return R.error("只能删除自己发布的攻略");
            }
        }
        travelGuideService.removeByIds(idList);
        // 关联行跟着攻略一起走，别在关联表里留孤儿
        travelGuideTagService.removeByGuideIds(idList);
        return R.ok();
    }

    /**
     * 会话里的登录账号，没登录返回 null。
     *
     * <p>这几个 key 是 AuthorizationInterceptor 校验 Token 时写进去的，
     * 所以这里取不到就等于「没登录或会话过期」。
     */
    private String sessionAccount(HttpServletRequest request) {
        Object username = request.getSession().getAttribute("username");
        if (username == null || StrUtil.isBlank(username.toString())) {
            return null;
        }
        return username.toString();
    }

    /**
     * 作者昵称：从 user 表按账号查，查不到就回落成账号本身
     * （页面上显示手机号总比显示一片空白强）。
     */
    private String authorName(String account) {
        String name = userNameDao.selectUserNameByAccount(account);
        return StrUtil.isBlank(name) ? account : name;
    }

}
