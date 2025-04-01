package lk.nd.rimberio;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExploreFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        SupportMapFragment supportMapFragment = new SupportMapFragment();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.exploreFrameLayout, supportMapFragment);
        fragmentTransaction.commit();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Log.i("ExploreLog", "Map is ready!");

                progressBar.setVisibility(View.GONE);

                LatLng[] locations = {
                        new LatLng(6.925858541388138, 79.86142846560236),  // Colombo
                        new LatLng(7.083080037322264, 80.01039450225727),  // Gampaha
                        new LatLng(6.830147485402176, 80.9885313876665),  // Bandarawela
                        new LatLng(6.952786554559401, 80.7858845826782),  // Nuwara Eliya
                        new LatLng(9.67251127209438, 80.01106336428845),  // Jaffna
                        new LatLng(7.290322796000962, 80.63041020465582),  // Kandy
                        new LatLng(8.325693454848489, 80.3677975359224),  // Anuradhapura
                        new LatLng(6.062789972398656, 80.21167325651317),  // Galle
                        new LatLng(7.12788634051773, 79.8842343953814),  // Seeduwa
                        new LatLng(9.000153895905685, 79.86946826021493),  // Mannar
                        new LatLng(6.978569208634186, 81.06029802925399),  // Badulla
                        new LatLng(5.960378900145285, 80.56111703069206),  // Matara
                        new LatLng(6.705901165961512, 80.38463211367367),  // Ratnapura
                        new LatLng(8.5875528030315, 81.21469369721461)   // Trincomalee
                };

                String[] locationTitles = {
                        "Colombo",
                        "Gampaha",
                        "Bandarawela",
                        "Nuwara Eliya",
                        "Jaffna",
                        "Kandy",
                        "Anuradhapura",
                        "Galle",
                        "Seeduwa",
                        "Mannar",
                        "Badulla",
                        "Matara",
                        "Ratnapura",
                        "Trincomalee"
                };


                for(int i = 0; i < locations.length; i++){

                    LatLng latLng = locations[i];
                    String title = locationTitles[i];

                    googleMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .title("Rimberio "+ title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                    );
                }

                LatLng sriLankaCenter = new LatLng(7.8731, 80.7718);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaCenter, 8));

                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
                uiSettings.setCompassEnabled(true);

            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {
            Window window = getActivity().getWindow();

            window.setStatusBarColor(getResources().getColor(R.color.sky_blue));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                WindowInsetsController insetsController = window.getInsetsController();
                if (insetsController != null) {
                    insetsController.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.white));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController insetsController = window.getInsetsController();
                if (insetsController != null) {
                    // Reset status bar icons to default
                    insetsController.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        }
    }
}