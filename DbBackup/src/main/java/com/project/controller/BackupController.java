package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.model.BackupHistory;
import com.project.model.RemoteDbCredentials;
import com.project.service.BackupService;

@RestController
@RequestMapping("/api/backup")
@CrossOrigin(origins = "http://localhost:4200")

public class BackupController 
{
	    @Autowired
	    private BackupService backupService; 

	    @GetMapping("/history")
	    public List<BackupHistory> getBackupHistory() 
	    {
	        return backupService.getBackupHistory();
	    }
	
	    @PostMapping("/schedule")
	    public ResponseEntity<String> scheduleBackup(@RequestBody RemoteDbCredentials remoteDb)
	    {
	        String backupTime = remoteDb.getBackupTime(); 					// Extract backup time from request
	        if (backupTime == null || backupTime.isEmpty())
	        {
	            return ResponseEntity.badRequest().body("Backup time is required!");
	        }

	        backupService.setScheduledBackup(remoteDb, backupTime); 		// Pass user credentials too

	        return ResponseEntity.ok("Backup scheduled for " + backupTime);
	    }

	    
//	    @PostMapping("/schedule")
//	    public ResponseEntity<String> scheduleBackup(@RequestBody Map<String, String> request)
//	    {
//	        String backupTime = request.get("backupTime");
//	        if (backupTime == null || backupTime.isEmpty()) 
//	        {
//	            return ResponseEntity.badRequest().body("Backup time is required!");
//	        }
//
//	        backupService.setScheduledBackupTime(backupTime);
//	        return ResponseEntity.ok("Backup scheduled for " + backupTime);
//	    }
	    
}





//@Autowired
//private BackupHistoryRepository backupHistoryRepository;


//@GetMapping("/history")
//public ResponseEntity<List<BackupHistory>> getBackupHistory() {
//    return ResponseEntity.ok(backupHistoryRepository.findAllByOrderByCreatedTimeDesc());
//}

   
//   @PostMapping("/remote")
//   public ResponseEntity<String> backupRemoteDatabase(@RequestBody RemoteDbCredentials remoteDb) 
//   {
//       backupService.backupDatabase(remoteDb);
//       return ResponseEntity.ok("Remote backup initiated");
//   }
   	
//   @PostMapping("/schedule")
//   public ResponseEntity<String> backupRemoteDatabase(@RequestBody RemoteDbCredentials remoteDb)
//   {
//       if (remoteDb.getBackupTime() == null || remoteDb.getBackupTime().isEmpty())
//       {
//           remoteDb.setBackupTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));
//       }
//       backupService.backupDatabase(remoteDb);
//       return ResponseEntity.ok("Remote backup initiated at " + remoteDb.getBackupTime());
//   }
   
//   @PostMapping("/schedule")
//   public ResponseEntity<String> backupRemoteDatabase(@RequestBody RemoteDbCredentials remoteDb) 
//   {
//       backupService.setLastEnteredCredentials(remoteDb);  // Store for later use
//       return ResponseEntity.ok("Backup scheduled successfully");
//   }



//@PostMapping("/schedule")
//public ResponseEntity<String> scheduleBackup(@RequestBody RemoteDbCredentials remoteDb) {
//    String backupTime = remoteDb.getBackupTime(); 
//
//    if (backupTime == null || backupTime.isEmpty()) {
//        return ResponseEntity.badRequest().body("Backup time is required!");
//    }
//
//    backupService.setLastEnteredCredentials(remoteDb); // Store the remote DB credentials
//    backupService.setScheduledBackupTime(backupTime);
//    
//    return ResponseEntity.ok("Backup scheduled for " + backupTime);
//}


