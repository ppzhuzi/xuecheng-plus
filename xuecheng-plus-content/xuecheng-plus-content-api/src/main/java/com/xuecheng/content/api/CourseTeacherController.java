package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    //添加修改教师信息
    @PostMapping("/courseTeacher")
    public CourseTeacher courseTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.insertAndUpdateCourseTeacher(courseTeacher);
    }

    //删除教师
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId,@PathVariable Long teacherId){
        courseTeacherService.deleteCourseTeacher(courseId,teacherId);
    }


}
