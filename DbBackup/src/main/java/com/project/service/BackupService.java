package com.project.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.model.BackupHistory;
import com.project.model.RemoteDbCredentials;
import com.project.repo.BackupHistoryRepository;

@Service
public class BackupService 
{
	
	 private RemoteDbCredentials lastEnteredCredentials;  // Store latest user input

	    public void setLastEnteredCredentials(RemoteDbCredentials remoteDb)
	    {
	        this.lastEnteredCredentials = remoteDb;
	    }
	
	@Autowired
    private BackupHistoryRepository backupHistoryRepository; 

	private static final String C_BACKUP_DIR = "C:\\Backup\\CBackupFolder";
	private static final String D_BACKUP_DIR = "D:\\Backup\\DBackupFolder";

	private static final int MAX_BACKUPS = 5;			//The backup paths and limit are shared across all method calls.			
	
	private static final Queue<String> backupQueueC = new LinkedList<>();
    private static final Queue<String> backupQueueD = new LinkedList<>();
	    
    private final AtomicReference<LocalDateTime> scheduledBackupTime = new AtomicReference<>(null);  
    
//    public void setScheduledBackupTime(String backupTime) 
//    {
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // Accepts 'yyyy-MM-dd'T'HH:mm'
//        LocalDateTime scheduledTime = LocalDateTime.parse(backupTime, formatter);
//        scheduledBackupTime.set(scheduledTime);
//        System.out.println("Backup scheduled for: " + scheduledTime);
//    }
    
   /* public void setScheduledBackupTime(String backupTime) 
    {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime scheduledTime = LocalDateTime.parse(backupTime, formatter);
        scheduledBackupTime.set(scheduledTime);
        System.out.println("Backup scheduled for: " + scheduledTime);

        // Store in database with "Pending" status
        BackupHistory pendingBackup = new BackupHistory();
        pendingBackup.setCreatedTime(backupTime);
        pendingBackup.setBackupPathC(""); // Empty paths before execution
        pendingBackup.setBackupPathD("");
        pendingBackup.setStatus("Pending");
        
        backupHistoryRepository.save(pendingBackup);
    } */
    
    public void setScheduledBackup(RemoteDbCredentials remoteDb, String backupTime) 
    {
        // Store the provided database credentials
        this.lastEnteredCredentials = remoteDb;  // Store user input instead of default values

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime scheduledTime = LocalDateTime.parse(backupTime, formatter);
        scheduledBackupTime.set(scheduledTime);
        System.out.println("Backup scheduled for: " + scheduledTime);

        // Store in database with "Pending" status
        BackupHistory pendingBackup = new BackupHistory();
        pendingBackup.setCreatedTime(backupTime);
        pendingBackup.setBackupPathC(""); // Empty paths before execution
        pendingBackup.setBackupPathD("");
        pendingBackup.setStatus("Pending");

        backupHistoryRepository.save(pendingBackup);
    }


    
    @Scheduled(fixedRate = 60000) // Check every 1 minute
    public void checkAndTriggerBackup() 
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = scheduledBackupTime.get();

        if (scheduledTime != null && now.isAfter(scheduledTime)) 
        {
            System.out.println("Database backup running at scheduled time: " + now);
            scheduledBackupTime.set(null); // Reset after execution
            backupDatabase();
        }
    }
    
