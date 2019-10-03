package ar.edu.itba.ingesoft.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ar.edu.itba.ingesoft.Authentication.Authenticator;
import ar.edu.itba.ingesoft.Classes.User;
import ar.edu.itba.ingesoft.MainActivity;
import ar.edu.itba.ingesoft.recyclerviews.Adapters.ProfileDataAdapter;
import ar.edu.itba.ingesoft.R;
import ar.edu.itba.ingesoft.ui.login.LoginActivity;

public class ProfileFragment extends Fragment {

    private ProgressBar theProgressBar;
    private TextView loadingTextView;

    private ProfileViewModel profileViewModel;

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView universityTextView;
    private TextView coursesTextView;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile_appbar_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_profile_logout:
                new Authenticator().signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                break;
        }

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(getActivity()).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        //Name TextView
        nameTextView = root.findViewById(R.id.profileNameTextView);

        //Email
        LinearLayout ll = root.findViewById(R.id.profileEmail);
        emailTextView = ll.findViewById(R.id.itemProfileDataDescription);
        ((TextView)ll.findViewById(R.id.itemProfileDataTitle)).setText(R.string.email);
        //University
        ll = root.findViewById(R.id.profileUniversity);
        universityTextView = ll.findViewById(R.id.itemProfileDataDescription);
        ((TextView)ll.findViewById(R.id.itemProfileDataTitle)).setText(R.string.university);
        //Courses
        ll = root.findViewById(R.id.profileCourses);
        coursesTextView = ll.findViewById(R.id.itemProfileDataDescription);
        ((TextView)ll.findViewById(R.id.itemProfileDataTitle)).setText(R.string.courses_taught);


        //ProgressBar + Loading TextView
        theProgressBar = root.findViewById(R.id.profileProgressBar);
        loadingTextView = root.findViewById(R.id.loadingTextView);

        profileViewModel.getCurrentUserLiveData().observe(getActivity(), new Observer<User>(){
            @Override
            public void onChanged(User u) {
                nameTextView.setText(u.getName());
                emailTextView.setText(u.getMail());
                //todo
                universityTextView.setText("No University");
                coursesTextView.setText("");
            }
        });

        profileViewModel.getLoading().observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if(loading){
                    (root.findViewById(R.id.profileLinearLayout)).setVisibility(View.GONE);
                    theProgressBar.setVisibility(View.VISIBLE);
                    loadingTextView.setVisibility(View.VISIBLE);
                }
                else{
                    (root.findViewById(R.id.profileLinearLayout)).setVisibility(View.VISIBLE);
                    theProgressBar.setVisibility(View.GONE);
                    loadingTextView.setVisibility(View.GONE);
                }
            }
        });

        return root;
    }
}