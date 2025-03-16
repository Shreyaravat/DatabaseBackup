import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

export interface BackupHistory
{
  id : number;
  createdTime : string;
  backupPathC : string;
  backupPathD : string;
  // dbIp : string;
  // dbName: string;
  // dbUsername : string;
  status : string;
}

@Injectable({
  providedIn: 'root'
})

export class BackupserviceService 
{
  private apiUrl = "http://localhost:8080/api/backup/history";
  // private remoteBackupUrl = "http://localhost:8080/api/backup/remote";
  private scheduleBackupUrl = "http://localhost:8080/api/backup/schedule";


  constructor( private http : HttpClient) { }

  // for displaying latest 10 rows by frontend.
  // getLatestBackupHistory() : Observable<BackupHistory[]> 
  // {
  //   return this.http.get<BackupHistory[]>(this.apiUrl).pipe(
  //     map((history) => history.slice(-10).reverse())  
  //   )
  // }

  getLatestBackupHistory() : Observable<BackupHistory[]> 
  {
    return this.http.get<BackupHistory[]>(this.apiUrl);
  }

  // backupRemoteDatabase(remoteDb: any): Observable<string> {
  //   return this.http.post(this.remoteBackupUrl, remoteDb, { responseType : 'text' });
  // }

  scheduleDatabaseBackup(remoteDb: any): Observable<string> 
  {
    return this.http.post(this.scheduleBackupUrl, remoteDb, { responseType: 'text' });
  }
}





















// Observable: Represents asynchronous data that Angular components can subscribe to.
// The data might not be available immediately (e.g., API call, user input, etc.).
// It allows us to "subscribe" and get updates when the data arrives.
// It supports multiple values over time (e.g., WebSockets, live data streams).
// this.http.get<BackupHistory[]> does not return data immediately.
// Instead, it returns an Observable, which means:
// The data arrives later (when the API responds).
// You need to subscribe to it to get the data.
// The .subscribe() method waits for the data.
// Once the data is received, the function inside next: runs.
// If there is an error, the error: function runs.














