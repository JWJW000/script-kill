package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.Script;
import com.scriptkill.mapper.ScriptMapper;
import com.scriptkill.service.ScriptService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ScriptServiceImpl extends ServiceImpl<ScriptMapper, Script> implements ScriptService {
    @Override
    public List<Script> listScripts(String name, String type, String difficulty, Integer minPlayers, Integer maxPlayers, BigDecimal minPrice, BigDecimal maxPrice) {
        QueryWrapper<Script> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只查询上架的剧本

        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            wrapper.eq("difficulty", difficulty);
        }
        if (minPlayers != null) {
            wrapper.ge("max_players", minPlayers);
        }
        if (maxPlayers != null) {
            wrapper.le("min_players", maxPlayers);
        }
        if (minPrice != null) {
            wrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            wrapper.le("price", maxPrice);
        }
        
        return this.list(wrapper);
    }
}
