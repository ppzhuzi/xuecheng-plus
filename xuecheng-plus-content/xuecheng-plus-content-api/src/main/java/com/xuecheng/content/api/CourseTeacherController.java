package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class CourseTeacherController {

    @Resource
    CourseTeacherService courseTeacherService;

    //查询教师信息
    @GetMapping("/courseTeacher/list/{courseId}")
    @ResponseBody
    public List<CourseTeacher> courseTeachers(@PathVariable Long courseId){
        List<CourseTeacher> courseTeachers = courseTeacherService.selectCourseTeacher(courseId);
        return courseTeachers;
    }
}
