package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.Room;
import com.scriptkill.mapper.RoomMapper;
import com.scriptkill.service.RoomService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {
    @Override
    public List<Room> listByStoreId(Long storeId) {
        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        wrapper.eq("store_id", storeId);
        wrapper.eq("status", 1);
        return this.list(wrapper);
    }
    
    @Override
    public List<Room> listActive() {
        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return this.list(wrapper);
    }
}
