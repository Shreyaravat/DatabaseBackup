// import { Component, OnInit } from '@angular/core';
// import { BackupHistory, BackupserviceService } from '../backupservice.service';
// import { CommonModule } from '@angular/common';

// @Component({
//   selector: 'app-dashboard',
//   standalone:true,
//   imports: [CommonModule],
//   templateUrl: './dashboard.component.html',
//   styleUrl: './dashboard.component.css'
// })

// export class DashboardComponent implements OnInit
// {
//     backupHistory: BackupHistory[] = [];

//     constructor(private backupService : BackupserviceService) { }

//     ngOnInit(): void
//     {
//       this.loadBackupHistory();
//     }

//     loadBackupHistory() : void
//     {
//         this.backupService.getLatestBackupHistory().subscribe({
//           next: (data) => {
//             this.backupHistory = data;
//           },
//           error : (error) => {
//             console.error("Error fetching backup history", error);
//           }
//         })
//     }
// }




import { Component, OnInit } from '@angular/core';
import { BackupHistory, BackupserviceService } from '../backupservice.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from  '@angular/forms';
import { response } from 'express';


@Component({
  selector: 'app-dashboard',
  standalone:true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})

export class DashboardComponent implements OnInit
{
    backupHistory: BackupHistory[] = [];
    remoteDb = { ip: '', port: '', dbName: '', username: '', password: '', backupTime: '' };
    
    constructor(private backupService : BackupserviceService) { }

    ngOnInit(): void
    {
      this.loadBackupHistory();
    }

    loadBackupHistory() : void
    {
        this.backupService.getLatestBackupHistory().subscribe({
          next: (data) => {
            this.backupHistory = data;
          },
          error : (error) => {
            console.error("Error fetching backup history", error);
          }
        })
      }

  // backupRemoteDatabase(): void 
  //   {
  //     this.backupService.backupRemoteDatabase(this.remoteDb).subscribe({
  //         next: (response) => { alert(response); },
  //         error: (error) => { console.error("Error starting remote backup", error); }
  //     });
  // }

  scheduleBackup(): void 
  {
    this.backupService.scheduleDatabaseBackup(this.remoteDb).subscribe({
        next: (response) => { alert('Backup Scheduled Successfully!'); },
        error: (error) => { console.error("Error scheduling backup", error); }
    });
  }
}



//   backupRemoteDatabase(): void 
  //   {
  //     this.backupService.backupRemoteDatabase(this.remoteDb).subscribe({
  //         next: () => { alert("Remote database backup initiated"); },
  //         error: (error) => { console.error("Error starting remote backup", error); }
  //     });
  // }