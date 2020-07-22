package com.here.hellomap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteAdapter extends ArrayAdapter<UserModal> {
    private final Context mContext;
    private final List<UserModal> mUserInfo;
    private final List<UserModal> mUserInfoAll;
    private final int mLayoutResourceId;

    public AutoCompleteAdapter(Context context, int resource, List<UserModal> userInfo) {
        super(context, resource, userInfo);
        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mUserInfo = new ArrayList<>(userInfo);
        this.mUserInfoAll = new ArrayList<>(userInfo);
    }

    public int getCount() {
        return mUserInfo.size();
    }

    public UserModal getItem(int position) {
        return mUserInfo.get(position);
    }

    public long getItemId(int position) {
        return mUserInfo.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(mLayoutResourceId, parent, false);
            }
            UserModal department = getItem(position);
            TextView name =convertView.findViewById(R.id.txt_autocomplete);
            name.setText(department.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                return ((UserModal) resultValue).getName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<UserModal> departmentsSuggestion = new ArrayList<>();
                if (constraint != null) {
                    for (UserModal department : mUserInfoAll) {
                        if (department.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            departmentsSuggestion.add(department);
                        }
                    }
                    filterResults.values = departmentsSuggestion;
                    filterResults.count = departmentsSuggestion.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mUserInfo.clear();
                if (results != null && results.count > 0) {
                    // avoids unchecked cast warning when using mDepartments.addAll((ArrayList<Department>) results.values);
                    for (Object object : (List<?>) results.values) {
                        if (object instanceof UserModal) {
                            mUserInfo.add((UserModal) object);
                        }
                    }
                    notifyDataSetChanged();
                } else if (constraint == null) {
                    // no filter, add entire original list back in
                    mUserInfo.addAll(mUserInfoAll);
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}