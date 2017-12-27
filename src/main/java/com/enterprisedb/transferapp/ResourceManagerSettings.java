package com.enterprisedb.transferapp;
import java.util.Properties;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;

public class ResourceManagerSettings
{
	private static UserTransactionManager atomikosTM;
	private static AtomikosDataSourceBean pgRM1;
	private static AtomikosDataSourceBean pgRM2;

	public static void setParams()
	{
		atomikosTM = new UserTransactionManager();
		try
		{
			atomikosTM.init();
			pgRM1 = new AtomikosDataSourceBean();
			pgRM1.setMaxPoolSize(10);
			pgRM1.setUniqueResourceName("postgres1");
			pgRM1.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");
			Properties p1 = new Properties();
			p1.setProperty("user", "atomikos");
			p1.setProperty("password", "abc");
			p1.setProperty("serverName", "localhost");
			p1.setProperty("portNumber", "5432");
			p1.setProperty("databaseName", "postgres");
			pgRM1.setXaProperties(p1);


			pgRM2 = new AtomikosDataSourceBean();
			pgRM2.setMaxPoolSize(10);
			pgRM2.setUniqueResourceName("postgres2");
			pgRM2.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");
			Properties p2 = new Properties();
			p2.setProperty("user", "atomikos");
			p2.setProperty("password", "abc");
			p2.setProperty("serverName", "localhost");
			p2.setProperty("portNumber", "9432");
			p2.setProperty("databaseName", "postgres");
			pgRM2.setXaProperties(p2);			
		}
		catch (SystemException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static void closeAll()
	{
		pgRM1.close();
		pgRM2.close();
		atomikosTM.close();
	}
	
	public static AtomikosDataSourceBean getRM1()
	{
		return pgRM1;
	}

	public static AtomikosDataSourceBean getRM2()
	{
		return pgRM2;
	}
	
	public static TransactionManager getAtomikos()
	{
		return atomikosTM;
	}
}
