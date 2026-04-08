package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.FavoriteVO;
import com.scriptkill.entity.Script;
import com.scriptkill.entity.UserFavorite;
import com.scriptkill.mapper.ScriptMapper;
import com.scriptkill.mapper.UserFavoriteMapper;
import com.scriptkill.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements FavoriteService {
    @Autowired
    private ScriptMapper scriptMapper;

    @Override
    public List<FavoriteVO> listUserFavorites(Long userId) {
        QueryWrapper<UserFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserFavorite> favorites = this.list(wrapper);
        
        List<FavoriteVO> result = new ArrayList<>();
        for (UserFavorite f : favorites) {
            Script script = scriptMapper.selectById(f.getScriptId());
            if (script != null) {
                FavoriteVO vo = new FavoriteVO();
                vo.setId(f.getId());
                vo.setScriptId(f.getScriptId());
                vo.setName(script.getName());
                vo.setType(script.getType());
                vo.setDifficulty(script.getDifficulty());
                vo.setMinPlayers(script.getMinPlayers());
                vo.setMaxPlayers(script.getMaxPlayers());
                vo.setDuration(script.getDuration());
                vo.setPrice(script.getPrice());
                vo.setCover(script.getCover());
                vo.setDescription(script.getDescription());
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public void addFavorite(Long userId, Long scriptId) {
        // 检查是否已收藏
        QueryWrapper<UserFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("script_id", scriptId);
        if (this.count(wrapper) > 0) {
            return; // 已收藏
        }
        
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setScriptId(scriptId);
        favorite.setCreateTime(LocalDateTime.now());
        this.save(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long scriptId) {
        QueryWrapper<UserFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("script_id", scriptId);
        this.remove(wrapper);
    }

    @Override
    public boolean isFavorited(Long userId, Long scriptId) {
        QueryWrapper<UserFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("script_id", scriptId);
        return this.count(wrapper) > 0;
    }

    @Override
    public FavoriteVO getFavoriteByScriptId(Long userId, Long scriptId) {
        QueryWrapper<UserFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("script_id", scriptId);
        UserFavorite favorite = this.getOne(wrapper);
        if (favorite == null) {
            return null;
        }
        Script script = scriptMapper.selectById(favorite.getScriptId());
        if (script == null) {
            return null;
        }
        FavoriteVO vo = new FavoriteVO();
        vo.setId(favorite.getId());
        vo.setScriptId(favorite.getScriptId());
        vo.setName(script.getName());
        vo.setType(script.getType());
        vo.setDifficulty(script.getDifficulty());
        vo.setMinPlayers(script.getMinPlayers());
        vo.setMaxPlayers(script.getMaxPlayers());
        vo.setDuration(script.getDuration());
        vo.setPrice(script.getPrice());
        vo.setCover(script.getCover());
        vo.setDescription(script.getDescription());
        return vo;
    }
}
