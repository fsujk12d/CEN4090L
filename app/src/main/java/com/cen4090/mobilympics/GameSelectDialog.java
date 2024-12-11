package com.cen4090.mobilympics;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class GameSelectDialog extends DialogFragment {
    private RadioGroup gameSelectionGroup;
    private OnGameSelectedListener listener;
    private String selectedGame = null;

    public interface OnGameSelectedListener {
        void onGameSelected(String selectedGame);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnGameSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnGameSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_game_select, container, false);

        gameSelectionGroup = view.findViewById(R.id.game_selection_group);

        gameSelectionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedGame = getSelectedGame(checkedId);
            listener.onGameSelected(selectedGame);
            dismiss();
        });

        return view;
    }

    private String getSelectedGame(int radioButtonId) {
        if (radioButtonId == R.id.radio_tictactoe) return "TicTacToe";
        if (radioButtonId == R.id.radio_mancala) return "Mancala";
        if (radioButtonId == R.id.radio_checkers) return "Checkers";
        if (radioButtonId == R.id.radio_chess) return "Chess";
        if (radioButtonId == R.id.radio_dotsandboxes) return "DotsAndBoxes";
        return null;
    }
}
