package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description
 * @date 2023/2/14 12:11
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Resource
    TeachplanMapper teachplanMapper;

    @Resource
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    private int getTeachplanCount(Long courseId,Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return  count+1;
    }
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划id判断是新增和修改
        Long teachplanId = saveTeachplanDto.getId();
        if(teachplanId ==null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            //确定排序字段，找到它的同级节点个数，排序字段就是个数加1  select count(1) from teachplan where course_id=117 and parentid=268
            Long parentid = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();
            int teachplanCount = getTeachplanCount(courseId, parentid);
            teachplan.setOrderby(teachplanCount);
            teachplanMapper.insert(teachplan);

        }else{
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            //将参数复制到teachplan
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }

    }
    @Transactional
    @Override
    public void deleteTeachPlan(Long courseId) {
        //判断课程计划是否有小节
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,courseId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        if(count > 0){
            //有小节
            XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
        }
        //没有小节，直接删除
        teachplanMapper.deleteById(courseId);
        LambdaQueryWrapper<TeachplanMedia> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        teachplanMediaMapper.delete(queryWrapper1);
    }

    @Transactional
    @Override
    public void orderByTeachplan(String moveType, Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        // 获取层级和当前orderby，章节移动和小节移动的处理方式不同
        Integer grade = teachplan.getGrade();
        Integer orderby = teachplan.getOrderby();
        // 章节移动是比较同一课程id下的orderby
        Long courseId = teachplan.getCourseId();
        // 小节移动是比较同一章节id下的orderby
        Long parentid = teachplan.getParentid();
        if ("moveup".equals(moveType)) {
            if (grade == 1) {
                // 章节上移，找到上一个章节的orderby，然后与其交换orderby
                // SELECT * FROM teachplan WHERE courseId = 117 AND grade = 1  AND orderby < 1 ORDER BY orderby DESC LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getGrade, 1)
                        .eq(Teachplan::getCourseId, courseId)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);
            } else if (grade == 2) {
                // 小节上移
                // SELECT * FROM teachplan WHERE parentId = 268 AND orderby < 5 ORDER BY orderby DESC LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);
            }

        } else if ("movedown".equals(moveType)) {
            if (grade == 1) {
                // 章节下移
                // SELECT * FROM teachplan WHERE courseId = 117 AND grade = 1 AND orderby > 1 ORDER BY orderby ASC LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getCourseId, courseId)
                        .eq(Teachplan::getGrade, grade)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);
            } else if (grade == 2) {
                // 小节下移
                // SELECT * FROM teachplan WHERE parentId = 268 AND orderby > 1 ORDER BY orderby ASC LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);
            }
        }
    }

    /**
     * 交换两个Teachplan的orderby
     * @param teachplan
     * @param tmp
     */
    private void exchangeOrderby(Teachplan teachplan, Teachplan tmp) {
        if (tmp == null)
            XueChengPlusException.cast("已经到头啦，不能再移啦");
        else {
            // 交换orderby，更新
            Integer orderby = teachplan.getOrderby();
            Integer tmpOrderby = tmp.getOrderby();
            teachplan.setOrderby(tmpOrderby);
            tmp.setOrderby(orderby);
            teachplanMapper.updateById(tmp);
            teachplanMapper.updateById(teachplan);
        }
    }


}