//    public void backupDatabase()
//    {
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
//        String backupFile = "dbBackup_" + timestamp + ".sql";
//        String backupPath = D_BACKUP_DIR + backupFile;
//
//        ProcessBuilder processBuilder = new ProcessBuilder(
//                "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
//                "-U", "postgres",
//                "-d", "second_db",
//                "-F", "c",
//                "-f", backupPath);
//
//        processBuilder.environment().put("PGPASSWORD", "root");
//
//        Process process = null;
//        
//        try 
//        {
//        	 process = processBuilder.start();
//  
//        	 try (InputStream inputStream = process.getInputStream();
//                  InputStream errorStream = process.getErrorStream()) 
//        	 {
//
//        		 int exitCode = process.waitFor();
//
//        		 if (exitCode == 0) 
//        		 {
//        			 System.out.println("Backup successful!");
//
//                	 String zipFile = "dbBackup_" + timestamp + ".zip";
//                	 String zipPathC = C_BACKUP_DIR + zipFile;
//               	  	 String zipPathD = D_BACKUP_DIR + zipFile;
//
//                	 zipBackup(backupPath, zipPathC);
//                	 zipBackup(backupPath, zipPathD);
//
//                	 // Delete original .sql file after zipping
//                	 Files.deleteIfExists(Paths.get(backupPath));
//
//                	 // Manage backup queue (limit to 5 backups)
//                	 manageBackupQueue(zipPathC, C_BACKUP_DIR, backupQueueC);
//                	 manageBackupQueue(zipPathD, D_BACKUP_DIR, backupQueueD);
//
//                	 // Save backup history to the database
//                	 BackupHistory backupRecord = new BackupHistory(timestamp, zipPathC, zipPathD, "Available");
//                     backupHistoryRepository.save(backupRecord);
//                 }            
//        		 else 
//        		 {
//        			 System.out.println("Backup failed!");
//        		 }
//        	 } 
//        }
//        
//        catch (IOException | InterruptedException e) 
//        {
//            e.printStackTrace();
//        }
//        
//        finally 
//        {
//        	if(process != null)
//        	{
//        		process.destroy();
//        	}
//		}
//    }

