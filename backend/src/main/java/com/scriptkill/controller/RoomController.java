package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.entity.Room;
import com.scriptkill.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/list")
    public Result<List<Room>> list() {
        List<Room> rooms = roomService.listActive();
        return Result.success(rooms);
    }

    @GetMapping("/list-by-store")
    public Result<List<Room>> listByStore(@RequestParam Long storeId) {
        List<Room> rooms = roomService.listByStoreId(storeId);
        return Result.success(rooms);
    }

    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        Room room = roomService.getById(id);
        return Result.success(room);
    }

    @PostMapping
    public Result<?> create(@RequestBody Room room, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        if (room.getStatus() == null) {
            room.setStatus(1);
        }
        roomService.save(room);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Room room, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        room.setId(id);
        roomService.updateById(room);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        roomService.removeById(id);
        return Result.success("删除成功");
    }
}
