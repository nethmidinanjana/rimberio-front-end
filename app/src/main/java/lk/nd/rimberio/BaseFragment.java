package lk.nd.rimberio;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import interfaces.BackButtonHandler;

public class BaseFragment extends Fragment implements BackButtonHandler {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void setupBackBtn(View view) {
        ImageView backBtn = view.findViewById(R.id.imageViewBackBtn);
        if(backBtn != null){
            backBtn.setOnClickListener(
                    v ->{
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        if (fragmentManager.getBackStackEntryCount() > 0){
                            fragmentManager.popBackStack();
                        }else{
                            requireActivity().onBackPressed();
                        }
                    }
            );
        }
    }
}