//    public void backupDatabase() {
//        if (lastEnteredCredentials == null) {
//            System.out.println("No database credentials provided yet, using default credentials.");
//            lastEnteredCredentials = new RemoteDbCredentials();
//            lastEnteredCredentials.setIp("localhost");
//            lastEnteredCredentials.setPort("5432");
//            lastEnteredCredentials.setDbName("second_db");
//            lastEnteredCredentials.setUsername("postgres");
//            lastEnteredCredentials.setPassword("root");
//        }
//        backupDatabase(lastEnteredCredentials); // Call with latest credentials
//    }
//    
////    public void backupDatabase(RemoteDbCredentials remoteDb)
////    {
////        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
////        String backupFile = "dbBackup_" + timestamp + ".sql";
////        String backupPath = D_BACKUP_DIR + backupFile;
////
////        ProcessBuilder processBuilder = new ProcessBuilder(
////            "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
////            "-h", remoteDb.getIp(),
////            "-p", remoteDb.getPort(),
////            "-U", remoteDb.getUsername(),
////            "-d", remoteDb.getDbName(),
////            "-F", "c",
////            "-f", backupPath
////        );
////
////        processBuilder.environment().put("PGPASSWORD", remoteDb.getPassword());
////
////        Process process = null;
////
////        try 
////        {
////            process = processBuilder.start();
////
////            try (InputStream inputStream = process.getInputStream();
////                 InputStream errorStream = process.getErrorStream()) 
////            {
////
////                int exitCode = process.waitFor();
////
////                if (exitCode == 0)
////                {
////                    System.out.println("Remote Backup successful!");
////
////                    String zipFile = "dbBackup_" + timestamp + ".zip";
////                    String zipPathC = C_BACKUP_DIR + zipFile;
////                    String zipPathD = D_BACKUP_DIR + zipFile;
////
////                    zipBackup(backupPath, zipPathC);
////                    zipBackup(backupPath, zipPathD);
////
////                    Files.deleteIfExists(Paths.get(backupPath));
////
////                    manageBackupQueue(zipPathC, C_BACKUP_DIR, backupQueueC);
////                    manageBackupQueue(zipPathD, D_BACKUP_DIR, backupQueueD);
////
////                    BackupHistory backupRecord = new BackupHistory(timestamp, zipPathC, zipPathD, "Available");
////                    backupHistoryRepository.save(backupRecord);
////                } 
////                else 
////                {
////                    System.out.println("Remote Backup failed! Exit Code: " + exitCode);
////                    
////                    // Read and log the error message from the process
////                    String errorMessage = new String(errorStream.readAllBytes());
////                    System.out.println("Backup Process Error: " + errorMessage);
////                }
////            }
////        }
////        
////        catch (IOException | InterruptedException e) 
////        {
////            System.out.println("Remote Backup failed! Error: " + e.getMessage());
////            e.printStackTrace();
////        }
////        
////        finally 
////        {
////            if (process != null) 
////            {
////                process.destroy();
////            }
////        }
////    }
//
//    public void backupDatabase(RemoteDbCredentials remoteDb) {
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
//        String backupFile = "dbBackup_" + timestamp + ".sql";
//        String backupPath = D_BACKUP_DIR + backupFile;
//
//        // Store the initial "Pending" backup record
//        BackupHistory pendingBackup = new BackupHistory();
//        pendingBackup.setCreatedTime(remoteDb.getBackupTime() != null ? remoteDb.getBackupTime() : timestamp);
//        pendingBackup.setBackupPathC("");  // Paths are empty before execution
//        pendingBackup.setBackupPathD("");
//        pendingBackup.setStatus("Pending"); // Initial status
//        backupHistoryRepository.save(pendingBackup);
//
//        ProcessBuilder processBuilder = new ProcessBuilder(
//            "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
//            "-h", remoteDb.getIp(),
//            "-p", remoteDb.getPort(),
//            "-U", remoteDb.getUsername(),
//            "-d", remoteDb.getDbName(),
//            "-F", "c",
//            "-f", backupPath
//        );
//
//        processBuilder.environment().put("PGPASSWORD", remoteDb.getPassword());
//
//        Process process = null;
//        try {
//            process = processBuilder.start();
//            try (InputStream inputStream = process.getInputStream();
//                 InputStream errorStream = process.getErrorStream()) {
//
//                int exitCode = process.waitFor();
//
//                if (exitCode == 0) {
//                    System.out.println("Remote Backup successful!");
//
//                    String zipFile = "dbBackup_" + timestamp + ".zip";
//                    String zipPathC = C_BACKUP_DIR + zipFile;
//                    String zipPathD = D_BACKUP_DIR + zipFile;
//
//                    zipBackup(backupPath, zipPathC);
//                    zipBackup(backupPath, zipPathD);
//                    Files.deleteIfExists(Paths.get(backupPath));
//
//                    manageBackupQueue(zipPathC, C_BACKUP_DIR, backupQueueC);
//                    manageBackupQueue(zipPathD, D_BACKUP_DIR, backupQueueD);
//
//                    // Update the backup record to "Available"
//                    pendingBackup.setBackupPathC(zipPathC);
//                    pendingBackup.setBackupPathD(zipPathD);
//                    pendingBackup.setStatus("Available");
//                    backupHistoryRepository.save(pendingBackup);
//
//                } else {
//                    System.out.println("Remote Backup failed! Exit Code: " + exitCode);
//                    String errorMessage = new String(errorStream.readAllBytes());
//                    System.out.println("Backup Process Error: " + errorMessage);
//                }
//            }
//        } catch (IOException | InterruptedException e) {
//            System.out.println("Remote Backup failed! Error: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            if (process != null) {
//                process.destroy();
//            }
//        }
//    }

    
    public void backupDatabase() {
        if (lastEnteredCredentials == null) {
            System.out.println("No database credentials provided yet, using default credentials.");
            lastEnteredCredentials = new RemoteDbCredentials();
            lastEnteredCredentials.setIp("localhost");
            lastEnteredCredentials.setPort("5432");
            lastEnteredCredentials.setDbName("second_db");
            lastEnteredCredentials.setUsername("postgres");
            lastEnteredCredentials.setPassword("root");
        }
        backupDatabase(lastEnteredCredentials);
    }

    @Transactional
    public void backupDatabase(RemoteDbCredentials remoteDb)
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String backupFile = "dbBackup_" + timestamp + ".sql";
        String backupPath = D_BACKUP_DIR + backupFile;

        // Find pending backup entry (latest one)
        BackupHistory pendingBackup = backupHistoryRepository.findTopByStatusOrderByCreatedTimeDesc("Pending");

        if (pendingBackup == null) {
            System.out.println("No pending backup found!");
            return;
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
            "-h", remoteDb.getIp(),
            "-p", remoteDb.getPort(),
            "-U", remoteDb.getUsername(),
            "-d", remoteDb.getDbName(),
            "-F", "c",
            "-f", backupPath
        );

        processBuilder.environment().put("PGPASSWORD", remoteDb.getPassword());

        Process process = null;
        try 
        {
            process = processBuilder.start();
            try (InputStream inputStream = process.getInputStream();
                 InputStream errorStream = process.getErrorStream())
            {

                int exitCode = process.waitFor();

                if (exitCode == 0) 
                {
                    System.out.println("Remote Backup successful!");

                    String zipFile = "dbBackup_" + timestamp + ".zip";
                    String zipPathC = C_BACKUP_DIR + zipFile;
                    String zipPathD = D_BACKUP_DIR + zipFile;

                    zipBackup(backupPath, zipPathC);
                    zipBackup(backupPath, zipPathD);
                    Files.deleteIfExists(Paths.get(backupPath));

                    manageBackupQueue(zipPathC, C_BACKUP_DIR, backupQueueC);
                    manageBackupQueue(zipPathD, D_BACKUP_DIR, backupQueueD);

                    // Update "Pending" record to "Available"
//                    if (pendingBackup != null) {
//                        pendingBackup.setBackupPathC(zipPathC);
//                        pendingBackup.setBackupPathD(zipPathD);
//                        pendingBackup.setStatus("Available");
//                        backupHistoryRepository.save(pendingBackup);
//                    }
                    
                    if (pendingBackup != null)
                    {

                    	try 
                    	{
                    	    pendingBackup = backupHistoryRepository.findById(pendingBackup.getId()).orElseThrow();
                    	    pendingBackup.setBackupPathC(zipPathC);
                    	    pendingBackup.setBackupPathD(zipPathD);
                    	    pendingBackup.setStatus("Available");
                    	    backupHistoryRepository.save(pendingBackup);
                    	    backupHistoryRepository.flush();
                    	    System.out.println("Backup updated successfully in DB!");
                    	}
                    	
                    	catch (Exception e) 
                    	{
                    	    System.out.println("Error updating backup: " + e.getMessage());
                    	    e.printStackTrace();
                    	}
                    }
                } 
                
                else 
                {
                    System.out.println("Remote Backup failed! Exit Code: " + exitCode);
                    String errorMessage = new String(errorStream.readAllBytes());
                    System.out.println("Backup Process Error: " + errorMessage);
                }
            }
        } 
        
        catch (IOException | InterruptedException e) 
        {
            System.out.println("Remote Backup failed! Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        finally 
        {
            if (process != null) 
            {
                process.destroy();
            }
        }
    }

    
    
	public void zipBackup(String sourceFilePath, String zipFilePath)
	{
	    try (FileOutputStream fos = new FileOutputStream(zipFilePath);
	         ZipOutputStream zos = new ZipOutputStream(fos)) 
	    {

	        File fileToZip = new File(sourceFilePath);
	        Path filePath = Paths.get(sourceFilePath); 
	        
	        zos.putNextEntry(new ZipEntry(fileToZip.getName()));
	        Files.copy(filePath, zos);
	        zos.closeEntry();

	        System.out.println("Backup zipped: " + zipFilePath);
	    } 
	    
	    catch (IOException e) 
	    {
	        System.err.println("Error creating ZIP file: " + zipFilePath);
	        e.printStackTrace();
	    }
	}
	
	private void manageBackupQueue(String newBackup, String backupDir, Queue<String> backupQueue) 
	{
        backupQueue.add(newBackup);

        if (backupQueue.size() > MAX_BACKUPS) 
        {
            String oldestBackup = backupQueue.poll();
            
            if (oldestBackup != null) 
            {
                try 
                {
                    Files.deleteIfExists(Paths.get(oldestBackup));
                    System.out.println("Deleted old backup: " + oldestBackup);
                    
                    Optional<BackupHistory> optionalBackup = backupHistoryRepository.findByBackupPathCOrBackupPathD(oldestBackup, oldestBackup); 			
                    if (optionalBackup.isPresent())
                    {
                        BackupHistory backup = optionalBackup.get();
                        backup.setStatus("Not Available");  // Mark as deleted
                        backupHistoryRepository.save(backup);
                    }
                }
                
                catch (IOException e) 
                {
                    System.err.println("Failed to delete old backup: " + oldestBackup);
                    e.printStackTrace();
                }
            }
        }
     }
	
	 public List<BackupHistory> getBackupHistory() 
	 {
	      Pageable pageable = PageRequest.of(0, 10);
	      Page<BackupHistory> page = backupHistoryRepository.findLatestBackups(pageable);
	      return page.getContent();  // Extracts only the list of 10 latest backups
	 }
}	
	
	

