package com.project.model;

public class RemoteDbCredentials 
{
    private String ip;
    private String port;
    private String dbName;
    private String username;
    private String password;
    private String backupTime;

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip) 
	{
		this.ip = ip;
	}

	public String getPort()
	{
		return port;
	}

	public void setPort(String port) 
	{
		this.port = port;
	}

	public String getDbName() 
	{
		return dbName;
	}
	
	public void setDbName(String dbName) 
	{
		this.dbName = dbName;
	}

	public String getUsername() 
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword() 
	{
		return password;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}
	
	public String getBackupTime()
	{
        return backupTime;
    }

    public void setBackupTime(String backupTime) 
    {
        this.backupTime = backupTime;
    }	
}









