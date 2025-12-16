package com.whd.cube.service.impl;

import com.whd.cube.entity.Event;
import com.whd.cube.mapper.EventMapper;
import com.whd.cube.service.EventService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 魔方项目字典表 服务实现类
 * </p>
 *
 * @author WHD
 */
@Service
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {

}
