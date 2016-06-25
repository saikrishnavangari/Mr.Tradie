package au.com.appscore.mrtradie;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by adityathakar on 19/08/15.
 */
public class ContentFragment extends Fragment {


    ImageButton imageButtonMoreServices,imageButtonElectrician,imageButtonCarpenter,imageButtonHandyman,imageButtonPlumber,imageButtonPainter,imageButtonGardener,imageButtonBuilder,imageButtonConcreter,imageButtonCabinetMaker;
    double latitude ;
    double longitude ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_fragment, container, false);

        Bundle bundle = this.getArguments();
        latitude = bundle.getDouble("latitude",0);
        longitude = bundle.getDouble("longitude",0);

        imageButtonMoreServices = (ImageButton) v.findViewById(R.id.imageButtonMoreServices);
        imageButtonElectrician = (ImageButton) v.findViewById(R.id.imageButtonElectrician);
        imageButtonCarpenter = (ImageButton) v.findViewById(R.id.imageButtonCarpenter);
        imageButtonHandyman = (ImageButton) v.findViewById(R.id.imageButtonHandyman);
        imageButtonPlumber = (ImageButton) v.findViewById(R.id.imageButtonPlumber);
        imageButtonPainter = (ImageButton) v.findViewById(R.id.imageButtonPainter);
        imageButtonGardener = (ImageButton) v.findViewById(R.id.imageButtonGardener);
        imageButtonBuilder = (ImageButton) v.findViewById(R.id.imageButtonBuilder);
        imageButtonConcreter = (ImageButton) v.findViewById(R.id.imageButtonConcreter);
        imageButtonCabinetMaker = (ImageButton) v.findViewById(R.id.imageButtonCabinatMaker);


        imageButtonMoreServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MoreServices.class);
                startActivity(intent);
            }
        });
        imageButtonElectrician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String) v.getTag());
            }
        });
        imageButtonCarpenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String)v.getTag());
            }
        });
        imageButtonHandyman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String)v.getTag());
            }
        });
        imageButtonPlumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String)v.getTag());
            }
        });
        imageButtonPainter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String)v.getTag());
            }
        });
        imageButtonGardener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String)v.getTag());
            }
        });
        imageButtonBuilder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String)v.getTag());
            }
        });
        imageButtonConcreter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String)v.getTag());
            }
        });
        imageButtonCabinetMaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBusinessListScreen((String)v.getTag());
            }
        });

        return v;
    }

    private void openBusinessListScreen(String tag) {
        Intent intent = new Intent(getActivity(),BusinessList.class);
        intent.putExtra("occupation_button_clicked",true);
        intent.putExtra("category",Integer.parseInt(tag));
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        startActivity(intent);
    }
}
