package com.example.nasacapstonecst2355;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HelpFragment extends Fragment {

    //empty constructor
    public HelpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflating layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        TextView helpText = view.findViewById(R.id.help_text);
        helpText.setText(R.string.help_text);

        //close button
        View closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(this::closeFragment);

        return view;
    }

    public void closeFragment(View view) {
        //close the fragment
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }
}
