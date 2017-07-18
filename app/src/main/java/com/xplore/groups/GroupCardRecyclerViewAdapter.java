package com.xplore.groups;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xplore.CircleTransformation;
import com.xplore.General;
import com.xplore.R;
import com.xplore.groups.search.GroupInfoActivity;

import java.util.ArrayList;

/**
 * Created by Nika on 7/17/2017.
 *
 * აღწერა:
 * ეს კლასი არის GroupCard კლასის RecyclerView-ს ადაპტერი. ვიყენებთ რომ ჯგუფების სია ვანახოთ
 * CardView-ებზე.
 *
 * Description:
 * This class is a RecyclerView adapter for GroupCard arraylists.
 * It's used to show a list of groups on CardViews.
 *
 */

public class GroupCardRecyclerViewAdapter extends RecyclerView.Adapter<GroupCardRecyclerViewAdapter.ResultsViewHolder> {
    private final ArrayList<GroupCard> groupCards;
    private final Activity activity;
    private final int imgSize;

    public GroupCardRecyclerViewAdapter(ArrayList<GroupCard> groupCards, Activity activity) {
        this.groupCards = groupCards;
        this.activity = activity;
        imgSize = Math.round(activity.getResources().getDimension(R.dimen.user_profile_image_tiny_size));
    }

    class ResultsViewHolder extends RecyclerView.ViewHolder {
        //TODO add ribbons and stuff
        ImageView groupImage;
        ImageView leaderImage;
        TextView leaderName;
        TextView leaderReputation;
        RelativeLayout leaderLayout;

        ResultsViewHolder(View itemView) {
            super(itemView);
            this.leaderImage = (ImageView) itemView.findViewById(R.id.leaderImageView);
            this.leaderName = (TextView) itemView.findViewById(R.id.leaderNameTextView);
            this.leaderReputation = (TextView) itemView.findViewById(R.id.leaderRepCombinedTextView);
            this.leaderLayout = (RelativeLayout) itemView.findViewById(R.id.leaderLayout);
            this.groupImage = (ImageView) itemView.findViewById(R.id.groupImageView);
        }
    }

    @Override
    public ResultsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ResultsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_card, parent, false));
    }

    @Override
    public void onBindViewHolder(ResultsViewHolder holder, final int position) {
        final GroupCard currentCard = groupCards.get(position);

        //Leader layout
        holder.leaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                General.openUserProfile(activity, currentCard.getLeaderId());
            }
        });

        //On card click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                General.HideKeyboard(activity);

                //Starting intent
                activity.startActivity(
                        GroupInfoActivity.getStartIntent(
                                activity,
                                currentCard.getId(),
                                Integer.valueOf(currentCard.getDestination_id())
                        )
                );
            }
        });

        //Leader name
        holder.leaderName.setText(currentCard.getLeaderName());

        //Leader reputation
        holder.leaderReputation.setText(currentCard.getLeaderReputation() + " " + activity.getResources().getString(R.string.reputation));

        //Leader image
        Picasso.with(activity)
                .load(currentCard.getLeaderImageUrl())
                .transform(new CircleTransformation(imgSize, imgSize))
                .into(holder.leaderImage);
        holder.leaderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                General.openUserProfile(activity, currentCard.getLeaderId());
            }
        });

        //Group image
        //TODO change this to just map or submitted image
        holder.groupImage.setImageResource(currentCard.getReserveImageId());

        //TODO add ribbons and other stuff
    }

    @Override
    public int getItemCount() {
        return groupCards.size();
    }
}