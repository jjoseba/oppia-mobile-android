package org.digitalcampus.oppia.service.courseinstall;

import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.service.FileIntentService;

import java.util.ArrayList;

public class CourseInstallerServiceDelegate {

    public void installCourse(Context context, Intent intent, Course courseSelected){
        intent.putExtra(FileIntentService.SERVICE_ACTION, FileIntentService.ACTION_DOWNLOAD);
        intent.putExtra(FileIntentService.SERVICE_URL, courseSelected.getDownloadUrl());
        intent.putExtra(CourseInstallerService.SERVICE_VERSIONID, courseSelected.getVersionId());
        intent.putExtra(CourseInstallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
        intent.putExtra(CourseInstallerService.SERVICE_RESTRICTED, courseSelected.isRestricted());
        intent.putIntegerArrayListExtra(CourseInstallerService.SERVICE_COHORTS, (ArrayList<Integer>) courseSelected.getRestrictedCohorts());
        context.startService(intent);

    }

    public void updateCourse(Context context, Intent intent, Course courseSelected){
        intent.putExtra(FileIntentService.SERVICE_ACTION, FileIntentService.ACTION_UPDATE);
        intent.putExtra(CourseInstallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
        intent.putExtra(CourseInstallerService.SERVICE_RESTRICTED, courseSelected.isRestricted());
        intent.putIntegerArrayListExtra(CourseInstallerService.SERVICE_COHORTS, (ArrayList<Integer>) courseSelected.getRestrictedCohorts());
        context.startService(intent);
    }

    public void cancelCourseInstall(Context context, Intent intent, Course courseSelected){
        intent.putExtra(FileIntentService.SERVICE_ACTION, FileIntentService.ACTION_CANCEL);
        intent.putExtra(FileIntentService.SERVICE_URL, courseSelected.getDownloadUrl());
        context.startService(intent);
    }
}
