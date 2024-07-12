package com.xin.xinChat.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xin.xinChat.model.dto.group.GroupInfoQueryRequest;
import com.xin.xinChat.model.entity.GroupInfo;
import org.apache.ibatis.annotations.Param;

/**
* @author 15712
* @description 针对表【groupInfo】的数据库操作Mapper
* @createDate 2024-06-20 20:45:56
* @Entity com.xin.xinChat.model.entity.GroupInfoEnum
*/
public interface GroupInfoMapper extends BaseMapper<GroupInfo> {

    IPage<GroupInfo> loadGroupInfo(Page<GroupInfo> page, @Param("groupInfoQueryRequest")GroupInfoQueryRequest groupInfoQueryRequest);

}




