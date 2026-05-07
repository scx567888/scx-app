package dev.scx.app.sql;

import dev.scx.app.ScxAppModule;
import dev.scx.app.sql.handler.ObjectSQLHandlerFactory;
import dev.scx.jdbc.spy.ScxJdbcSpy;
import dev.scx.jdbc.spy.listener.logging.LoggingDataSourceListener;
import dev.scx.jdbc.spy.listener.logging.PreparedStatementLogStyle;
import dev.scx.sql.JDBCConnectionInfo;
import dev.scx.sql.SQLClient;
import dev.scx.sql.TypeSQLResolver;

public class ScxAppSQLModule implements ScxAppModule {

    static SQLClient initSQLClient(ScxAppOptions scxOptions, ScxFeatureConfig scxFeatureConfig) {

        return SQLClient.of(
            new JDBCConnectionInfo(
                scxOptions.dataSourceUrl(),
                scxOptions.dataSourceUsername(),
                scxOptions.dataSourcePassword(),
                scxOptions.dataSourceParameters()
            ),
            TypeSQLResolver.registerDefaultHandlers(TypeSQLResolver.builder())
                .registerHandlerFactory(new ObjectSQLHandlerFactory())
                .build(),
            d-> {
                return scxFeatureConfig.get(USE_SPY) ? ScxJdbcSpy.spy(d, new LoggingDataSourceListener(PreparedStatementLogStyle.RENDERED_SQL)): d;
            }
        );
    }


}
