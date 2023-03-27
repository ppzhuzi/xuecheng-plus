package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Resource
    CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<CourseTeacher> selectCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        return courseTeachers;
    }

    @Transactional
    @Override
    public CourseTeacher insertAndUpdateCourseTeacher(CourseTeacher courseTeacher) {
        //判断是新增还是修改
        Long teacherId = courseTeacher.getId();
        CourseTeacher courseTeacher1 = new CourseTeacher();
        BeanUtils.copyProperties(courseTeacher,courseTeacher1);
        //新增
        if(teacherId == null){
            int insert = courseTeacherMapper.insert(courseTeacher1);
            if(insert <= 0){
                XueChengPlusException.cast("新增失败");
            }
            return courseTeacherMapper.selectById(courseTeacher1.getId());
        }
        //修改
        int i = courseTeacherMapper.updateById(courseTeacher1);
        if(i <= 0){
            XueChengPlusException.cast("修改失败");
        }
        return  courseTeacherMapper.selectById(courseTeacher1.getId());
    }

    @Transactional
    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        queryWrapper.eq(CourseTeacher::getId,teacherId);
        int i = courseTeacherMapper.delete(queryWrapper);
        if(i <= 0){
            XueChengPlusException.cast("删除失败");
        }
    }
}
