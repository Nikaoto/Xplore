package com.xplore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by Nikaoto on 7/5/2017.
 * Temporary class until REMOVE popup button is added to creategroup activity
 * TODO write description of this class - what it does and why.
 */


class InvitingMemberListAdapter extends RecyclerView.Adapter<InvitingMemberListAdapter.MemberViewHolder> {

    private ArrayList<User> users = new ArrayList<>();
    private Context context;
    private RelativeLayout memberLayout;

    private int selectedMemberPos;
    private User currentMember;
    private TextView member_fname_text, member_lname_text, member_age_text, member_tel_text;

    InvitingMemberListAdapter(Context context, ArrayList<User> users, RelativeLayout memberLayout){
        this.context = context;
        this.users = users;
        this.memberLayout = memberLayout;

        selectedMemberPos = -1;
        member_fname_text = (TextView) memberLayout.findViewById(R.id.member_fname_text);
        member_lname_text = (TextView) memberLayout.findViewById(R.id.member_lname_text);
        member_age_text = (TextView) memberLayout.findViewById(R.id.member_age_text);
        member_tel_text = (TextView) memberLayout.findViewById(R.id.member_tel_text);
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

    void SetSelectedMemberPos(int selectedMemberPos)
    {
        this.selectedMemberPos = selectedMemberPos;
    }

    int GetSelectedMemberPos()
    {
        return this.selectedMemberPos;
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
                currentMember = users.get(position);
                if(selectedMemberPos == position) {//same member click
                    memberLayout.setVisibility(View.GONE);
                    selectedMemberPos = -1;
                    //divider.animate().translationY(-dividerMoveY);
                }
                else {//other member click

                    if(selectedMemberPos == -1) {//first click
                        memberLayout.setVisibility(View.VISIBLE);
                        //divider.animate().translationY(dividerMoveY);
                    }

                    selectedMemberPos = position;
                    member_fname_text.setText(currentMember.getFname());
                    member_lname_text.setText(currentMember.getLname());
                    member_age_text.setText(context.getString(R.string.age) + ": " +
                            General.calculateAge(TimeManager.Companion.getGlobalTimeStamp(), currentMember.getBirth_date()));
                    member_tel_text.setText(context.getString(R.string.tel) + ": " + currentMember.getTel_num());
                }
            }
        });
    }
}