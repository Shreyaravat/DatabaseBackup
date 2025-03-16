package com.project.scheduler;

//import java.time.LocalDateTime;
//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.project.service.BackupService;

@Component
public class BackupScheduler 
{
	private final BackupService backupService;
	
	public BackupScheduler(BackupService backupService)
	{
		this.backupService = backupService;
	}
	
//	@Scheduled(cron = "0 0/1 * * * *")
//	@Scheduled(cron = "*/10 * * * * *")

	public void scheduleBackup()
	{
//		System.out.println("Database backup running..");
//		System.out.println("Scheduled Backup triggered at: "+LocalDateTime.now());
		
		backupService.backupDatabase();
	}
}













