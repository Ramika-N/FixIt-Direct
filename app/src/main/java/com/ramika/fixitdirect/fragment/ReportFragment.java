package com.ramika.fixitdirect.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.ramika.fixitdirect.R;

public class ReportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etReportIssue = view.findViewById(R.id.etReportIssue);
        MaterialButton btnSubmitReport = view.findViewById(R.id.btnSubmitReport);

        btnSubmitReport.setOnClickListener(v -> {
            String issue = etReportIssue.getText().toString().trim();
            if (issue.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter the problem!", Toast.LENGTH_SHORT).show();
            } else {
                //database to store the report
                Toast.makeText(requireContext(), "Report submitted successfully!", Toast.LENGTH_LONG).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

}
