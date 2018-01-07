package com.example.root.gni_transport.gni.ui.activities;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.root.gni_transport.R;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.root.gni_transport.gni.ui.adapters.Routeselect;
import com.example.root.gni_transport.gni.ui.models.SelectRoutemodel;
import com.example.root.gni_transport.gni.utils.Conection;
import com.example.root.gni_transport.gni.utils.Contants;
import com.example.root.gni_transport.gni.utils.Sharedpref;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build();
    @BindView(R.id.rv)
    RelativeLayout rv;
    @BindView(R.id.route_select_recycle)
    RecyclerView recyclerView;
    @BindView(R.id.r1)
    RelativeLayout nointernet;
    @BindView(R.id.searcherror)
    RelativeLayout searcherror;
    Routeselect routeselect;
    Conection conection;
    List<SelectRoutemodel> list = new ArrayList<>();
    LinearLayoutManager layoutManager;
    Contants contants = new Contants();
    LoadToast loadToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Sharedpref sharedpref = new Sharedpref(this);
        boolean checked = sharedpref.getFirstopen();
        AndroidNetworking.initialize(getApplicationContext());
        ButterKnife.bind(this);
        loadToast = new LoadToast(this);
        conection = new Conection(this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        if (conection.isInternet()) {
            if(sharedpref.getRouteselected()){
                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                finish();
            }
            loadToast.show();
            getAllroutedetails();
        } else {
            recyclerView.setVisibility(View.GONE);
            nointernet.setVisibility(View.VISIBLE);
            Snackbar.make(nointernet
                    , getString(R.string.nointernet), Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    private void getAllroutedetails() {
        String url = contants.Allroutes;
        Log.d("ALL_ROUTE_URL", url);
        AndroidNetworking.post(url)
                .addBodyParameter("Authkey", getString(R.string.Authkey))
                .setOkHttpClient(okHttpClient)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadToast.success();
                        Log.d("TAGGG", response.toString());
                        if (!response.has(getString(R.string.searcherrorselecting))) {
                            try {
                                JSONArray jsonArray = response.getJSONArray("Routes");
                                if (jsonArray.length() > 0) {
                                    list.clear();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        try {
                                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                                            SelectRoutemodel selectRoutemodel = new SelectRoutemodel();
                                            selectRoutemodel.setRouteNumber(jsonObject.getString("RouteNumber"));
                                            selectRoutemodel.setFcmrouteId(jsonObject.getString("FcmRouteId"));
                                            selectRoutemodel.setFullRoute(jsonObject.getString("Route"));
                                            selectRoutemodel.setStartoint(jsonObject.getString("StartPoint"));
                                            selectRoutemodel.setEndpoint(jsonObject.getString("EndPoint"));
                                            selectRoutemodel.setViapoint(jsonObject.getString("ViaPoint"));
                                            list.add(selectRoutemodel);
                                            // Log.d("TAGGMODEL",list.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();

                                        }
                                    }
                                    Log.d("TEST", list.toString());
                                    routeselect = new Routeselect(MainActivity.this, list);
                                    recyclerView.setAdapter(routeselect);
                                } else {
                                    showError();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();

                            }
                        } else if (response.has(getString(R.string.searcherrorselecting))) {
                            recyclerView.setVisibility(View.INVISIBLE);
                            searcherror.setVisibility(View.VISIBLE);

                        }


                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("LOADERROR", anError.toString());
                        loadToast.error();
                        showError();

                    }
                });

    }

    public void showError() {
        loadToast.error();
        Snackbar.make(rv, "tryagainlater", Snackbar.LENGTH_INDEFINITE).show();
    }
}

