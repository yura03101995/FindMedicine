package sprinwood.findmedicine;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {


    ListView lvMain;
    final String LOG_TAG = "myLogs";
    List<String[]> namesAndVendors;
    Map<String,String> pharmacyCost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myFirebaseRef = database.getReference("drugs_pharmacy");
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                namesAndVendors = new ArrayList<String[]>();
                List<String> tmp = new ArrayList<String>();
                for(DataSnapshot dsp : snapshot.getChildren()){
                    for(DataSnapshot dsp2 : dsp.getChildren()){
                        if(String.valueOf(dsp2.getKey()).equals("name")){
                            tmp.add(String.valueOf(dsp2.getValue()));
                        }
                        else if(String.valueOf(dsp2.getKey()).equals("vendor_code")) {
                            tmp.add(String.valueOf(dsp2.getValue()));
                        }
                        else if(String.valueOf(dsp2.getKey()).equals("id")){
                            tmp.add(String.valueOf(dsp2.getValue()));
                        }
                    }
                    namesAndVendors.add(new String[]{tmp.get(1),tmp.get(2), tmp.get(0)});
                    tmp.clear();
                }

                // находим список
                lvMain = (ListView) findViewById(R.id.lvMain);

                // создаем адаптер
                ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]> (getBaseContext(),
                        android.R.layout.simple_list_item_2, android.R.id.text1, namesAndVendors){
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        String[] entry = namesAndVendors.get(position);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        text1.setText(entry[0]);
                        text2.setText(entry[1]);
                        return view;
                    }
                };
                // присваиваем адаптер списку
                lvMain.setAdapter(adapter);
                lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent intent = new Intent(getApplicationContext(), Information.class);
                        String[] selectedFromList = (String[]) lvMain.getItemAtPosition(position);
                        intent.putExtra("name", selectedFromList[0]);
                        intent.putExtra("vendor", selectedFromList[1]);
                        intent.putExtra("id", selectedFromList[2]);
                        startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databasError) {
                Log.e("The read failed: " ,databasError.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // User pressed the search button
        final List<String[]> findingNames = new ArrayList<String[]>();
        for(String[] s : namesAndVendors){
            if(s[0].toLowerCase().contains(query)){
                findingNames.add(s);
            }
        }
        lvMain = (ListView) findViewById(R.id.lvMain);
        ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]> (getBaseContext(),
                android.R.layout.simple_list_item_2, android.R.id.text1, findingNames){
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String[] entry = findingNames.get(position);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(entry[0]);
                text2.setText(entry[1]);
                return view;
            }
        };
        lvMain.setAdapter(adapter);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // User changed the text
        final List<String[]> findingNames = new ArrayList<String[]>();
        for(String[] s : namesAndVendors) {
            if (s[0].toLowerCase().contains(newText)) {
                findingNames.add(s);
            }
        }
        lvMain = (ListView) findViewById(R.id.lvMain);
        ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]> (getBaseContext(),
                android.R.layout.simple_list_item_2, android.R.id.text1, findingNames){
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String[] entry = findingNames.get(position);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(entry[0]);
                text2.setText(entry[1]);
                return view;
            }
        };
        lvMain.setAdapter(adapter);
        return false;
    }
}