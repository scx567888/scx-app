package dev.scx.app.base;

import dev.scx.data.sql.annotation.Column;

import java.time.LocalDateTime;

// todo
/// 最基本的 model 包含最基础的元数据
///
/// @author scx567888
/// @version 0.0.1
public abstract class BaseModel {

    /// id
    @Column(primary = true, autoIncrement = true)
    public Long id;

    /// 创建时间
    @Column(notNull = true, defaultValue = "(NOW())", index = true)
    public LocalDateTime createdDate;

    /// 最后修改时间
    @Column(notNull = true, defaultValue = "(NOW())", onUpdate = "CURRENT_TIMESTAMP", index = true)
    public LocalDateTime updatedDate;

}
