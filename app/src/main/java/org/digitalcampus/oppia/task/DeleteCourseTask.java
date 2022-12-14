package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.List;

public class DeleteCourseTask extends AsyncTask<Course, String, BasicResult> {

    public static final String TAG = DeleteCourseTask.class.getSimpleName();

    private DeleteCourseListener mStateListener;
    private Context ctx;

    public DeleteCourseTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected BasicResult doInBackground(Course... params) {

        Course course = params[0];

        try {
            DbHelper db = DbHelper.getInstance(ctx);
            List<Media> courseMedia = db.getCourseMedia(course.getCourseId());
            db.deleteCourse(course.getCourseId());

            // remove files
            String courseLocation = course.getLocation();
            File f = new File(courseLocation);
            boolean exists = f.exists();
            boolean success = exists && FileUtils.deleteDir(f);

            File courseBackup = CourseInstall.savedBackupCourse(ctx, course.getShortname());
            if (success && courseBackup != null) {
                FileUtils.deleteFile(courseBackup);
            }

            deleteCourseMedia(courseMedia);

            return BasicResult.SUCCESS;
        } catch (NullPointerException npe) {

        }
        return BasicResult.ERROR;
    }

    private void deleteCourseMedia(List<Media> courseMedia) {
        DbHelper db = DbHelper.getInstance(ctx);
        for (Media media : courseMedia) {
            String filename = Storage.getMediaPath(ctx) + media.getFilename();
            File mediaFile = new File(filename);
            if (mediaFile.exists() && db.getMediaUsages(media.getFilename()) == 0) {
                // The media file was only used by this course, we can delete it
                boolean success = mediaFile.delete();
                Log.d(TAG, "Removing unused media file " + media.getFilename() +
                        (success ? " successfully" : "failed"));
            }

        }
    }

    @Override
    protected void onPostExecute(BasicResult result) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.onCourseDeletionComplete(result);
            }
        }
    }

    public void setOnDeleteCourseListener(DeleteCourseListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
