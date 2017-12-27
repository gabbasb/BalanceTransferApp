package com.enterprisedb.transferapp;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.XAConnection;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class TransferApp
{
	public static void createAccounts()
	{
		try
		{
			Connection c1 = ResourceManagerSettings.getRM1().getConnection();
			Statement s1 = c1.createStatement();
			s1.executeUpdate("DROP TABLE IF EXISTS ACCOUNTS");
			s1.executeUpdate("create table ACCOUNTS(id int, title varchar(255), balance int)");
			s1.executeUpdate("insert into ACCOUNTS values (1, 'Account A1', 100)");
			s1.close();
			c1.close();

			Connection c2 = ResourceManagerSettings.getRM2().getConnection();
			Statement s2 = c2.createStatement();
			s2.executeUpdate("DROP TABLE IF EXISTS ACCOUNTS");
			s2.executeUpdate("create table ACCOUNTS(id int, title varchar(255), balance int)");
			s2.executeUpdate("insert into ACCOUNTS values (2, 'Account A2', 0)");
			s2.close();
			c2.close();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean checkTransfer()
	{
		try
		{
			Connection c1 = ResourceManagerSettings.getRM1().getConnection();
			Statement s1 = c1.createStatement();
			ResultSet rs1 =  s1.executeQuery("select balance from accounts where id = 1");

			Connection c2 = ResourceManagerSettings.getRM2().getConnection();
			Statement s2 = c2.createStatement();
			ResultSet rs2 =  s2.executeQuery("select balance from accounts where id = 2");

			if (rs1.next() && rs2.next())
			{
				if (rs1.getInt(1) == 0 && rs2.getInt(1) == 100)
					return true;
				else
					return false;
			}
			rs1.close();
			s1.close();
			c1.close();

			rs2.close();
			s2.close();
			c2.close();

		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static void doTransfer()
	{
		Transaction transaction = null;
		try
		{
			ResourceManagerSettings.getAtomikos().begin();
			transaction = ResourceManagerSettings.getAtomikos().getTransaction();

			XAConnection xac1 = ResourceManagerSettings.getRM1().getXaDataSource().getXAConnection();
			transaction.enlistResource(xac1.getXAResource());
			
			Connection c1 = xac1.getConnection();
			Statement s1 = c1.createStatement();
			s1.executeUpdate("update ACCOUNTS set balance = 0 where id = 1");
			s1.close();
			c1.close();
			
			transaction.delistResource(xac1.getXAResource(), XAResource.TMSUCCESS);



			XAConnection xac2 = ResourceManagerSettings.getRM2().getXaDataSource().getXAConnection();
			transaction.enlistResource(xac2.getXAResource());
			
			Connection c2 = xac2.getConnection();
			Statement s2 = c2.createStatement();
			s2.executeUpdate("update ACCOUNTS set balance = 100 where id = 2");
			s2.close();
			c2.close();
			
			transaction.delistResource(xac2.getXAResource(), XAResource.TMSUCCESS);

			transaction.commit();
		}
		catch (Throwable e)
		{	
			e.printStackTrace();
			try
			{
				if (transaction != null)
				{
					ResourceManagerSettings.getAtomikos().rollback();
				}
			}
			catch (IllegalStateException e1)
			{
				e1.printStackTrace();
			}
			catch (SecurityException e1)
			{
				e1.printStackTrace();
			}
			catch (SystemException e1)
			{
				e1.printStackTrace();
			}
		}
	}
}
