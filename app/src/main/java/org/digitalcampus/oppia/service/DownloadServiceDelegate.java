package org.digitalcampus.oppia.service;

import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.model.Media;

public class DownloadServiceDelegate {

    private Intent getIntent(Context ctx){
        return new Intent(ctx, DownloadService.class);
    }

    public void startDownload(Context context, Media mediaToDownload){
        Intent intent = getIntent(context);
        intent.putExtra(FileIntentService.SERVICE_ACTION, FileIntentService.ACTION_DOWNLOAD);
        intent.putExtra(FileIntentService.SERVICE_URL, mediaToDownload.getDownloadUrl());
        intent.putExtra(DownloadService.SERVICE_DIGEST, mediaToDownload.getDigest());
        intent.putExtra(DownloadService.SERVICE_FILENAME, mediaToDownload.getFilename());
        context.startService(intent);
    }

    public void stopDownload(Context context, Media mediaToDownload) {
        Intent intent = getIntent(context);
        intent.putExtra(FileIntentService.SERVICE_ACTION, FileIntentService.ACTION_CANCEL);
        intent.putExtra(FileIntentService.SERVICE_URL, mediaToDownload.getDownloadUrl());
        context.startService(intent);
    }

}
