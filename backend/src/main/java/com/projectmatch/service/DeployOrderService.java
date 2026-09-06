package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.common.BizException;
import com.projectmatch.dto.DeployOrderRequest;
import com.projectmatch.entity.DeployOrder;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.SiteMessage;
import com.projectmatch.entity.SysUser;
import com.projectmatch.mapper.DeployOrderMapper;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.SiteMessageMapper;
import com.projectmatch.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class DeployOrderService {

    private final DeployOrderMapper orderMapper;
    private final ProjectMapper projectMapper;
    private final SysUserMapper userMapper;
    private final SiteMessageMapper messageMapper;

    public DeployOrderService(DeployOrderMapper orderMapper, ProjectMapper projectMapper,
                              SysUserMapper userMapper, SiteMessageMapper messageMapper) {
        this.orderMapper = orderMapper;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
    }

    @Transactional
    public DeployOrder create(Long projectId, Long userId, DeployOrderRequest request) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在");
        }
        if (project.getDeployServiceEnabled() != null && project.getDeployServiceEnabled() == 0) {
            throw new BizException("该项目暂未开放远程部署服务");
        }
        Long active = orderMapper.selectCount(new LambdaQueryWrapper<DeployOrder>()
                .eq(DeployOrder::getUserId, userId)
                .eq(DeployOrder::getProjectId, projectId)
                .in(DeployOrder::getStatus, Arrays.asList("PENDING", "PAID", "IN_PROGRESS")));
        if (active != null && active > 0) {
            throw new BizException("你已有进行中的远程部署订单，请到「我的工坊」查看");
        }
        DeployOrder order = new DeployOrder();
        order.setUserId(userId);
        order.setProjectId(projectId);
        order.setContact(request == null ? null : request.getContact());
        order.setEnvironmentNote(request == null ? null : request.getEnvironmentNote());
        order.setAmount(project.getDeployPrice() == null ? 199 : project.getDeployPrice());
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }

    @Transactional
    public DeployOrder pay(Long orderId, Long userId) {
        DeployOrder order = requireMine(orderId, userId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BizException("当前订单状态不可支付");
        }
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Project project = projectMapper.selectById(order.getProjectId());
        String name = project == null ? ("项目#" + order.getProjectId()) : project.getName();
        notify(userId, order.getProjectId(), "远程部署订单已支付",
                "你已支付「" + name + "」远程部署协助（¥" + order.getAmount()
                        + "）。工作人员会按你留下的联系方式对接，协助本地或服务器跑通，不会代替你完成毕业设计正文。");
        notifyAdmins(order, name);
        return order;
    }

    public List<DeployOrder> mine(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<DeployOrder>()
                .eq(DeployOrder::getUserId, userId)
                .orderByDesc(DeployOrder::getCreateTime));
    }

    public List<DeployOrder> all() {
        return orderMapper.selectList(new LambdaQueryWrapper<DeployOrder>().orderByDesc(DeployOrder::getCreateTime));
    }

    @Transactional
    public DeployOrder updateStatus(Long orderId, String status) {
        DeployOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (!Arrays.asList("PAID", "IN_PROGRESS", "DONE", "CANCELLED").contains(status)) {
            throw new BizException("不支持的状态");
        }
        order.setStatus(status);
        orderMapper.updateById(order);
        Project project = projectMapper.selectById(order.getProjectId());
        String name = project == null ? "项目" : project.getName();
        if ("IN_PROGRESS".equals(status)) {
            notify(order.getUserId(), order.getProjectId(), "开始远程部署",
                    "工作人员已开始处理「" + name + "」的远程部署协助。");
        } else if ("DONE".equals(status)) {
            notify(order.getUserId(), order.getProjectId(), "远程部署已完成",
                    "「" + name + "」远程部署协助已完成。请确认环境可运行，并继续理解代码、完成自己的修改。");
        }
        return order;
    }

    private DeployOrder requireMine(Long orderId, Long userId) {
        DeployOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private void notifyAdmins(DeployOrder order, String projectName) {
        List<SysUser> admins = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "ADMIN"));
        String contact = StringUtils.hasText(order.getContact()) ? order.getContact() : "未填写";
        for (SysUser admin : admins) {
            notify(admin.getId(), order.getProjectId(), "新的远程部署订单",
                    "用户 #" + order.getUserId() + " 已支付「" + projectName + "」远程部署（¥"
                            + order.getAmount() + "），联系方式：" + contact);
        }
    }

    private void notify(Long userId, Long projectId, String title, String content) {
        SiteMessage message = new SiteMessage();
        message.setUserId(userId);
        message.setProjectId(projectId);
        message.setMsgType("DEPLOY");
        message.setTitle(title);
        message.setContent(content);
        message.setReadFlag(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }
}
