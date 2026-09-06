package com.projectmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.projectmatch.entity.SiteVisit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface SiteVisitMapper extends BaseMapper<SiteVisit> {

    @Select("SELECT COUNT(DISTINCT visitor_id) FROM site_visit")
    long countDistinctVisitors();

    @Select("SELECT COUNT(DISTINCT visitor_id) FROM site_visit WHERE visit_date = #{date}")
    long countDistinctVisitorsOn(@Param("date") LocalDate date);

    @Select("SELECT IFNULL(SUM(page_views), 0) FROM site_visit")
    long sumPageViews();

    @Select("SELECT IFNULL(SUM(page_views), 0) FROM site_visit WHERE visit_date = #{date}")
    long sumPageViewsOn(@Param("date") LocalDate date);
}
