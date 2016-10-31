package sprinwood.findmedicine;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mvOnMapAct;
    List<String[]> namesAndCosts;
    String idPharmInCycle = "kek";
    FirebaseDatabase database;
    String idDrug;
    String name;
    String vendor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        database = FirebaseDatabase.getInstance();
        idDrug = intent.getStringExtra("idDrug");
        vendor = intent.getStringExtra("vendor");
        name = intent.getStringExtra("name");
        createMapView();
        DatabaseReference myFirebaseRef = database.getReference("pharma_drug_cost");
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                namesAndCosts = new ArrayList<String[]>();
                List<String> tmp = new ArrayList<String>();
                idPharmInCycle = "kek";
                String idStr = String.valueOf(idDrug);
                for(DataSnapshot dsp : snapshot.getChildren()){
                    Log.d("MYTAG",String.valueOf(dsp.child("id_drug").getValue()));
                    if(String.valueOf(dsp.child("id_drug").getValue()).equals(idStr)) {
                        String namePharm = String.valueOf(dsp.child("id_pharma").getValue());
                        if (namePharm.equals("0")) {
                            idPharmInCycle = "МАЯК НА ПОЛЕЖАЕВСКОЙ";
                        } else if (namePharm.equals("1")) {
                            idPharmInCycle = "ФЛОРИЯ ЭКОНОМ ПОЛЕЖАЕВСКАЯ";
                        } else if (namePharm.equals("2")) {
                            idPharmInCycle = "ИРИСТ 2000 НА МАРШАЛА ЖУКОВА";
                        } else if (namePharm.equals("3")) {
                            idPharmInCycle = "АПТЕКИ СТОЛИЧКИ ВОРОНЦОВСКАЯ";
                        } else if (namePharm.equals("4")) {
                            idPharmInCycle = "АПТЕКА ЛФ";
                        }
                        tmp.add(String.valueOf(dsp.child("cost").getValue()));
                        tmp.add(idPharmInCycle);
                        tmp.add(String.valueOf(dsp.child("id_pharma").getValue()));
                        namesAndCosts.add(new String[]{tmp.get(1), tmp.get(0), tmp.get(2)});
                        tmp.clear();
                    }
                }
                for(final String[] aStr : namesAndCosts) {
                    DatabaseReference newRef = database.getReference("pharmacy_addr/" + aStr[2]);
                    newRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            double lat=0;
                            double lng=0;

                            for(DataSnapshot dsp : dataSnapshot.getChildren() ){
                                if(String.valueOf(dsp.getKey()).equals("latitude")){
                                    lat = Double.valueOf(String.valueOf(dsp.getValue()));
                                } else if(String.valueOf(dsp.getKey()).equals("longtitude")){
                                    lng = Double.valueOf(String.valueOf(dsp.getValue()));
                                }
                            }
                            addMarker(lat,lng,String.valueOf(namesAndCosts.indexOf(aStr)));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("The read failed: " ,databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
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

    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent(getApplicationContext(), Information.class);
        intent.putExtra("name", name);
        intent.putExtra("vendor", vendor);
        intent.putExtra("id", idDrug);
        startActivity(intent);
        return true;

    }
}
