package com.example.destinity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ViewHolder> {

        private List<Users> data;
    private LayoutInflater inflater;
    private View.OnClickListener clickListener;

    Context context;
    String[] usernames;
    String[] alder;
    String[] bosted;
    String[] informasjon;
    String[] brukerbilde;


        public ProgramAdapter(Context context, List<Users> data){

            this.data =data;
            this.inflater = LayoutInflater.from(context);

        }

    @NonNull
    @Override
    public ProgramAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view= inflater.inflate(R.layout.singeuser, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramAdapter.ViewHolder holder, int position) {
        Users currentUserObj = data.get(position);
        holder.setData(currentUserObj);
        if(clickListener != null){
            holder.itemView.setOnClickListener(clickListener);

        }

    }
        @Override
        public int getItemCount () {
            return data.size();
        }

      /*  holder.brukerNavn.setText("Navn: "+usernames[position]);
        holder.brukerAlder.setText("Alder: " + alder[position]);
        holder.brukerBilde.setImageResource(brukerbilde[position]);
        holder.brukerBosted.setText("Bosted:  " +bosted[position]);
        holder.brukerDesc.setText("Litt om meg: " +informasjon[position]);
*/


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView brukerAlder;
        TextView brukerNavn;
        TextView brukerBosted;
        TextView brukerDesc;
        ImageView brukerBilde;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            brukerAlder = itemView.findViewById(R.id.recBrukerAlder);
            brukerNavn = itemView.findViewById(R.id.recBrukerNavn);
            brukerBosted = itemView.findViewById(R.id.recBrukerBosted);
            brukerDesc = itemView.findViewById(R.id.recBrukerInfo);
            brukerBilde = itemView.findViewById(R.id.brukerBilde);
        }

        public void setData(Users current) {
            this.brukerNavn.setText(current.getBrukerNavn());


        }

    }
}
