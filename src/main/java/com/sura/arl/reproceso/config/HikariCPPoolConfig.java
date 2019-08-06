package com.sura.arl.reproceso.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class HikariCPPoolConfig {

	@Autowired
	Environment env;

	static final long TIMEOUT = (10 * 60) * 1000;
	static final long IDLE_TIMEOUT = 60000;

	@Bean
	public HikariDataSource dataSource() {

		StringBuilder jdbcUrl = new StringBuilder("jdbc:oracle:thin:@").append(env.getRequiredProperty("db.host"))
				.append(":").append(env.getRequiredProperty("db.port")).append(":")
				.append(env.getRequiredProperty("db.service"));

		Integer poolSizeMax = Integer.parseInt(env.getRequiredProperty("pool.size"));
		Integer idleConMin = poolSizeMax / 2;

		HikariDataSource ds = new HikariDataSource();
		ds.setPoolName("RecaudosHikari");
		ds.setJdbcUrl(jdbcUrl.toString());
		ds.setDriverClassName("oracle.jdbc.OracleDriver");
		ds.setUsername(env.getRequiredProperty("db.user"));
		ds.setPassword(env.getRequiredProperty("db.password"));
		ds.setAutoCommit(false);
		ds.setMaximumPoolSize(poolSizeMax);
		ds.setMinimumIdle(idleConMin);
		ds.setIdleTimeout(20 * 60 * 1000);
		ds.setRegisterMbeans(true);

		return ds;
	}

}