//process.destroy();


//public List<BackupHistory> getBackupHistory() 
//{
//	  return backupHistoryRepository.findAll();
//}

//public List<BackupHistory> getBackupHistory() 
//{
//	  return backupHistoryRepository.findLatestBackups(PageRequest.of(0, 10)).getContent();
//}

//public List<BackupHistory> getBackupHistory() {
//   return backupHistoryRepository.findTop10ByOrderByIdDesc();
//}

























//	private void manageBackupQueue(String newBackup, String backupDir)
//	{
//        backupQueue.add(newBackup); // Add new backup to the queue
//        
//        if (backupQueue.size() > MAX_BACKUPS)
//        {
//            String oldestBackup = backupQueue.poll(); // Remove the oldest backup
//            if (oldestBackup != null) 
//            {
//                try 
//                {
//                    Files.deleteIfExists(Paths.get(oldestBackup)); // Delete file
//                    System.out.println("Deleted old backup: " + oldestBackup);
//                } 
//                catch (IOException e) 
//                {
//                    System.err.println("Failed to delete old backup: " + oldestBackup);
//                    e.printStackTrace();
//                }
//            }
//        }
//    }



















//extra
//public void zipBackup(String sourceFilePath, String zipFilePath)
//{
//	try (FileOutputStream fos = new FileOutputStream(zipFilePath);
//             ZipOutputStream zos = new ZipOutputStream(fos))
//	{
//
//            File fileToZip = new File(sourceFilePath);
//            try (FileSystem fileSystem = FileSystems.getDefault())
//            {
//                Path filePath = fileSystem.getPath(sourceFilePath);
//                zos.putNextEntry(new ZipEntry(fileToZip.getName()));
//                Files.copy(filePath, zos);
//                zos.closeEntry();
//            }
//
//            System.out.println("Backup zipped: " + zipFilePath);
//        } 
//	catch (IOException e) 
//	{
//            System.err.println("Error creating ZIP file: " + zipFilePath);
//            e.printStackTrace();
//    }
//}












