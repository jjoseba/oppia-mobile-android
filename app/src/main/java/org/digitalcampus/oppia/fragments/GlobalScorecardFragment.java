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

package org.digitalcampus.oppia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.adapter.ScorecardsGridAdapter;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GlobalScorecardFragment extends AppFragment implements ScorecardsGridAdapter.OnItemClickListener {

    private ScorecardsGridAdapter adapterScorecards;

    @Inject
    CoursesRepository coursesRepository;
    private RecyclerView recyclerScorecards;
    private View emptyState;
    private List<Course> courses = new ArrayList<>();

    public static GlobalScorecardFragment newInstance() {
        return new GlobalScorecardFragment();
    }
    public GlobalScorecardFragment(){
        // Required empty public constructor
    }

    private void initializeDagger() {
        MobileLearning app = (MobileLearning) getActivity().getApplication();
        app.getComponent().inject(this);
    }

    private void findViews(View layout) {
        recyclerScorecards = layout.findViewById(R.id.recycler_scorecards);
        emptyState = layout.findViewById(R.id.empty_state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_global_scorecard, container, false);
        findViews(layout);

        adapterScorecards = new ScorecardsGridAdapter(getActivity(), courses);
        adapterScorecards.setOnItemClickListener(this);
        recyclerScorecards.setAdapter(adapterScorecards);

        return layout;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeDagger();

    }


    @Override
    public void onResume() {
        super.onResume();

        courses.clear();
        courses.addAll(coursesRepository.getCourses(getActivity()));

        showEmptyStateView(courses.isEmpty());

        adapterScorecards.notifyDataSetChanged();

    }

    private void showEmptyStateView(boolean show) {

        if (show) {
            recyclerScorecards.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

            Button download = emptyState.findViewById(R.id.btn_download_courses);
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AdminSecurityManager.with(getActivity()).checkAdminPermission(R.id.menu_download, new AdminSecurityManager.AuthListener() {
                        public void onPermissionGranted() {
                            startActivity(new Intent(getActivity(), TagSelectActivity.class));
                        }
                    });
                }
            });
        } else {
            recyclerScorecards.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {

        Course selectedCourse = adapterScorecards.getItemAtPosition(position);
        Intent i = new Intent(super.getActivity(), CourseIndexActivity.class);
        Bundle tb = new Bundle();
        tb.putSerializable(Course.TAG, selectedCourse);
        i.putExtras(tb);
        startActivity(i);
    }
}
