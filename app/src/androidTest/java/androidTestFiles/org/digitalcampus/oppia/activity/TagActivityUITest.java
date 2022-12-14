package androidTestFiles.org.digitalcampus.oppia.activity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static androidTestFiles.Utils.Matchers.RecyclerViewMatcher.withRecyclerView;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.Utils.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class TagActivityUITest extends MockedApiEndpointTest {


    private static final String TAGS_LIVE_DRAFT_RESPONSE = "tags/all_statuses_types.json";
    private static final String TAGS_NO_COURSE_STATUSES_FIELD_RESPONSE = "tags/no_course_statuses_field.json";

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    TagRepository tagRepository;

    @Test
    public void showCorrectCategory() throws Exception {

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            BasicResult result = new BasicResult();
            result.setSuccess(true);
            result.setResultMessage("{}");
            ((TagSelectActivity) ctx).apiRequestComplete(result);
            return null;
        }).when(tagRepository).getTagList(any(), any());


        doAnswer(invocationOnMock -> {
            ArrayList<Tag> tags = (ArrayList<Tag>) invocationOnMock.getArguments()[0];
            tags.add(new Tag() {{
                setName("Mocked Course Name");
                setDescription("Mocked Course Description");
                setCountAvailable(3);
            }});
            return null;
        }).when(tagRepository).refreshTagList(any(), any(), any());

        startServer(200, null);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_name))
                    .check(matches(withText(startsWith("Mocked Course Name"))));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("3")));
        }
    }


    @Test
    public void checkCountOfLiveAndDraftCourses() throws Exception {

        startServer(200, TAGS_LIVE_DRAFT_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("2")));
        }

    }

    @Test
    public void checkCountOfReadOnlyCoursesIfInstalled() throws Exception {

        installCourse("common", "test_course.zip");

        startServer(200, TAGS_LIVE_DRAFT_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("3")));
        }

    }

    @Test
    public void checkCountOfNewDownloadsDisabledCoursesIfInstalled() throws Exception {

        installCourse("common", "test_course_2.zip");

        startServer(200, TAGS_LIVE_DRAFT_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("3")));
        }

    }

    @Test
    public void checkCountOfReadOnlyAndNewDownloadsDisabledCoursesIfInstalled() throws Exception {

        installCourse("common", "test_course.zip");
        installCourse("common", "test_course_2.zip");

        startServer(200, TAGS_LIVE_DRAFT_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("4")));
        }

    }

    @Test
    public void checkCountIfNoCoursesStatusesFieldExists() throws Exception {

        startServer(200, TAGS_NO_COURSE_STATUSES_FIELD_RESPONSE, 0, 2);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.tag_count))
                    .check(matches(withText("3")));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(1, R.id.tag_count))
                    .check(matches(withText("2")));
        }

    }
}
