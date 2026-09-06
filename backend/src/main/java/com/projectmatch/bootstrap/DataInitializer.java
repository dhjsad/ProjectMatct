package com.projectmatch.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.entity.Article;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.ProjectResource;
import com.projectmatch.entity.SysUser;
import com.projectmatch.entity.UserProfile;
import com.projectmatch.mapper.ArticleMapper;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.ProjectResourceMapper;
import com.projectmatch.mapper.SysUserMapper;
import com.projectmatch.mapper.UserProfileMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final UserProfileMapper profileMapper;
    private final ProjectMapper projectMapper;
    private final ProjectResourceMapper resourceMapper;
    private final ArticleMapper articleMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper userMapper, UserProfileMapper profileMapper,
                           ProjectMapper projectMapper, ProjectResourceMapper resourceMapper,
                           ArticleMapper articleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.projectMapper = projectMapper;
        this.resourceMapper = resourceMapper;
        this.articleMapper = articleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()) == 0) {
            seedUsers();
        }
        ensureDemoUsers();
        if (projectMapper.selectCount(new LambdaQueryWrapper<Project>()) == 0) {
            seedProjects();
        }
        if (articleMapper.selectCount(new LambdaQueryWrapper<Article>()) == 0) {
            seedArticles();
        }
    }

    private void seedUsers() {
        SysUser admin = user("admin", "admin@projectmatch.local", "admin123", "ADMIN");
        SysUser student = user("student", "student@projectmatch.local", "student123", "USER");
        UserProfile profile = new UserProfile();
        profile.setUserId(student.getId());
        profile.setMajor("计算机科学与技术");
        profile.setSkillLevel("junior");
        profile.setTechStack("Java,Spring Boot,MyBatis,Vue,MySQL");
        profile.setInterests("校园、医疗");
        profile.setExpectedDifficulty("medium");
        profile.setExpectedDuration(40);
        profile.setBio("只会 Spring Boot CRUD，想做一个功能完整但不至于太难的毕业设计。");
        profileMapper.insert(profile);
    }

    private void ensureDemoUsers() {
        ensureUser("mei", "mei@projectmatch.local", "mei123", "USER",
                "软件工程", "junior", "Java,Spring Boot,MyBatis,Vue,MySQL",
                "医疗、养老", "medium", 40, "对医疗信息化感兴趣，希望做预约或档案类毕业设计。");
        ensureUser("yuan", "yuan@projectmatch.local", "yuan123", "USER",
                "数据科学", "junior", "Python,Flask,Pandas,ECharts",
                "数据分析、校园", "easy", 21, "不想做大型管理系统，更想做可视化分析。");
        ensureUser("xiao", "xiao@projectmatch.local", "xiao123", "USER",
                "计算机科学与技术", "beginner", "微信小程序,Java,Spring Boot",
                "校园、小程序", "easy", 16, "两周课程设计，只会一点小程序。");
    }

    private void ensureUser(String username, String email, String raw, String role,
                            String major, String skill, String stack, String interests,
                            String difficulty, int days, String bio) {
        SysUser exists = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (exists != null) {
            return;
        }
        SysUser created = user(username, email, raw, role);
        UserProfile profile = new UserProfile();
        profile.setUserId(created.getId());
        profile.setMajor(major);
        profile.setSkillLevel(skill);
        profile.setTechStack(stack);
        profile.setInterests(interests);
        profile.setExpectedDifficulty(difficulty);
        profile.setExpectedDuration(days);
        profile.setBio(bio);
        profileMapper.insert(profile);
    }

    private SysUser user(String username, String email, String raw, String role) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(raw));
        user.setRole(role);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    private void seedProjects() {
        add(project("宠物医院管理系统", "pet-hospital",
                "面向中小型宠物医院的预约、病历、药品和收费管理系统。模块完整，适合作为 Spring Boot + Vue 毕业设计，不涉及微服务。",
                "毕业设计", "Java", "Java,Spring Boot,MyBatis,Vue,MySQL", "medium", 40,
                "计算机本科、Java 初学者、会基础 Vue 的学生",
                "预约挂号,病历管理,药品库存,收费结算,医生排班,统计报表",
                "单体 Spring Boot 提供 REST 接口，Vue 管理端按业务模块拆分页面，MySQL 存储核心业务数据。",
                "用户、角色、宠物、预约、病历、药品、订单等表。宠物与主人一对多，病历关联预约与医生。",
                "导入 SQL 后启动后端，配置前端代理即可本地运行。",
                "GET /api/pets?ownerId=1 按主人查询宠物列表，体现基础 CRUD 与关联查询。",
                "建议先完成后台登录与宠物档案，再做预约与收费。答辩时重点讲预约冲突校验和库存扣减。",
                2));

        add(project("智慧养老管理系统", "smart-elderly-care",
                "社区养老场景下的老人档案、健康监测、护工派单和家属通知系统。业务完整，领取名额有限。",
                "毕业设计", "Java", "Java,Spring Boot,MyBatis,Vue,MySQL,Redis", "medium", 45,
                "希望做医疗/养老方向、已掌握 Spring Boot CRUD 的学生",
                "老人档案,健康打卡,护工排班,服务工单,家属端查询,数据统计",
                "Spring Boot 单体 + Vue 管理端。可用 Redis 缓存热点统计，非必须集群。",
                "老人、护工、工单、健康记录、家属绑定表。工单状态机：待派单-服务中-已完成。",
                "先完成后台，再视时间决定是否做简易家属查询页。",
                "工单派单接口按护工空闲状态分配，避免重复派给同一繁忙护工。",
                "学习重点是状态流转和角色权限，而不是堆砌页面数量。",
                1));

        add(project("学生选课与成绩管理系统", "course-grade",
                "经典教务场景：学期开课、学生选课、教师录入成绩、学分统计。周期短，适合课程设计。",
                "课程设计", "Java", "Java,SSM,JSP,MySQL", "easy", 20,
                "两到三周要交差、Java Web 基础一般的学生",
                "用户登录,课程维护,选课,退课,成绩录入,成绩单打印",
                "SSM 三层结构，页面以服务端渲染为主，降低前端门槛。",
                "学生、教师、课程、开课计划、选课记录、成绩表。选课记录唯一约束防止重复选课。",
                "部署 Tomcat 或直接 Spring Boot 内嵌均可。",
                "选课接口需校验容量与时间冲突。",
                "不要做成纯 CRUD：至少实现容量校验，答辩才站得住。",
                3));

        add(project("校园二手交易平台", "campus-secondhand",
                "学生发布闲置、在线沟通意向、管理员审核违规信息。前后端分离，功能丰富但技术点克制。",
                "毕业设计", "Java", "Java,Spring Boot,MyBatis,Vue,MySQL", "medium", 35,
                "会一点 Vue、想做校园业务的本科生",
                "商品发布,分类搜索,收藏,留言沟通,订单意向,举报审核",
                "Spring Boot REST + Vue 用户端。图片可用本地目录模拟对象存储。",
                "用户、商品、分类、收藏、留言、举报表。",
                "注意文件上传大小限制。",
                "商品列表支持分类 + 关键字分页查询。",
                "可把“审核流”作为技术说明点，避免做成淘宝缩小版却讲不清核心。",
                2));

        add(project("在线考试系统", "online-exam",
                "题库、组卷、限时作答与自动阅卷。逻辑比普通管理系统稍难，适合想在简历里写“有一点业务规则”的同学。",
                "求职项目", "Java", "Java,Spring Boot,MyBatis,Vue,MySQL", "medium", 30,
                "有一年以内 Java 经验、希望作品能写进简历",
                "题库管理,组卷策略,在线答题,自动阅卷,成绩分析",
                "后端控制考试会话与截止时间，前端倒计时只是展示。",
                "题目、试卷、试卷题目关联、考试记录、作答明细。",
                "建议用 JWT 区分教师与学生角色。",
                "提交试卷后根据题型计算客观题得分。",
                "答辩/面试时讲清楚“时间到自动交卷”和“防重复提交”。",
                2));

        add(project("图书借阅管理系统", "library",
                "馆藏、借还、超期与预约。难度低，适合两周课程设计或练手。",
                "课程设计", "Java", "Java,Spring Boot,MyBatis,Thymeleaf,MySQL", "easy", 18,
                "Java 初学者、先求能跑起来",
                "图书检索,借书,还书,超期提醒,预约",
                "Spring Boot + Thymeleaf，减少前端学习成本。",
                "图书、副本、借阅、预约、读者。",
                "一条 SQL 脚本即可初始化。",
                "还书时更新副本状态并计算是否超期。",
                "把超期规则写清楚，比多做几个菜单更有价值。",
                5));

        add(project("医疗预约管理系统", "medical-appointment",
                "科室、医生排班、患者预约与就诊记录。方向明确，和“医疗兴趣 + Spring Boot”高度匹配。",
                "毕业设计", "Java", "Java,Spring Boot,MyBatis,Vue,MySQL", "medium", 38,
                "对医疗信息化感兴趣的计算机专业学生",
                "科室管理,医生排班,号源预约,就诊记录,取消改约",
                "号源按日期和时段生成，预约时扣减余号。",
                "医院、科室、医生、排班、号源、预约、就诊记录。",
                "可用模拟数据演示早高峰抢号。",
                "预约接口必须在事务中扣减 remaining。",
                "这和平台自己的“限量领取”是同一类并发问题，适合写进设计说明。",
                2));

        add(project("流浪动物救助系统", "stray-animal",
                "救助上报、临时安置、领养审核与志愿活动。语义上接近宠物方向，但业务不是医院收费。",
                "毕业设计", "Java", "Java,Spring Boot,MyBatis,Vue,MySQL", "medium", 32,
                "想做公益/宠物相关但希望有故事性的选题",
                "救助上报,动物档案,领养申请,审核流,志愿活动",
                "标准前后端分离，审核流用状态字段即可。",
                "上报记录、动物、领养申请、志愿者、活动报名。",
                "图片上传可用本地存储。",
                "领养申请需管理员审核通过后才变更动物状态。",
                "适合讲清状态机：待救助-安置中-可领养-已领养。",
                2));

        add(project("Flask 校园消费数据分析", "campus-consume-analysis",
                "用 Flask 做数据清洗展示：食堂消费、借阅或模拟一卡通流水的可视化看板。",
                "课程设计", "Python", "Python,Flask,Pandas,ECharts,MySQL", "easy", 21,
                "Python 方向、不想做大型管理系统的学生",
                "数据导入,清洗,多维统计,可视化看板,简要结论",
                "Flask 提供页面与 JSON 接口，前端用 ECharts。",
                "流水事实表 + 维度表（窗口、时段、学院）。",
                "可用 CSV 作为数据源降低环境要求。",
                "按小时聚合消费金额接口。",
                "重点是分析结论，而不是再写一套用户权限系统。",
                3));

        add(project("机器学习房价预测实验平台", "house-price-ml",
                "数据预处理、模型训练对比与预测可视化。适合人工智能方向课程设计，不是 Web CRUD。",
                "课程设计", "人工智能", "Python,Scikit-learn,Flask,Pandas", "medium", 28,
                "机器学习入门、有 Python 基础",
                "数据集管理,特征处理,模型训练,指标对比,单条预测",
                "训练在后端离线完成，Web 只做实验记录和预测。",
                "实验任务、参数、指标、预测日志。",
                "注明使用公开数据集，避免隐私数据。",
                "暴露 /predict 接收特征并返回预测价格。",
                "报告里写清特征含义和过拟合处理，比调参玄学更重要。",
                2));

        add(project("微信小程序校园导航", "campus-miniprogram-map",
                "建筑物检索、路线说明与活动公告。前端以小程序为主，后端可以很薄。",
                "课程设计", "移动开发", "微信小程序,Java,Spring Boot,MySQL", "easy", 16,
                "只会一点小程序、两周要交差",
                "地点检索,分类导览,路线说明,公告",
                "小程序负责展示，Spring Boot 提供地点 JSON。",
                "地点、分类、公告。",
                "可用模拟坐标，不必真接地图商用 Key。",
                "按关键字返回地点列表。",
                "把信息架构做清楚，避免做成无数据的地图壳。",
                4));

        add(project("Spring Cloud 微服务商城", "microservice-mall",
                "用户、商品、订单拆分服务。技术点多、联调成本高，不建议时间紧或只会 CRUD 的同学作为毕设主项目。",
                "求职项目", "Java", "Java,Spring Cloud,Nacos,Gateway,MyBatis,Vue,MySQL", "hard", 70,
                "已有扎实 Java 基础、想挑战微服务的同学",
                "用户服务,商品服务,订单服务,网关鉴权,基础秒杀演示",
                "按业务拆分服务，经网关对外。",
                "服务各自独立库或分表，订单依赖商品库存。",
                "本地需同时启动注册中心与多个服务。",
                "下单时通过 Feign 扣减库存。",
                "若答辩时间不足，优先证明你理解拆分边界，而不是复制一套未跑通的框架。",
                1));
    }

    private Project project(String name, String slug, String desc, String type, String category,
                            String stack, String difficulty, int days, String suitable, String modules,
                            String architecture, String db, String deploy, String sample, String tutorial, int remaining) {
        Project p = new Project();
        p.setName(name);
        p.setSlug(slug);
        p.setDescription(desc);
        p.setProjectType(type);
        p.setCategory(category);
        p.setTechStack(stack);
        p.setDifficulty(difficulty);
        p.setEstimatedDuration(days);
        p.setSuitableFor(suitable);
        p.setModules(modules);
        p.setArchitecture(architecture);
        p.setDbDesign(db);
        p.setDeployGuide(deploy);
        p.setSampleCode(sample);
        p.setTutorial(tutorial);
        p.setRemainingCount(remaining);
        p.setDeployPrice(199);
        p.setDeployServiceEnabled(1);
        p.setStatus("PUBLISHED");
        p.setCreateTime(LocalDateTime.now());
        return p;
    }

    private void add(Project project) {
        projectMapper.insert(project);
        resource("PUBLIC", "功能模块说明", project.getModules(), project.getId());
        resource("PUBLIC", "开发教程提纲", project.getTutorial(), project.getId());
        resource("CLAIMED", "学习用库表脚本说明", "领取后可查看建表思路与示例字段。请自行实现，不要把示例当作可提交的成品源码。", project.getId());
        resource("CLAIMED", "部署与二次开发建议", project.getDeployGuide() + "\n完成基础功能后，建议增加权限、校验和自己的业务规则。", project.getId());
    }

    private void resource(String access, String title, String content, Long projectId) {
        ProjectResource resource = new ProjectResource();
        resource.setProjectId(projectId);
        resource.setResourceType("DOC");
        resource.setTitle(title);
        resource.setContent(content);
        resource.setAccessType(access);
        resourceMapper.insert(resource);
    }

    private void seedArticles() {
        article("Spring Boot 毕设选题：怎么判断项目适不适合你", "spring-boot-topic",
                "选题", "先看技术栈重合度和周期，再看模块是否讲得清，而不是先看界面好不好看。");
        article("Java 毕设项目推荐：CRUD 之后还可以做什么", "java-crud-next",
                "Java", "在熟悉的 Spring Boot 上增加状态机、权限和并发约束，比盲目上微服务更稳。");
        article("计算机专业毕业设计选题避坑", "cs-thesis-pitfalls",
                "选题", "不要承诺“下载即可提交”。评审看的是你能否讲清设计和自己改过什么。");
    }

    private void article(String title, String slug, String keywords, String summary) {
        Article article = new Article();
        article.setTitle(title);
        article.setSlug(slug);
        article.setKeywords(keywords);
        article.setSummary(summary);
        article.setContent(summary + "\n\n毕设工坊把项目当作学习样本：公开页保留介绍、架构和教程，完整学习资料采用限量领取，并要求用户自行实现与修改。");
        article.setCreateTime(LocalDateTime.now());
        articleMapper.insert(article);
    }
}
