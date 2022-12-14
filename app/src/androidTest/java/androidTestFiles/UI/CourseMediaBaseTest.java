package androidTestFiles.UI;

import android.Manifest;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.io.File;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.database.TestDBHelper;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public abstract class CourseMediaBaseTest extends DaggerInjectMockUITest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    protected static final String COURSE_WITH_NO_MEDIA = "Course_with_no_media.zip";
    protected static final String COURSE_WITH_MEDIA_1 = "Course_with_media_1.zip";
    protected static final String COURSE_WITH_MEDIA_2 = "Course_with_media_2.zip";

    protected static final String MEDIA_FILE_VIDEO_TEST_1 = "video-test-1.mp4";
    protected static final String MEDIA_FILE_VIDEO_TEST_2 = "video-test-2.mp4";

    protected Context context;
    protected TestDBHelper testDBHelper;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // First ensure to use in-memory database
        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();

        CourseUtils.cleanUp();
    }



    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
        CourseUtils.cleanUp();
    }

    protected void copyCourseFromAssets(String filename){
        FileUtils.copyZipFromAssetsPath(context, "courses_media", filename);  //Copy course zip from assets to download path
    }

    protected void copyMediaFromAssets(String filename, String destinationFilename){
        File mediaPath = new File(Storage.getMediaPath(context));
        FileUtils.copyFileFromAssets(context, "courses_media", filename, mediaPath, destinationFilename);  //Copy course zip from assets to download path
    }

    protected void copyMediaFromAssets(String filename){
        copyMediaFromAssets(filename, filename);
    }
}
