package dev.scx.app.test.like;

import dev.scx.app.base.BaseModel;
import dev.scx.data.sql.annotation.Table;

//特殊表名
@Table("like")
public class Like extends BaseModel {

    public Order order;

}
