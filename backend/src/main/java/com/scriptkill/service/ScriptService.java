package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.Script;
import java.math.BigDecimal;
import java.util.List;

public interface ScriptService extends IService<Script> {
    List<Script> listScripts(String name, String type, String difficulty, Integer minPlayers, Integer maxPlayers, BigDecimal minPrice, BigDecimal maxPrice);
}
