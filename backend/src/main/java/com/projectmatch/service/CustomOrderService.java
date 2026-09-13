package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.common.BizException;
import com.projectmatch.dto.CustomOrderRequest;
import com.projectmatch.entity.CustomOrder;
import com.projectmatch.entity.SiteMessage;
import com.projectmatch.entity.SysUser;
import com.projectmatch.mapper.CustomOrderMapper;
import com.projectmatch.mapper.SiteMessageMapper;
import com.projectmatch.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class CustomOrderService {

    private final CustomOrderMapper orderMapper;
    private final SiteMessageMapper messageMapper;
    private final SysUserMapper userMapper;

    public CustomOrderService(CustomOrderMapper orderMapper, SiteMessageMapper messageMapper,
                              SysUserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public CustomOrder create(Long userId, CustomOrderRequest request) {
        if (request == null || !StringUtils.hasText(request.getContact()) || !StringUtils.hasText(request.getRequirement())) {
            throw new BizException("请填写联系方式和需求说明");
        }
        CustomOrder order = new CustomOrder();
        order.setUserId(userId);
        order.setContactName(request.getContactName());
        order.setContact(request.getContact().trim());
        order.setCompany(request.getCompany());
        order.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : "高端定制咨询");
        order.setRequirement(request.getRequirement().trim());
        order.setBudget(request.getBudget());
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        notifyAdmins(order);
        if (userId != null) {
            notify(userId, "定制咨询已提交", "我们已收到你的商业定制需求，顾问会尽快通过你留下的联系方式沟通方案与报价。");
        }
        return order;
    }

    public List<CustomOrder> mine(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<CustomOrder>()
                .eq(CustomOrder::getUserId, userId)
                .orderByDesc(CustomOrder::getCreateTime));
    }

    public List<CustomOrder> all() {
        return orderMapper.selectList(new LambdaQueryWrapper<CustomOrder>().orderByDesc(CustomOrder::getCreateTime));
    }

    @Transactional
    public CustomOrder updateStatus(Long id, String status) {
        CustomOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("定制单不存在");
        }
        if (!Arrays.asList("PENDING", "IN_PROGRESS", "DONE", "CANCELLED").contains(status)) {
            throw new BizException("不支持的状态");
        }
        order.setStatus(status);
        orderMapper.updateById(order);
        if (order.getUserId() != null) {
            if ("IN_PROGRESS".equals(status)) {
                notify(order.getUserId(), "定制顾问已接洽", "「" + order.getTitle() + "」已进入沟通，请留意电话或微信。");
            } else if ("DONE".equals(status)) {
                notify(order.getUserId(), "定制咨询已完成", "「" + order.getTitle() + "」已结案。如需继续开发，可再次提交需求。");
            }
        }
        return order;
    }

    private void notifyAdmins(CustomOrder order) {
        List<SysUser> admins = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "ADMIN"));
        String title = order.getTitle() == null ? "高端定制" : order.getTitle();
        for (SysUser admin : admins) {
            notify(admin.getId(), "新的商业定制咨询",
                    "联系人 " + (StringUtils.hasText(order.getContactName()) ? order.getContactName() : "未填")
                            + " / " + order.getContact() + "，预算 " + (StringUtils.hasText(order.getBudget()) ? order.getBudget() : "待议")
                            + "。主题：" + title);
        }
    }

    private void notify(Long userId, String title, String content) {
        SiteMessage message = new SiteMessage();
        message.setUserId(userId);
        message.setMsgType("CUSTOM");
        message.setTitle(title);
        message.setContent(content);
        message.setReadFlag(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }
}
