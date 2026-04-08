package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.Store;
import java.util.List;

public interface StoreService extends IService<Store> {
    List<Store> listActive();
}
