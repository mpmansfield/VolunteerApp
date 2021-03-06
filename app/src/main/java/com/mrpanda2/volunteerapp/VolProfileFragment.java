package com.mrpanda2.volunteerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VolProfileFragment extends Fragment {

    private FirebaseUser mUser;
    private TextView mVolName;
    private Button mShowEventButton;
    private Button editProfile;
    private Button signOut;
    private Button viewButton;
    private Button mMapButton;
    private Button mPreviousSessions;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.volunteer_sign_in, container, false);
        viewButton = v.findViewById(R.id.view_edit_vol_data);
        viewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(VolProfileFragment.this.getActivity(), ViewVolunteerAttendanceActivity.class);
                startActivity(intent);
            }
        });
        mVolName = v.findViewById(R.id.volunteer_name);
        mVolName.setText(mUser.getDisplayName());
        editProfile = v.findViewById(R.id.vol_edit_profile);
        editProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(VolProfileFragment.this.getActivity(), EditProfileInfoActivity.class);
                startActivity(intent);
            }
        });

        signOut = v.findViewById(R.id.signOutButton);
        signOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(VolProfileFragment.this.getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        mShowEventButton = v.findViewById(R.id.vol_events_button);
        mShowEventButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(VolProfileFragment.this.getActivity(), ShowEventActivity.class);
                startActivity(intent);
            }
        });
        mMapButton = v.findViewById(R.id.map_button);
        mMapButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(VolProfileFragment.this.getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });


        mPreviousSessions = v.findViewById(R.id.previous_sessions);
        mPreviousSessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VolProfileFragment.this.getActivity(), PreviousVolSessions.class);
                startActivity(intent);
            }
        });

        return v;

    }


}