//----------code-------------------




//package com.project.service;

//import java.io.IOException;

//import org.springframework.stereotype.Service;

//@Service
//public class BackupService 
//{
//	public void backupDatabase()
//	{
//		ProcessBuilder processBuilder = new ProcessBuilder(
//				"C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
//				"-U", "postgres",
//				"-d", "second_db", 
//				"-F", "c",
//				"-f", "D:\\Backup\\dbBackup.sql");
//		
//		processBuilder.environment().put("PGPASSWORD", "root");
//		
//		try 
//		{
//			Process process = processBuilder.start();
			
//			int exitCode = process.waitFor();
//			
//			if(exitCode == 0)
//			{
//				System.out.println("Backup successful!");
//			}
//			else
//			{
//				System.out.println("Backup failed!");
//			}			
//		}
		
//		catch(IOException | InterruptedException e)
//		{
//			e.printStackTrace();
//		}
//	}
//}




//package com.project.service;

//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.LinkedList;
//import java.util.Queue;

//import org.springframework.stereotype.Service;

//@Service
//public class BackupService 
//{
//	private static final String BACKUP_DIR = "D:\\Backup";
//	private static final int MAX_BACKUPS = 5;
//	private static final Queue<String> backupQueue = new LinkedList<>();
//	
//	public void backupDatabase()
//	{
//		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
//		String backupFile = BACKUP_DIR + "dbBackup_" + timestamp + ".sql";
	
//		ProcessBuilder processBuilder = new ProcessBuilder(
//				"C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
//				"-U", "postgres",
//				"-d", "second_db", 
//				"-F", "c",
//				"-f", backupFile);
		
//		processBuilder.environment().put("PGPASSWORD", "root");
		
//		try 
//		{
//			Process process = processBuilder.start();
//			
//			int exitCode = process.waitFor();
//			
//			if(exitCode == 0)
//			{
//				System.out.println("Backup successful!");
//				manageBackupQueue(backupFile);
//			}
//			else
//			{
//				System.out.println("Backup failed!");
//			}			
//		}
		
//		catch(IOException | InterruptedException e)
//		{
//			e.printStackTrace();
//		}
//	}
//	private void manageBackupQueue(String newBackup)
//	{
//		backupQueue.add(newBackup);
		
//		if(backupQueue.size() > MAX_BACKUPS)
//		{
//			String oldestBackup = backupQueue.poll();		//removes the oldest one
			
//			if(oldestBackup != null)
//			{
//				try 
//				{
//					Files.deleteIfExists(Paths.get(oldestBackup));  	//delete file
//					System.out.println("Deleted old backup file: "+oldestBackup);
//				}
//				catch (IOException e) 
//				{
//					System.err.println("Failed to delete old backup: "+oldestBackup);
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//}

//----------code-------------------

