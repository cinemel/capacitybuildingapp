package androidTestFiles.org.digitalcampus.oppia.activity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.Utils.FileUtils;
import androidTestFiles.Utils.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class TagActivityUITest extends MockedApiEndpointTest {

    @Test
    public void showCorrectCategory() throws Exception {

        startServer(200, "tags/tags.json", 0);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_name))
                    .check(matches(withText(startsWith("Mocked Course Name"))));
        }
    }


    @Test
    public void refreshCachedCourses() throws Exception {

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(PrefsActivity.PREF_SERVER_COURSES_CACHE).commit();

        String responseAsset = "responses/course/response_200_courses_list.json";
        startServer(200, responseAsset, 0);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            String responseBody = FileUtils.getStringFromFile(
                    InstrumentationRegistry.getInstrumentation().getContext(), responseAsset);

            Thread.sleep(200); // Manual waiting for Asynctask. Espresso only waits if there is a view interaction at the end.

            String cachedCourses = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, "");

            assertEquals(responseBody, cachedCourses);
        }
    }


}
