package com.example.loknath.locationtracker.adaper;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.loknath.locationtracker.R;
import com.example.loknath.locationtracker.dto.UserDto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserAdaper extends RecyclerView.Adapter<UserAdaper.ViewHolder> implements Filterable {

    private ArrayList<UserDto> myArrayList;
    private List<UserDto> contactListFiltered;
    private OnclickListener onclickListener;

    public UserAdaper(ArrayList<UserDto> myArrayList) {
        this.myArrayList=myArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      View view=LayoutInflater.from(viewGroup.getContext())
              .inflate(R.layout.row_user, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.tv_user.setText(myArrayList.get(i).Name);
        viewHolder.tv_status.setText(""+myArrayList.get(i).status);
        if(myArrayList.get(i).status){
            viewHolder.btn_request.setVisibility(View.VISIBLE);
        }else {
            viewHolder.btn_request.setVisibility(View.GONE);
        }

        viewHolder.btn_request.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        onclickListener.onItemClick(i);

    }

    //to sotre request table
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
   // FirebaseDatabase.getInstance().getReference().child("User").child(userId).child("Name").setValue(email);
   // DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("Request").child(userId).child("sender").setValue(userId);
});
    }

    @Override
    public int getItemCount() {
        return myArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    contactListFiltered = myArrayList;
                } else {
                    ArrayList<UserDto> filteredList = new ArrayList<>();
                    for (UserDto row : myArrayList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.Name.toLowerCase().contains(charString.toLowerCase()) || row.key.contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_user;
        Button btn_request;
        TextView tv_status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_user= itemView.findViewById(R.id.tv_user);
            tv_status = itemView.findViewById(R.id.tv_uStatus);
            btn_request= itemView.findViewById(R.id.btn_request);
        }
    }
    public void setOnclickListener(OnclickListener onclickListener)
    {
        this.onclickListener=onclickListener;
    }
    public interface OnclickListener{
         void onItemClick(int posistion);
    }
}
