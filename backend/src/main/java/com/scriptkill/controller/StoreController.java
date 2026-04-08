package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.entity.Store;
import com.scriptkill.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/store")
public class StoreController {
    @Autowired
    private StoreService storeService;

    @GetMapping("/list")
    public Result<List<Store>> list() {
        List<Store> stores = storeService.listActive();
        return Result.success(stores);
    }

    @GetMapping("/{id}")
    public Result<Store> getById(@PathVariable Long id) {
        Store store = storeService.getById(id);
        return Result.success(store);
    }

    @PostMapping
    public Result<?> create(@RequestBody Store store, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        if (store.getStatus() == null) {
            store.setStatus(1);
        }
        storeService.save(store);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Store store, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        store.setId(id);
        storeService.updateById(store);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        storeService.removeById(id);
        return Result.success("删除成功");
    }
}
