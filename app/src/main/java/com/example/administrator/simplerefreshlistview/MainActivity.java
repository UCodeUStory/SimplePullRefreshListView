package com.example.administrator.simplerefreshlistview;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.custom.listview.OnRefreshListener;
import com.custom.listview.SimpleRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnRefreshListener{

    private SimpleRefreshListView mSimpleRefreshListView;
    private List<String> datas;
    private MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSimpleRefreshListView = (SimpleRefreshListView) findViewById(R.id.listView);
        datas = new ArrayList<String>();
        for (int i = 0; i < 15; i++) {
            datas.add("This is ListView item" + i);
        }
        adapter = new MyAdapter(this);
        mSimpleRefreshListView.setAdapter(adapter);
        mSimpleRefreshListView.setOnRefreshListener(this);
    }

    @Override
    public void onPullRefresh() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SystemClock.sleep(2000);
                datas.add("This is pull add item");
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                adapter.notifyDataSetChanged();
                mSimpleRefreshListView.hideHeaderView();
            }
        }.execute(new Void[] {});
    }

    @Override
    public void onUpRefresh() {
        datas.add("This is up add item");
    }

    private class MyAdapter extends BaseAdapter {
        private Context mContext;
        public MyAdapter(Context context){
            this.mContext = context;
        }
        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view = LayoutInflater.from(mContext).inflate(R.layout.listview_item, parent,
                    false);
            TextView textView = (TextView)view.findViewById(R.id.textView);
            textView.setText(datas.get(position));
            return view;
        }

    }
}
