package com.bootstudy.gulimall.member.dao;

import com.bootstudy.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author hhd
 * @email 1017193846@qq.com
 * @date 2022-09-26 09:40:29
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