//public void backupDatabase()
//{
//	String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
//	String backupFile ="dbBackup_" + timestamp + ".sql";
//	String backupPath = D_BACKUP_DIR + backupFile;
	
//	ProcessBuilder processBuilder = new ProcessBuilder(
//			"C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
//			"-U", "postgres",
//			"-d", "second_db", 
//			"-F", "c",
//			"-f", backupPath);
	
//	processBuilder.environment().put("PGPASSWORD", "root");
	
//	try 
//	{
//		Process process = processBuilder.start();
		
//		int exitCode = process.waitFor();
		
//		if(exitCode == 0)
//		{
//			System.out.println("Backup successful!");
			
//			String zipFile = "dbBackup_" + timestamp + ".zip";
			
//			String zipPathC = C_BACKUP_DIR + zipFile;
//			String zipPathD = D_BACKUP_DIR + zipFile;

//			zipBackup(backupPath, zipPathC);
//			zipBackup(backupPath, zipPathD);

//			// Delete original .sql file after zipping
//            Files.deleteIfExists(Paths.get(backupPath));

//            // Manage backup queue (limit to 5 backups)
//            manageBackupQueue(zipPathC, C_BACKUP_DIR, backupQueueC);
//            manageBackupQueue(zipPathD, D_BACKUP_DIR, backupQueueD);
            
//            // Add backup details to history
//            BackupHistory backupRecord = new BackupHistory(timestamp, zipPathC, zipPathD, "Available");
//            backupHistoryRepository.save(backupRecord);  // Save to database

//            // Keep only last MAX_BACKUPS entries
//            while (backupHistory.size() > MAX_BACKUPS) 
//            {
//                backupHistory.remove(0);
//            }
//        }
		
//		else
//		{
//			System.out.println("Backup failed!");
//		}			
//	}
			
//	catch(IOException | InterruptedException e)
//	{
//		e.printStackTrace();
//	}
//}



//Runs the pg_dump command to create a PostgreSQL database backup.
//Uses ProcessBuilder to execute the system command.

//Process process = processBuilder.start();
//int exitCode = process.waitFor();
//Starts the backup process and waits for it to complete before proceeding.


//if (exitCode == 0) 
//    System.out.println("Backup successful!");
//Ensures that the backup completed successfully before proceeding.


//File fileToZip = new File(sourceFilePath);
//Path filePath = Paths.get(sourceFilePath);

//File fileToZip = new File(sourceFilePath);
//Creates a File object representing the backup .sql file that needs to be zipped.

//Path filePath = Paths.get(sourceFilePath);
//Converts the file path string into a Path object for easier file operations.

//zos.putNextEntry(new ZipEntry(fileToZip.getName()));
//new ZipEntry(fileToZip.getName())

//Creates a new entry in the ZIP file with the same name as the original backup file (.sql).
//Example: If sourceFilePath = "D:\\Backup\\dbBackup_20250227_1200.sql",
//The ZIP entry name inside the .zip file will be dbBackup_20250227_1200.sql.
//zos.putNextEntry(...)

//Adds the new ZIP entry to the archive.
//This signals the ZipOutputStream that the next data written will belong to this entry.

//Files.copy(filePath, zos);
//Files.copy(filePath, zos);
//Reads the .sql file and writes its binary content directly into the ZIP entry.
//This efficiently transfers file data without needing a manual loop or byte array.

//zos.closeEntry();
//Closes the current ZIP entry.
//Marks the completion of writing the current file inside the ZIP.



