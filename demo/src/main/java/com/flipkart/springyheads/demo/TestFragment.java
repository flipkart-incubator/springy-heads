package com.flipkart.springyheads.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flipkart.springyheads.demo.R;

public class TestFragment extends Fragment {

    public static TestFragment newInstance(int identifier) {
        TestFragment testFragment = new TestFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("id", identifier);
        testFragment.setArguments(bundle);
        return testFragment;
    }

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_test, container, false);
        TextView identifier = (TextView) inflate.findViewById(R.id.identifier);
        identifier.setText(String.valueOf(getArguments().getInt("id")));
        return inflate;
    }


}
