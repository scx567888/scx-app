package dev.scx.app.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.scx.app.ScxAppInitContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;
import dev.scx.app.config.ScxConfig;
import dev.scx.app.sql.handler.ObjectSQLHandlerFactory;
import dev.scx.jdbc.spy.ScxJdbcSpy;
import dev.scx.jdbc.spy.listener.logging.LoggingDataSourceListener;
import dev.scx.jdbc.spy.listener.logging.PreparedStatementLogStyle;
import dev.scx.sql.JDBCConnectionInfo;
import dev.scx.sql.SQLClient;
import dev.scx.sql.TypeSQLResolver;

public class ScxAppSQLModule implements ScxAppModule {

    @Override
    public ScxAppModuleDefinition init(ScxAppInitContext context) {
        SQLClient sqlClient = initSQLClient(context.config());

        return ScxAppModuleDefinition.of().componentInstance(sqlClient);
    }

    static SQLClient initSQLClient(ScxConfig scxOptions) {
        var dataSourceUrl=scxOptions.get("scx.sql.url", String.class);
        var dataSourceUsername=scxOptions.get("scx.sql.username", String.class);
        var dataSourcePassword=scxOptions.get("scx.sql.password", String.class);
        var dataSourceParameters=scxOptions.get("scx.sql.parameters", String[].class);

        JDBCConnectionInfo jdbcConnectionInfo = new JDBCConnectionInfo(
            dataSourceUrl,
            dataSourceUsername,
            dataSourcePassword,
            dataSourceParameters
        );

        TypeSQLResolver build = TypeSQLResolver.builder()
            .registerDefaultHandlers()
            .registerHandlerFactory(new ObjectSQLHandlerFactory())
            .build();

        var useSpy= scxOptions.get("xxx", boolean.class,false);

        return SQLClient.of(
            jdbcConnectionInfo,
            build,
            d->{
                var hikariConfig = new HikariConfig();
                hikariConfig.setDataSource(d);
                return new HikariDataSource(hikariConfig);
            },
            d-> {
                return useSpy ? ScxJdbcSpy.spy(d, new LoggingDataSourceListener(PreparedStatementLogStyle.RENDERED_SQL)): d;
            }
        );
    }


}
