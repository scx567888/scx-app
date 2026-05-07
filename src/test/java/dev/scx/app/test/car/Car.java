package dev.scx.app.test.car;

import dev.scx.app._old.base.BaseModel;
import dev.scx.data.sql.annotation.Column;
import dev.scx.data.sql.annotation.NoColumn;
import dev.scx.data.sql.annotation.Table;

@Table("car")
public class Car extends BaseModel {

    @Column(unique = true)
    public String name;

    public CarColor color;

    public CarOwner owner;

    public String[] tags;

    //测试虚拟字段
    @NoColumn
    public String reverseName;

}
