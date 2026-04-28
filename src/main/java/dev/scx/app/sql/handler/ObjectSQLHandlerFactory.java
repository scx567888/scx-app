package dev.scx.app.sql.handler;

import dev.scx.reflect.TypeInfo;
import dev.scx.sql.handler.TypeSQLHandler;
import dev.scx.sql.handler.TypeSQLHandlerFactory;

public class ObjectSQLHandlerFactory implements TypeSQLHandlerFactory {

    @Override
    public TypeSQLHandler<?> createHandler(TypeInfo typeInfo) {
        return new ObjectSQLHandler(typeInfo);
    }

}
