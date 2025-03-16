package com.project.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.model.BackupHistory;

@Repository
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Integer>
{
	@Query("SELECT b FROM BackupHistory b ORDER BY b.id DESC")
    Page<BackupHistory> findLatestBackups(Pageable pageable);
	
    Optional<BackupHistory> findByBackupPathCOrBackupPathD(String backupPathC, String backupPathD);
    
    // Find the latest pending backup
    BackupHistory findTopByStatusOrderByCreatedTimeDesc(String status);
    
    // Fetch all backups sorted by created time
    List<BackupHistory> findAllByOrderByCreatedTimeDesc();
}



//@Query("SELECT b FROM backup_history b  ORDER BY b.id DESC LIMIT 1")
//@Query("SELECT u FROM BackupHistory u ORDER BY u.id DESC limit 10")



