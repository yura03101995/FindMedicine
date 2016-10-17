package sprinwood.findmedicine;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.games.internal.api.StatsImpl;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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


public class Information extends AppCompatActivity implements OnMapReadyCallback {
    TextView tvName;
    TextView tvVendor;
    Button btnBack;
    GoogleMap mvOnInformation;
    List<String[]> namesAndCosts;
    ListView lvInformation;
    ScrollView mScrollView;
    String idDrug;
    String idPharmInCycle;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        database = FirebaseDatabase.getInstance();
        Intent intent = getIntent();
        tvName = (TextView) findViewById(R.id.tvName);
        tvVendor = (TextView) findViewById(R.id.tvVendor);
        btnBack = (Button) findViewById(R.id.btnBack);

        String name = intent.getStringExtra("name");
        String vendor = intent.getStringExtra("vendor");
        idDrug = intent.getStringExtra("id");
        Log.d("MYTAG","drugs_pharmacy/" + String.valueOf(idDrug) + "/pharmacies_costs");
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

                // находим список
                lvInformation = (ListView) findViewById(R.id.lvInformation);

                // создаем адаптер
                ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]> (getBaseContext(),
                        R.layout.my_item_list, R.id.tvNameOnMyItem, namesAndCosts){
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        String[] entry = namesAndCosts.get(position);
                        TextView tvName = (TextView) view.findViewById(R.id.tvNameOnMyItem);
                        TextView tvPrice = (TextView) view.findViewById(R.id.tvPriceOnMyItem);
                        TextView tvDir = (TextView) view.findViewById(R.id.tvDirectionOnMyItem);
                        tvName.setText(entry[0]);
                        tvPrice.setText("Цена: " + entry[1] + " р.");
                        tvDir.setText("Расстояние: ");
                        return view;
                    }
                };
                // присваиваем адаптер списку
                lvInformation.setAdapter(adapter);
                setListViewHeightBasedOnChildren(lvInformation);

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


        tvName.setText(name);
        tvVendor.setText(vendor);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(intent);
            }
        });
        createMapView();
    }

    private void addMarker(double lat, double lng,String idPharm) {
        /** Make sure that the map has been initialised **/
        if (null != mvOnInformation) {
            mvOnInformation.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(idPharm)
                    .draggable(true)
            );
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mvOnInformation = googleMap;
            if (null == mvOnInformation) {
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
            mvOnInformation.setMyLocationEnabled(true);
            mScrollView = (ScrollView) findViewById(R.id.svOnInformation); //parent scrollview in xml, give your scrollview id value

            ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapViewOnInformation))
                    .setListener(new WorkaroundMapFragment.OnTouchListener() {
                        @Override
                        public void onTouch() {
                            mScrollView.requestDisallowInterceptTouchEvent(true);
                        }
                    });
            CameraUpdate center=
                    CameraUpdateFactory.newLatLng(new LatLng(55.772458, 37.526306999999));
            CameraUpdate zoom= CameraUpdateFactory.zoomTo((float) 11.6);
            mvOnInformation.moveCamera(center);
            mvOnInformation.animateCamera(zoom);
            mvOnInformation.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
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

            mvOnInformation.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    Intent intent = new Intent(getBaseContext(),MapActivity.class);
                    intent.putExtra("idDrug",idDrug);
                    startActivity(intent);
                }
            });
        }
        catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ActionBar.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private void createMapView(){
        try {
            if(null == mvOnInformation){

                ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapViewOnInformation)).getMapAsync(this);
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
}
