package com.xplore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static com.xplore.CreateGroupFragment.invitedMembers;

/**
 * Created by Nikaoto on 3/4/2017.
 * TODO write description of this class - what it does and why.
 */

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.MemberViewHolder> {

    private ArrayList<User> users = new ArrayList<>();
    private Context context;
    private User currentMember;

    public MemberListAdapter(Context context, ArrayList<User> users){
        this.context = context;
        this.users = users;
    }

    class MemberViewHolder extends RecyclerView.ViewHolder{
        TextView rep_txt;
        ImageView memberImage;

        MemberViewHolder(View itemView) {
            super(itemView);
            rep_txt = (TextView) itemView.findViewById((R.id.member_rep_text));
            memberImage = (ImageView) itemView.findViewById(R.id.member_profile_image);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_list_item, parent, false);
        return new MemberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, final int position) {
        currentMember = users.get(position);

        //Loading Member Reputation
        holder.rep_txt.setText(String.valueOf(currentMember.getReputation()));

        //Loading Member Image
        Picasso.with(context)
                .load(currentMember.getProfile_picture_url())
                .transform(new RoundedCornersTransformation(
                        context.getResources().getInteger(R.integer.pic_small_angle),
                        context.getResources().getInteger(R.integer.pic_small_margin)))
                .into(holder.memberImage);

        //Configuring Clicks
        holder.memberImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentMember = users.get(position); //DO NOT REMOVE, THIS IS NECESSARY
                General.openUserProfile((Activity) context, currentMember.getId());
            }
        });

        //Pops up "remove?" dialog
        holder.memberImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                currentMember = users.get(position);
                General.vibrateDevice(context, null);

                //TODO string resources
                new AlertDialog.Builder(context)
                        .setTitle("Remove "+currentMember.getFname()+" "+currentMember.getLname())
                        .setMessage("Do you wish to remove this member from your group?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Removing user and updating recycler view
                                invitedMembers.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, invitedMembers.size());
                                Toast.makeText(context, R.string.member_removed, Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();
                return false;
            }
        });
    }
}
