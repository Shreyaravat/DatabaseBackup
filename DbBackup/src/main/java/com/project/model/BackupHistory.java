package com.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BackupHistory 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String createdTime;
	private String backupPathC;
	private String backupPathD;
    private String status;
     
    // Store Remote DB Info
//    private String dbIp;
//    private String dbName;
//    private String dbUsername;
    
    public BackupHistory( String createdTime, String backupPathC, String backupPathD, String status) 
	{
		this.createdTime = createdTime;
		this.backupPathC = backupPathC;
		this.backupPathD = backupPathD;
		this.status = status;
	}
	 
	public BackupHistory()
	{
		
	}
	
	public int getId() 
	{
		return id;
	}
	
	public void setId(int id) 
	{
		this.id = id;
	}
	
	public String getCreatedTime()
	{
		return createdTime;
	}
	
	public void setCreatedTime(String createdTime) 
	{
		this.createdTime = createdTime;
	}
	
	public String getBackupPathC() 
	{
		return backupPathC;
	}
	
	public void setBackupPathC(String backupPathC) 
	{
		this.backupPathC = backupPathC;
	}
	
	public String getBackupPathD() 
	{
		return backupPathD;
	}
	
	public void setBackupPathD(String backupPathD) 
	{
		this.backupPathD = backupPathD;
	}
	
	public String getStatus() 
	{
		return status;
	}
	
	public void setStatus(String status) 
	{
		this.status = status;
	}     
	
	  // Getters & Setters
//    public String getDbIp() { return dbIp; }
//    public void setDbIp(String dbIp) { this.dbIp = dbIp; }
//
//    public String getDbName() { return dbName; }
//    public void setDbName(String dbName) { this.dbName = dbName; }
//
//    public String getDbUsername() { return dbUsername; }
//    public void setDbUsername(String dbUsername) { this.dbUsername = dbUsername; }
}



