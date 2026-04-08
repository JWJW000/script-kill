package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("store")
public class Store {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("store_name")
    private String storeName;
    
    @TableField("location")
    private String location;
    
    private Integer status;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