/* latest working code
 
  
package com.project.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.project.model.BackupHistory;
import com.project.model.RemoteDbCredentials;
import com.project.repo.BackupHistoryRepository;

@Service
public class BackupService 
{
	
	@Autowired
    private BackupHistoryRepository backupHistoryRepository; 

	private static final String C_BACKUP_DIR = "C:\\Backup\\CBackupFolder";
	private static final String D_BACKUP_DIR = "D:\\Backup\\DBackupFolder";

	private static final int MAX_BACKUPS = 5;			//The backup paths and limit are shared across all method calls.			
	
	private static final Queue<String> backupQueueC = new LinkedList<>();
    private static final Queue<String> backupQueueD = new LinkedList<>();
	    
//    public void backupDatabase()
//    {
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
//        String backupFile = "dbBackup_" + timestamp + ".sql";
//        String backupPath = D_BACKUP_DIR + backupFile;
//
//        ProcessBuilder processBuilder = new ProcessBuilder(
//                "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
//                "-U", "postgres",
//                "-d", "second_db",
//                "-F", "c",
//                "-f", backupPath);
//
//        processBuilder.environment().put("PGPASSWORD", "root");
//
//        Process process = null;
//        
//        try 
//        {
//        	 process = processBuilder.start();
//  
//        	 try (InputStream inputStream = process.getInputStream();
//                  InputStream errorStream = process.getErrorStream()) 
//        	 {
//
//        		 int exitCode = process.waitFor();
//
//        		 if (exitCode == 0) 
//        		 {
//        			 System.out.println("Backup successful!");
//
//                	 String zipFile = "dbBackup_" + timestamp + ".zip";
//                	 String zipPathC = C_BACKUP_DIR + zipFile;
//               	  	 String zipPathD = D_BACKUP_DIR + zipFile;
//
//                	 zipBackup(backupPath, zipPathC);
//                	 zipBackup(backupPath, zipPathD);
//
//                	 // Delete original .sql file after zipping
//                	 Files.deleteIfExists(Paths.get(backupPath));
//
//                	 // Manage backup queue (limit to 5 backups)
//                	 manageBackupQueue(zipPathC, C_BACKUP_DIR, backupQueueC);
//                	 manageBackupQueue(zipPathD, D_BACKUP_DIR, backupQueueD);
//
//                	 // Save backup history to the database
//                	 BackupHistory backupRecord = new BackupHistory(timestamp, zipPathC, zipPathD, "Available");
//                     backupHistoryRepository.save(backupRecord);
//                 }            
//        		 else 
//        		 {
//        			 System.out.println("Backup failed!");
//        		 }
//        	 } 
//        }
//        
//        catch (IOException | InterruptedException e) 
//        {
//            e.printStackTrace();
//        }
//        
//        finally 
//        {
//        	if(process != null)
//        	{
//        		process.destroy();
//        	}
//		}
//    }

    public void backupDatabase() 
    {
        // Default Local Database Credentials
        RemoteDbCredentials localDb = new RemoteDbCredentials();
        localDb.setIp("localhost");
        localDb.setPort("5432");  
        localDb.setDbName("second_db");
        localDb.setUsername("postgres");
        localDb.setPassword("root");

        backupDatabase(localDb); // Calls the method for remote DB with local DB credentials
    }
    
    public void backupDatabase(RemoteDbCredentials remoteDb)
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String backupFile = "dbBackup_" + timestamp + ".sql";
        String backupPath = D_BACKUP_DIR + backupFile;

        ProcessBuilder processBuilder = new ProcessBuilder(
            "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
            "-h", remoteDb.getIp(),
            "-p", remoteDb.getPort(),
            "-U", remoteDb.getUsername(),
            "-d", remoteDb.getDbName(),
            "-F", "c",
            "-f", backupPath
        );

        processBuilder.environment().put("PGPASSWORD", remoteDb.getPassword());

        Process process = null;

        try 
        {
            process = processBuilder.start();

            try (InputStream inputStream = process.getInputStream();
                 InputStream errorStream = process.getErrorStream()) 
            {

                int exitCode = process.waitFor();

                if (exitCode == 0)
                {
                    System.out.println("Remote Backup successful!");

                    String zipFile = "dbBackup_" + timestamp + ".zip";
                    String zipPathC = C_BACKUP_DIR + zipFile;
                    String zipPathD = D_BACKUP_DIR + zipFile;

                    zipBackup(backupPath, zipPathC);
                    zipBackup(backupPath, zipPathD);

                    Files.deleteIfExists(Paths.get(backupPath));

                    manageBackupQueue(zipPathC, C_BACKUP_DIR, backupQueueC);
                    manageBackupQueue(zipPathD, D_BACKUP_DIR, backupQueueD);

                    BackupHistory backupRecord = new BackupHistory(timestamp, zipPathC, zipPathD, "Available");
                    backupHistoryRepository.save(backupRecord);
                } 
                else 
                {
                    System.out.println("Remote Backup failed! Exit Code: " + exitCode);
                    
                    // Read and log the error message from the process
                    String errorMessage = new String(errorStream.readAllBytes());
                    System.out.println("Backup Process Error: " + errorMessage);
                }
            }
        }
        
        catch (IOException | InterruptedException e) 
        {
            System.out.println("Remote Backup failed! Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        finally 
        {
            if (process != null) 
            {
                process.destroy();
            }
        }
    }

    
//    public void backupDatabase(RemoteDbCredentials remoteDb) {
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
//        String backupFile = "dbBackup_" + timestamp + ".sql";
//        String backupPath = D_BACKUP_DIR + backupFile;
//
//        ProcessBuilder processBuilder = new ProcessBuilder(
//            "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
//            "-h", remoteDb.getIp(),
//            "-p", remoteDb.getPort(),
//            "-U", remoteDb.getUsername(),
//            "-d", remoteDb.getDbName(),
//            "-F", "c",
//            "-f", backupPath
//        );
//
//        processBuilder.environment().put("PGPASSWORD", remoteDb.getPassword());
//
//        Process process = null;
//
//        try {
//            process = processBuilder.start();
//
//            try (InputStream inputStream = process.getInputStream();
//                 InputStream errorStream = process.getErrorStream()) {
//
//                int exitCode = process.waitFor();
//
//                if (exitCode == 0) {
//                    System.out.println("Remote Backup successful!");
//
//                    String zipFile = "dbBackup_" + timestamp + ".zip";
//                    String zipPathC = C_BACKUP_DIR + zipFile;
//                    String zipPathD = D_BACKUP_DIR + zipFile;
//
//                    zipBackup(backupPath, zipPathC);
//                    zipBackup(backupPath, zipPathD);
//
//                    Files.deleteIfExists(Paths.get(backupPath));
//
//                    manageBackupQueue(zipPathC, C_BACKUP_DIR, backupQueueC);
//                    manageBackupQueue(zipPathD, D_BACKUP_DIR, backupQueueD);
//
//                    BackupHistory backupRecord = new BackupHistory(timestamp, zipPathC, zipPathD, "Available");
//                    backupHistoryRepository.save(backupRecord);
//                }
//                else 
//                {
////                    System.out.println("Remote Backup failed!");
//                	try {
//                	    // Your backup logic here
//                	} catch (Exception e) {
//                	    System.out.println("Remote Backup failed! Error: " + e.getMessage());
//                	    e.printStackTrace(); // Print full stack trace for debugging
//                	}
//
//                }
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            if (process != null) {
//                process.destroy();
//            }
//        }
//    }

	public void zipBackup(String sourceFilePath, String zipFilePath)
	{
	    try (FileOutputStream fos = new FileOutputStream(zipFilePath);
	         ZipOutputStream zos = new ZipOutputStream(fos)) 
	    {

	        File fileToZip = new File(sourceFilePath);
	        Path filePath = Paths.get(sourceFilePath); 
	        
	        zos.putNextEntry(new ZipEntry(fileToZip.getName()));
	        Files.copy(filePath, zos);
	        zos.closeEntry();

	        System.out.println("Backup zipped: " + zipFilePath);
	    } 
	    
	    catch (IOException e) 
	    {
	        System.err.println("Error creating ZIP file: " + zipFilePath);
	        e.printStackTrace();
	    }
	}
	
	private void manageBackupQueue(String newBackup, String backupDir, Queue<String> backupQueue) 
	{
        backupQueue.add(newBackup);

        if (backupQueue.size() > MAX_BACKUPS) 
        {
            String oldestBackup = backupQueue.poll();
            
            if (oldestBackup != null) 
            {
                try 
                {
                    Files.deleteIfExists(Paths.get(oldestBackup));
                    System.out.println("Deleted old backup: " + oldestBackup);
                    
                    Optional<BackupHistory> optionalBackup = backupHistoryRepository.findByBackupPathCOrBackupPathD(oldestBackup, oldestBackup); 			
                    if (optionalBackup.isPresent())
                    {
                        BackupHistory backup = optionalBackup.get();
                        backup.setStatus("Not Available");  // Mark as deleted
                        backupHistoryRepository.save(backup);
                    }
                }
                
                catch (IOException e) 
                {
                    System.err.println("Failed to delete old backup: " + oldestBackup);
                    e.printStackTrace();
                }
            }
        }
     }
	
	 public List<BackupHistory> getBackupHistory() 
	 {
	      Pageable pageable = PageRequest.of(0, 10);
	      Page<BackupHistory> page = backupHistoryRepository.findLatestBackups(pageable);
	      return page.getContent();  // Extracts only the list of 10 latest backups
	 }
}	
	
	
  
 latest working code */








