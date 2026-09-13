package com.projectmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projectmatch.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    @Update("UPDATE project SET remaining_count = remaining_count - 1 WHERE id = #{projectId} AND remaining_count > 0")
    int decreaseRemaining(Long projectId);

    @Update("UPDATE project SET remaining_count = 0 WHERE id = #{projectId} AND remaining_count > 0")
    int markSold(Long projectId);
}
