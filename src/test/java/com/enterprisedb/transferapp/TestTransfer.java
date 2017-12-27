package com.enterprisedb.transferapp;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestTransfer
{	
	@BeforeClass
	public static void init()
	{
		ResourceManagerSettings.setParams();
	}

	@Before
	public void reset()
	{
		TransferApp.createAccounts();
	}
	
	@AfterClass
	public static void shutdown()
	{
		ResourceManagerSettings.closeAll();
	}
	
	@Test
	public void doTest()
	{
		TransferApp.doTransfer();
		Assert.assertEquals(true, TransferApp.checkTransfer());
	}
}
