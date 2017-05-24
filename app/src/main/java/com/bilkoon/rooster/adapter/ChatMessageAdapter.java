package com.bilkoon.rooster.adapter;

import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/*import com.blikoon.rooster.R;
import com.blikoon.rooster.model.ChatMessage;*/
import com.ahmed.chatapplication.R;
import com.bilkoon.rooster.model.ChatMessage;
import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.himanshusoni.chatmessageview.ChatMessageView;

/**
 * Created by himanshusoni on 06/09/15.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageHolder> {
    private final String TAG = "ChatMessageAdapter";
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1, SECTION = 2;

    private List<ChatMessage> mMessages;
    private Context mContext;
    public boolean isAddingStoredMessages = true;

    public ChatMessageAdapter(Context context, List<ChatMessage> data) {
        mContext = context;
        mMessages = data;
    }

    @Override
    public int getItemCount() {
        return mMessages == null ? 0 : mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage item = mMessages.get(position);
        if(item.isSection()) return SECTION;
        else if (item.isMine()) return MY_MESSAGE;
        else return OTHER_MESSAGE;
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == SECTION){
            return new MessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_section, parent, false), false, true);
        }else if (viewType == MY_MESSAGE) {
            return new MessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_mine_message, parent, false), true, false);
        } else {
            return new MessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_other_message, parent, false), false, false);
        }
    }

    public void add(ChatMessage message) {
        mMessages.add(message);
        notifyItemInserted(mMessages.size() - 1);
    }

    public void add(int index, ChatMessage message) {
        mMessages.add(index, message);
        notifyItemInserted(index);
    }

    public void update(int id, ChatMessage message){
        mMessages.set(id, message);
        notifyItemChanged(id);
    }

    public void remove(int id) {
        mMessages.remove(id);
        notifyItemRemoved(id);
    }

    public void updateMessageStatus(String stanzaId, String status){
        int id = findMessageByStanzaId(stanzaId);
        if(id!=-1){
            ChatMessage chatMessage = mMessages.get(id);
            chatMessage.setMessageStatus(status);
            mMessages.set(id, chatMessage);
            notifyItemChanged(id);
        }
    }

    public int findMessageByStanzaId(String stanzaId){
        for(int i=0;i<mMessages.size();i++){
            if(stanzaId.equals(mMessages.get(i).getStanzaId())) return i;
        }
        return -1;
    }
    @Override
    public void onBindViewHolder(final MessageHolder holder, final int position) {
        ChatMessage chatMessage = mMessages.get(position);
        if(!chatMessage.isSection()) {
            if (chatMessage.isImage()) {
                holder.ivImage.setVisibility(View.VISIBLE);
                if (!isAddingStoredMessages) {
                    if (chatMessage.getContent() == null || chatMessage.getContent().equals("")) {
                        holder.ivImage.setImageResource(R.drawable.img_sample);
                        holder.imageLoading.setVisibility(View.VISIBLE);
                        holder.tvMessage.setVisibility(View.GONE);
                    } else {
                        Glide.with(mContext)
                                .load(new File(chatMessage.getContent()))
                                .into(holder.ivImage);
                        holder.tvMessage.setVisibility(View.GONE);
                    }
                }
            } else {
                holder.ivImage.setVisibility(View.GONE);
                //Glide.clear(holder.ivImage);
                holder.tvMessage.setVisibility(View.VISIBLE);
                if (holder.imageLoading != null) holder.imageLoading.setVisibility(View.GONE);
                holder.tvMessage.setText(chatMessage.getContent());
            }

            if (chatMessage.isMine()) {
                String status = chatMessage.getMessageStatus();
                if (status.equals("unsent")) {
                    holder.messageUnsent.setVisibility(View.VISIBLE);
                    holder.messageSent.setVisibility(View.GONE);
                    holder.messageDelivered.setVisibility(View.GONE);
                    holder.messageRead.setVisibility(View.GONE);
                } else if (status.equals("sent")) {
                    holder.messageSent.setVisibility(View.VISIBLE);
                    holder.messageUnsent.setVisibility(View.GONE);
                    holder.messageDelivered.setVisibility(View.GONE);
                    holder.messageRead.setVisibility(View.GONE);
                } else if (status.equals("delivered")) {
                    holder.messageDelivered.setVisibility(View.VISIBLE);
                    holder.messageUnsent.setVisibility(View.GONE);
                    holder.messageSent.setVisibility(View.GONE);
                    holder.messageRead.setVisibility(View.GONE);
                } else if (status.equals("read")) {
                    holder.messageRead.setVisibility(View.VISIBLE);
                    holder.messageUnsent.setVisibility(View.GONE);
                    holder.messageSent.setVisibility(View.GONE);
                    holder.messageDelivered.setVisibility(View.GONE);
                }
            }
            String[] time_arr = chatMessage.getTime().split(" ");
            holder.tvTime.setText(time_arr[1] + " " + time_arr[2]);
            holder.chatMessageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }else{
            holder.sectionTxt.setText(chatMessage.getTime());
        }
    }

    class MessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, sectionTxt;
        ImageView ivImage, messageUnsent, messageSent, messageDelivered, messageRead;
        ChatMessageView chatMessageView;
        ProgressBar imageLoading;

        MessageHolder(View itemView, boolean isMine, boolean isSection) {
            super(itemView);
            if(isSection){
                sectionTxt = (TextView) itemView.findViewById(R.id.sectionTxt);
                return;
            }
            chatMessageView = (ChatMessageView) itemView.findViewById(R.id.chatMessageView);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
            if(isMine) {
                messageUnsent = (ImageView) itemView.findViewById(R.id.messageUnsent);
                messageSent = (ImageView) itemView.findViewById(R.id.messageSent);
                messageDelivered = (ImageView) itemView.findViewById(R.id.messageDelivered);
                messageRead = (ImageView) itemView.findViewById(R.id.messageRead);
            }
            imageLoading = (ProgressBar) itemView.findViewById(R.id.imageLoading);
        }
    }
}