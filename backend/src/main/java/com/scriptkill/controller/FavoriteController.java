package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.entity.FavoriteVO;
import com.scriptkill.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/list")
    public Result<List<FavoriteVO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FavoriteVO> favorites = favoriteService.listUserFavorites(userId);
        return Result.success(favorites);
    }

    @GetMapping("/my-favorites")
    public Result<List<FavoriteVO>> myFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FavoriteVO> favorites = favoriteService.listUserFavorites(userId);
        return Result.success(favorites);
    }

    @PostMapping
    public Result<?> add(@RequestParam Long scriptId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.addFavorite(userId, scriptId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // id here is the scriptId
        favoriteService.removeFavorite(userId, id);
        return Result.success("已取消收藏");
    }

    @GetMapping("/check")
    public Result<FavoriteVO> check(@RequestParam Long scriptId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        FavoriteVO favorite = favoriteService.getFavoriteByScriptId(userId, scriptId);
        return Result.success(favorite);
    }
}
