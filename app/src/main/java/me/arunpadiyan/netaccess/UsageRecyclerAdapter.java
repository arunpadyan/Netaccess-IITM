package me.arunpadiyan.netaccess;

/**
 * Created by arunp on 27-Sep-15.
 */

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.annotation.SuppressLint;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UsageRecyclerAdapter extends RecyclerView.Adapter<UsageRecyclerAdapter.CustomViewHolder> {
    private ArrayList<Usage> feedItemList;
    private Context mContext;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Button but;

    public UsageRecyclerAdapter(Context context, ArrayList<Usage> feedItemList, SwipeRefreshLayout mSwipeRefreshLayout) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
    }

    @Override
    public UsageRecyclerAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.usage_item, viewGroup, false);
        CustomViewHolder pvh = new CustomViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final UsageRecyclerAdapter.CustomViewHolder customViewHolder, final int i) {
        customViewHolder.ip.setText(feedItemList.get(i).getIp());
        customViewHolder.usage.setText(feedItemList.get(i).getAmount());
        customViewHolder.revoke.setEnabled(feedItemList.get(i).getActive());
        customViewHolder.revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Revoke(feedItemList.get(i).getLink());
                customViewHolder.revoke.setEnabled(false);
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        but=customViewHolder.revoke;


    }

    @Override
    public int getItemCount() {
        return feedItemList.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected Button revoke;
        protected TextView ip, usage;

        public CustomViewHolder(View view) {
            super(view);
            this.revoke = (Button) view.findViewById(R.id.revoke);
            this.ip = (TextView) view.findViewById(R.id.ip);
            this.usage = (TextView) view.findViewById(R.id.usage);
        }
    }

    private void Revoke(String link) {

        String requestURL1, requestURL2;
        requestURL1 = "https://netaccess.iitm.ac.in/account/login";
        requestURL2 = link;
        final RequestQueue queue = Volley.newRequestQueue(mContext);
        final StringRequest stringRequest2 = new StringRequest(Request.Method.GET, requestURL2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       // Log.d("respons", response);
                        mSwipeRefreshLayout.setRefreshing(false);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
              //  Log.d("server error", error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                but.setEnabled(true);

            }
        });
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestURL1,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log.d("respons",response);
                        queue.add(stringRequest2);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Log.d("server error", error.toString());
                but.setEnabled(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userPassword", MainActivity.getprefString("ldap", MyApplication.getContext()));
                params.put("userLogin", MainActivity.getprefString("rollno", MyApplication.getContext()));
                return params;
            }
        };

        queue.add(stringRequest);


    }

}