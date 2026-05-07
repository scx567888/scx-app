package dev.scx.app.test.person;

import dev.scx.app._old.base.BaseModel;
import dev.scx.data.sql.annotation.Table;

@Table("person")
public class Person extends BaseModel {
    /// 关联的 汽车 ID
    public Long carID;

    /// 年龄
    public Integer age;

    /// 钱
    public Long money;

    public Person setMoney(long money) {
        this.money = money;
        return this;
    }

}
