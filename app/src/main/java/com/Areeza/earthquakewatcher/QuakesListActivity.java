package com.Areeza.earthquakewatcher;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import model.EarthQuake;
import util.Constants;

public class QuakesListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> arrayList;
    private RequestQueue requestQueue;
    private ArrayAdapter arrayAdapter;
    private List<EarthQuake> quakeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quakes_list);

        listView = (ListView) findViewById(R.id.listView);
        arrayList = new ArrayList<>();
        quakeList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        getAllQuakes(Constants.URL);
    }

    private void getAllQuakes(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = response.getJSONArray("features");
                            for(int i = 0; i< Constants.LIMIT; i++){
                                //get properties object
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                                arrayList.add(properties.getString("place"));
                            }

                            arrayAdapter = new ArrayAdapter(QuakesListActivity.this, android.R.layout.simple_list_item_1, arrayList);
                            listView.setAdapter(arrayAdapter);
                            arrayAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
