package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.Room;
import java.util.List;

public interface RoomService extends IService<Room> {
    List<Room> listByStoreId(Long storeId);
    List<Room> listActive();
}
