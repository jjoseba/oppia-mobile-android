/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.service.courseinstall;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CourseInstallerService extends IntentService {

    public static final String TAG = CourseInstallerService.class.getSimpleName();
    public static final String BROADCAST_ACTION = "com.digitalcampus.oppia.COURSEINSTALLERSERVICE";

    public static final String SERVICE_ACTION = "action";
    public static final String SERVICE_URL = "fileurl"; //field for providing file URL
    public static final String SERVICE_SHORTNAME = "shortname"; //field for providing Course shortname
    public static final String SERVICE_VERSIONID = "versionid"; //field for providing file URL
    public static final String SERVICE_MESSAGE = "message";

    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_DOWNLOAD = "download";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_INSTALL = "install";
    public static final String ACTION_COMPLETE = "complete";
    public static final String ACTION_FAILED = "failed";

    private ArrayList<String> tasksCancelled;
    private ArrayList<String> tasksDownloading;

    private static CourseInstallerService currentInstance;

    private static void setInstance(CourseInstallerService instance){
        currentInstance = instance;
    }

    public static List<String> getTasksDownloading(){
        if (currentInstance != null){
            synchronized (currentInstance){
                return currentInstance.tasksDownloading;
            }
        }
        return new ArrayList<>();
    }

    public CourseInstallerService() {
        super(TAG);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        CourseInstallerService.setInstance(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.hasExtra(SERVICE_ACTION) && intent.hasExtra(SERVICE_URL)) {
            // Set the canceling flag to that file
            if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_CANCEL)){
                Log.d(TAG, "CANCEL commmand received");
                addCancelledTask(intent.getStringExtra(SERVICE_URL));
            }
            else if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_DOWNLOAD) ||
                    intent.getStringExtra(SERVICE_ACTION).equals(ACTION_UPDATE)) {
                addDownloadingTask(intent.getStringExtra(SERVICE_URL));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.hasExtra (SERVICE_ACTION)) {
            boolean cancel = intent.getStringExtra(SERVICE_ACTION).equals(ACTION_CANCEL);
            //We have nothing more to do with a 'cancel' action than what is done in onStartCommand()
            if (cancel) { return; }
        }

        if (!intent.hasExtra(SERVICE_URL)){
            Log.d(TAG, "No Course passed to the service. Invalid task");
            return;
        }

        if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_DOWNLOAD)){
            final String fileUrl = intent.getStringExtra(SERVICE_URL);
            final String shortname = intent.getStringExtra(SERVICE_SHORTNAME);
            Double versionID = intent.getDoubleExtra(SERVICE_VERSIONID, 0);
            String filename = Course.getLocalFilename(shortname, versionID);

            if (isCancelled(fileUrl)) {
                //If it was cancelled before starting, we do nothing
                Log.d(TAG, "Course " + fileUrl + " cancelled before started.");
                removeCancelled(fileUrl);
                removeDownloading(fileUrl);
                return;
            }
            boolean success = downloadCourseFile(fileUrl, shortname, versionID);
            if (success){
                CourseInstall.installDownloadedCourse(this, filename, shortname, new CourseInstall.CourseInstallingListener() {
                    @Override
                    public void onInstallProgress(int progress) {
                        sendBroadcast(fileUrl, ACTION_INSTALL, ""+progress);
                    }

                    @Override
                    public void onError(String message) {
                        sendBroadcast(fileUrl, ACTION_FAILED, message);
                        removeDownloading(fileUrl);
                    }

                    @Override
                    public void onFail(String message) {
                        onError(message);
                    }

                    @Override
                    public void onComplete() {
                        removeDownloading(fileUrl);
                        sendBroadcast(fileUrl, ACTION_COMPLETE, null);
                    }
                });

            }
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        CourseInstallerService.setInstance(null);
    }

    private void logAndNotifyError(String fileUrl, Exception e){
        Log.d(TAG, "Error: " + e.getMessage(), e);
        sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
        removeDownloading(fileUrl);
    }

    /*
    * Sends a new Broadcast with the results of the action
    */
    private void sendBroadcast(String fileUrl, String result, String message){

        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(SERVICE_ACTION, result);
        localIntent.putExtra(SERVICE_URL, fileUrl);
        if (message != null){
            localIntent.putExtra(SERVICE_MESSAGE, message);
        }
        // Broadcasts the Intent to receivers in this app.
        Log.d(TAG, fileUrl + "=" + result + ":" + message);
        sendOrderedBroadcast(localIntent, null);

    }

    private void addCancelledTask(String fileUrl){
        if (tasksCancelled == null){
            tasksCancelled = new ArrayList<>();
        }
        if (!tasksCancelled.contains(fileUrl)){
            tasksCancelled.add(fileUrl);
        }
    }

    private boolean isCancelled(String fileUrl){
        return (tasksCancelled != null) && (tasksCancelled.contains(fileUrl));
    }

    private boolean removeCancelled(String fileUrl) {
        return tasksCancelled != null && tasksCancelled.remove(fileUrl);
    }

    private void addDownloadingTask(String fileUrl){
        if (tasksDownloading == null){
            tasksDownloading = new ArrayList<>();
        }
        if (!tasksDownloading.contains(fileUrl)){
            synchronized (this){
                tasksDownloading.add(fileUrl);
            }
        }
    }

    private boolean removeDownloading(String fileUrl){
        if (tasksDownloading != null){
            synchronized (this){
                return tasksDownloading.remove(fileUrl);
            }
        }
        return false;
    }

    private boolean downloadCourseFile(String fileUrl, String shortname, Double versionID){

        long startTime = System.currentTimeMillis();
        File downloadedFile = null;
        FileOutputStream f = null;

        try {
        	DbHelper db = DbHelper.getInstance(this);
        	User u = db.getUser(SessionManager.getUsername(this));

            OkHttpClient client = HTTPClientUtils.getClient(this);
            Request request = new Request.Builder()
                    .url(HTTPClientUtils.getUrlWithCredentials(fileUrl, u.getUsername(), u.getApiKey()))
                    .build();

            Response response = client.newCall(request).execute();

            long fileLength = response.body().contentLength();
            Log.d(TAG, "Content-length: " + fileLength);
            long availableStorage = Storage.getAvailableStorageSize(this);

            if (fileLength >= availableStorage){
                sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_insufficient_storage_available));
                removeDownloading(fileUrl);
                return false;
            }

            String localFileName = Course.getLocalFilename(shortname, versionID);
            downloadedFile = new File(Storage.getDownloadPath(this),localFileName);
            f = new FileOutputStream(downloadedFile);
            InputStream in = response.body().byteStream();

            byte[] buffer = new byte[8192];
            int len1;
            long total = 0;
            int previousProgress = 0;
            int progress;
            while ((len1 = in.read(buffer)) > 0) {
                //If received a cancel action while downloading, stop it
                if (isCancelled(fileUrl)) {
                    Log.d(TAG, "Course " + localFileName + " cancelled while downloading. Deleting temp file...");
                    FileUtils.deleteFile(downloadedFile);
                    removeCancelled(fileUrl);
                    removeDownloading(fileUrl);
                    return false;
                }

                total += len1;
                progress = (int)((total*100)/fileLength);
                if ( (progress > 0) && (progress > previousProgress)){
                    sendBroadcast(fileUrl, ACTION_DOWNLOAD, ""+progress);
                    previousProgress = progress;
                }
                f.write(buffer, 0, len1);
            }
            in.close();
            f.flush();
        } catch (MalformedURLException e) {
            logAndNotifyError(fileUrl, e);
            return false;
        } catch (IOException e) {
            FileUtils.deleteFile(downloadedFile);
            logAndNotifyError(fileUrl, e);
            return false;
        } catch (UserNotFoundException unfe) {
            logAndNotifyError(fileUrl, unfe);
            return false;
		} finally {
            if (f != null){
                try {
                    f.close();
                } catch (IOException ioe) {
                    Log.d(TAG, "couldn't close FileOutputStream object", ioe);
                }
            }
        }

        Log.d(TAG, fileUrl + " succesfully downloaded");
        removeDownloading(fileUrl);
        sendBroadcast(fileUrl, ACTION_INSTALL, "0");

        long estimatedTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "MeasureTime - " + ": " + estimatedTime + "ms");
        return true;
    }

}
