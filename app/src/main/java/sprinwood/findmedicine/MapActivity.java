package sprinwood.findmedicine;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mvOnMapAct;
    List<String[]> namesAndCosts;
    String idPharmInCycle = "kek";
    Firebase myFirebaseRef;
    String idDrug;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        myFirebaseRef = new Firebase("https://pharmacy-cost-map.firebaseio.com");
        idDrug = intent.getStringExtra("idDrug");
        Log.d("TAG","idDrug = " + idDrug);
        createMapView();
        myFirebaseRef.child("drugs_pharmacy").child(idDrug).child("pharmacies_costs")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        namesAndCosts = new ArrayList<String[]>();
                        List<String> tmp = new ArrayList<String>();
                        idPharmInCycle = "kek";
                        for(DataSnapshot dsp : snapshot.getChildren()){
                            for(DataSnapshot dsp2 : dsp.getChildren()){
                                if(String.valueOf(dsp2.getKey()) == "name"){
                                    tmp.add(String.valueOf(dsp2.getValue()));
                                    String namePharm = String.valueOf(dsp2.getValue());

                                    if(namePharm.equals("МАЯК НА ПОЛЕЖАЕВСКОЙ")){
                                        idPharmInCycle = "0";
                                    } else if(namePharm.equals("ФЛОРИЯ ЭКОНОМ ПОЛЕЖАЕВСКАЯ")){
                                        idPharmInCycle = "1";
                                    }else if(namePharm.equals("ИРИСТ 2000 НА МАРШАЛА ЖУКОВА")){
                                        idPharmInCycle = "2";
                                    }else if(namePharm.equals("АПТЕКИ СТОЛИЧКИ ВОРОНЦОВСКАЯ")) {
                                        idPharmInCycle = "3";
                                    }else if(namePharm.equals("АПТЕКА ЛФ")) {
                                        idPharmInCycle = "4";
                                    }
                                    tmp.add(idPharmInCycle);
                                }
                                else if(String.valueOf(dsp2.getKey()) == "cost") {
                                    tmp.add(String.valueOf(dsp2.getValue()));
                                }
                            }
                            namesAndCosts.add(new String[]{tmp.get(1),tmp.get(0),tmp.get(2)});
                            tmp.clear();
                        }
                        for(final String[] aStr : namesAndCosts) {
                            myFirebaseRef.child("pharmacy_addr").child(aStr[2]).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    double lat=0;
                                    double lng=0;

                                    for(DataSnapshot dsp : dataSnapshot.getChildren() ){
                                        if(String.valueOf(dsp.getKey()) == "latitude"){
                                            lat = Double.valueOf(String.valueOf(dsp.getValue()));
                                        } else if(String.valueOf(dsp.getKey())=="longtitude"){
                                            lng = Double.valueOf(String.valueOf(dsp.getValue()));
                                        }
                                    }
                                    addMarker(lat,lng,String.valueOf(namesAndCosts.indexOf(aStr)));
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.e("The read failed: " ,firebaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e("The read failed: " ,firebaseError.getMessage());
                    }
                });
    }

    private void addMarker(double lat, double lng,String idPharm) {
        /** Make sure that the map has been initialised **/
        if (null != mvOnMapAct) {
            mvOnMapAct.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(idPharm)
                    .draggable(true)
            );
        }
    }

    private void createMapView(){
        try {
            if(null == mvOnMapAct){

                ((MapFragment) getFragmentManager().findFragmentById(R.id.mapViewOnMapAct)).getMapAsync(this);
                //mapFrag.getMapAsync(this);

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
            }
        }
        catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mvOnMapAct = googleMap;
            if (null == mvOnMapAct) {
                Toast.makeText(getApplicationContext(),
                        "Error creating map", Toast.LENGTH_SHORT).show();
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mvOnMapAct.setMyLocationEnabled(true);

            CameraUpdate center=
                    CameraUpdateFactory.newLatLng(new LatLng(55.772458, 37.526306999999));
            CameraUpdate zoom= CameraUpdateFactory.zoomTo((float) 11.6);
            mvOnMapAct.moveCamera(center);
            mvOnMapAct.animateCamera(zoom);
            mvOnMapAct.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);

                    TextView tvNameOnWindow = (TextView) v.findViewById(R.id.nameOnInfoWindow);
                    TextView tvTelOnWindow = (TextView) v.findViewById(R.id.telOnInfoWindow);
                    TextView tvDirOnWindow = (TextView) v.findViewById(R.id.directionOnInfoWindow);
                    TextView tvPriceOnWindow = (TextView) v.findViewById(R.id.priceOnInfoWindow);
                    String idPharm = marker.getTitle();

                    tvNameOnWindow.setText("Название: " + namesAndCosts.get(Integer.valueOf(idPharm))[0]);
                    tvPriceOnWindow.setText("Цена: " + namesAndCosts.get(Integer.valueOf(idPharm))[1]);
                    tvTelOnWindow.setText("Телефон: ");
                    tvDirOnWindow.setText("Расстояние: ");
                    return v;
                }
            });

        }
        catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }
}
