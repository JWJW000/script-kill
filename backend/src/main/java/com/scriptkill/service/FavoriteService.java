package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.FavoriteVO;
import com.scriptkill.entity.UserFavorite;
import java.util.List;

public interface FavoriteService extends IService<UserFavorite> {
    List<FavoriteVO> listUserFavorites(Long userId);
    void addFavorite(Long userId, Long scriptId);
    void removeFavorite(Long userId, Long scriptId);
    boolean isFavorited(Long userId, Long scriptId);
    FavoriteVO getFavoriteByScriptId(Long userId, Long scriptId);
}
