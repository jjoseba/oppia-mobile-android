package Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.storage.*;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CourseUtils {

    public static String getCourseTitle(Context ctx){

        File dir = new File(Storage.getDownloadPath(ctx));
        String[] children = dir.list();

        String courseXMLPath = "";
        CourseXMLReader cxr = null;
        File tempdir = new File(Storage.getStorageLocationRoot(ctx) + "temp/");
        tempdir.mkdirs();

        boolean unzipResult = org.digitalcampus.oppia.utils.storage.FileUtils.unzipFiles(Storage.getDownloadPath(ctx), children[0], tempdir.getAbsolutePath());


        if (!unzipResult){
            //then was invalid zip file and should be removed
            FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + children[0]);

        }

        String[] courseDirs = tempdir.list();
        Course c = null;
        SharedPreferences prefs = null;

        try {
            courseXMLPath = tempdir + File.separator + courseDirs[0] + File.separator + MobileLearning.COURSE_XML;
            cxr = new CourseXMLReader(courseXMLPath, 0, ctx);

            prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            c = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
            c.getMultiLangInfo().setTitles(cxr.getTitles());

        } catch (ArrayIndexOutOfBoundsException aioobe){
            org.digitalcampus.oppia.utils.storage.FileUtils.cleanUp(tempdir, Storage.getDownloadPath(ctx) + children[0]);
            return null;
        } catch (InvalidXMLException e) {
            e.printStackTrace();
            return null;
        } catch(Exception e ){
            e.printStackTrace();
            return null;
        }

        return c.getMultiLangInfo().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
    }

    public static void cleanUp(){
        //Clean course folders and database
        try {

            Context ctx = InstrumentationRegistry.getTargetContext();

            //Clean downloads folder
            File downloadsFolder = new File(Storage.getDownloadPath(ctx));
            cleanDirectory(downloadsFolder);

            //Clean courses folder
            File coursesFolder = new File(Storage.getCoursesPath(ctx));
            cleanDirectory(coursesFolder);

            //Clean temp folder
            File tempFolder = new File(Storage.getStorageLocationRoot(ctx) + "temp/");
            if (tempFolder.exists()) {
                cleanDirectory(tempFolder);
            }

            //Clean database
            DbHelper db = DbHelper.getInstance(ctx);
            for(Course c : db.getAllCourses()){
                db.deleteCourse(c.getCourseId());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Course createMockCourse(){

        Course mockCourse = mock(Course.class);

        when(mockCourse.getNoActivities()).thenReturn(3);
        doNothing().when(mockCourse.getMultiLangInfo()).setTitlesFromJSONString(anyString());

        when(mockCourse.getCourseId()).thenReturn(999);


        Section mockSection = mock(Section.class);
        when(mockSection.getMultiLangInfo().getTitle(anyString())).thenReturn("Mock Section");


        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getCourseId()).thenReturn((long) 999);
        when(mockActivity.getActId()).thenReturn(999);
        when(mockActivity.getSectionId()).thenReturn(999);

        mockSection.addActivity(mockActivity);


        return mockCourse;

    }
}