package com.example.novamarket.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.novamarket.Activity.Home_Chat;
import com.example.novamarket.Activity.Home_Location2;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.orhanobut.logger.Logger;

import java.text.ParseException;
import java.util.ArrayList;

public class Chat_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<DTO_Chat> items = new ArrayList<>();
    int online = 0;
    Home_Chat homeChat;

    public void setHomeChat(Home_Chat homeChat){}

    public Chat_adapter(Context context) {
        this.context = context;
    }

    public void setOnline(int online) {
        for (DTO_Chat item : items) {
            item.setMsg_state(online + "");
            notifyItemRangeChanged(0, items.size());
        }
    }

    public void addItem(DTO_Chat item) {
        items.add(item);
        notifyItemInserted(items.size());
    }

    public void sendItem(DTO_Chat item) {
        items.add(0, item);
        notifyItemInserted(0);
    }

    public ArrayList<DTO_Chat> getItems() {
        return items;
    }

    public void setItems(ArrayList<DTO_Chat> list) {
        this.items = list;
        notifyItemRangeChanged(0, list.size());
    }

    public void itemsClear() {
        int size = getItemCount();
        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public int getItemViewType(int position) {
        DTO_Chat item = items.get(position);
        String type = item.getMsg_type();
        String writer = item.getUser_id();
        String user_id = PreferenceManager.getString(context, "user_id");

        /* 어떤 View 로 문자를 주고 받을지 설정 */
        switch (type) {
            case "msg":
                /* 내가 보낸건지 아닌지 확인 */
                if (writer.equals(user_id)) {
                    return 1;
                } else {

                    return 2;
                }
            case "img":
                /* 내가 보낸건지 아닌지 확인 */
                if (writer.equals(user_id)) {
                    return 3;
                } else {
                    return 4;
                }
            case "loc":
                if (writer.equals(user_id)) {
                    return 5;
                } else {

                    return 6;
                }
            default:
                Logger.e("Type error 확인 필요함");
                return 7;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        /* 결정된 ViewType 에 맞게 viewHolder 와 inflater setting */
        switch (viewType) {
            case 1:
                /*  메세지 오른쪽 */
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send, parent, false);
                return new Msg_Right_VH(view, this);
            case 2:
                /* 메세지 왼쪽 */
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receive, parent, false);
                return new Msg_Left_VH(view, this);
            case 3:
                /* 이미지 오른쪽 */
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_image_send, parent, false);
                return new img_Right_VH(view, this);
            case 4:
                /* 이미지 왼쪽 */
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_image_receive, parent, false);
                return new img_Left_VH(view, this);
            case 5:
                /* 위치 보내기 */
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_location_send, parent, false);
                return new Loc_Send_VH(view, this);
            case 6:
                /* 위치 받기 */
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_location_receive, parent, false);
                return new Loc_Receive_VH(view, this);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_send, parent, false);
                Logger.d("오류오류");
                return new Msg_Right_VH(view, this);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DTO_Chat item = items.get(position);
        String user_id = PreferenceManager.getString(context, "user_id"); // 클라이언트 user_id

        if (holder instanceof Msg_Right_VH) {
            /* 보낸 msg [ 메세지, 작성시간 ]*/
            Msg_Right_VH VH = (Msg_Right_VH) holder;
            VH.item_message.setText(item.getMsg());
            int state = Integer.parseInt(item.getMsg_state());
            if (state > 1) {
                VH.item_memeber.setVisibility(View.INVISIBLE); // 안읽음 표시 사라짐
            } else {
                VH.item_memeber.setVisibility(View.VISIBLE); // 안읽음 표시 있음
            }
            try {
                VH.item_date.setText(DateConverter.resultDateToString(item.getMsg_date(), "a h시 m분"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (holder instanceof Msg_Left_VH) {
            /* 받은 msg [ 보낸사람, 프로필사진, 내용, 작성시간 ]*/
            Msg_Left_VH VH = (Msg_Left_VH) holder;
            VH.item_name.setText(item.getMsg_name());
            VH.item_message.setText(item.getMsg());
            int state = Integer.parseInt(item.getMsg_state());
            if (state > 1) {
                VH.item_member.setVisibility(View.INVISIBLE);// 안읽음 표시 사라짐
            } else {
                VH.item_member.setVisibility(View.VISIBLE); // 안읽음 표시 있음
            }
            Glide.with(context).load(item.getMsg_profile()).circleCrop().into(VH.item_image);
            try {
                VH.item_date.setText(DateConverter.resultDateToString(item.getMsg_date(), "a h시 m분"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (holder instanceof img_Right_VH) {
            /* 보낸 img [사진, 작성시간] */
            img_Right_VH VH = (img_Right_VH) holder;
            int state = Integer.parseInt(item.getMsg_state());
            if (state > 1) {
                VH.item_member.setVisibility(View.INVISIBLE);
            } else {
                VH.item_member.setVisibility(View.VISIBLE);
            }
            /* 보낸 이미지 */
            Glide.with(context).load(item.getMsg_image()).centerCrop().into(VH.item_image);
            try {
                VH.item_date.setText(DateConverter.resultDateToString(item.getMsg_date(), "a h시 m분"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (holder instanceof img_Left_VH) {
            /* 받은 img [보낸사람, 프로필사진, 사진, 작성시간] */
            img_Left_VH VH = (img_Left_VH) holder;
            VH.item_name.setText(item.getMsg_name());
            int state = Integer.parseInt(item.getMsg_state());
            if (state > 1) {
                VH.item_member.setVisibility(View.INVISIBLE);
            } else {
                VH.item_member.setVisibility(View.VISIBLE);
            }
            /* 유저 프로필 */
            Glide.with(context).load(item.getMsg_profile()).circleCrop().into(VH.item_profile);
            /* 받은 이미지 */
            Glide.with(context).load(item.getMsg_image()).centerCrop().into(VH.item_image);
            try {
                VH.item_date.setText(DateConverter.resultDateToString(item.getMsg_date(), "a h시 m분"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (holder instanceof Loc_Send_VH) {
            /* 보낸 장소 이미지 뷰 */
            Loc_Send_VH VH = (Loc_Send_VH) holder;
            int state = Integer.parseInt(item.getMsg_state());
            if (state > 1) {
                VH.item_member.setVisibility(View.INVISIBLE);
            } else {
                VH.item_member.setVisibility(View.VISIBLE);
            }
            /* 받은 이미지 */
//            Glide.with(context).load(item.getMsg_image()).centerCrop().into(VH.item_image);
            try {
                VH.item_date.setText(DateConverter.resultDateToString(item.getMsg_date(), "a h시 m분"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (holder instanceof Loc_Receive_VH) {
            /* 받은 장소 이미지 뷰 */
            Loc_Receive_VH VH = (Loc_Receive_VH) holder;
            VH.item_name.setText(item.getMsg_name());
            int state = Integer.parseInt(item.getMsg_state());
            if (state > 1) {
                VH.item_member.setVisibility(View.INVISIBLE);
            } else {
                VH.item_member.setVisibility(View.VISIBLE);
            }
            /* 유저 프로필 */
            Glide.with(context).load(item.getMsg_profile()).circleCrop().into(VH.item_profile);
            /* 받은 이미지 */
//            Glide.with(context).load(item.getMsg_image()).centerCrop().into(VH.item_image);
            try {
                VH.item_date.setText(DateConverter.resultDateToString(item.getMsg_date(), "a h시 m분"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class Msg_Right_VH extends RecyclerView.ViewHolder {
        TextView item_message, item_date, item_memeber;

        public Msg_Right_VH(View view, Chat_adapter chat_adapter) {
            super(view);
            item_message = itemView.findViewById(R.id.item_send_message);
            item_date = itemView.findViewById(R.id.item_send_date);
            item_memeber = itemView.findViewById(R.id.item_send_member);
        }
    }

    private static class Msg_Left_VH extends RecyclerView.ViewHolder {
        TextView item_name, item_message, item_date, item_member;
        ImageView item_image;

        public Msg_Left_VH(View view, Chat_adapter chat_adapter) {
            super(view);
            item_name = itemView.findViewById(R.id.item_receive_name);
            item_message = itemView.findViewById(R.id.item_receive_message);
            item_date = itemView.findViewById(R.id.item_receive_date);
            item_image = itemView.findViewById(R.id.item_receive_image);
            item_member = itemView.findViewById(R.id.item_receive_member);
        }
    }

    private static class img_Right_VH extends RecyclerView.ViewHolder {
        TextView item_member, item_date;
        ImageView item_image;

        public img_Right_VH(View view, Chat_adapter chat_adapter) {
            super(view);
            item_member = itemView.findViewById(R.id.item_send_image_member);
            item_date = itemView.findViewById(R.id.item_send_image_date);
            item_image = itemView.findViewById(R.id.item_send_image);
        }
    }

    private static class img_Left_VH extends RecyclerView.ViewHolder {
        TextView item_name, item_date, item_member;
        ImageView item_image, item_profile;

        public img_Left_VH(View view, Chat_adapter chat_adapter) {
            super(view);
            item_name = itemView.findViewById(R.id.item_receive_image_name);
            item_member = itemView.findViewById(R.id.item_receive_image_member);
            item_date = itemView.findViewById(R.id.item_receive_image_date);
            item_image = itemView.findViewById(R.id.item_receive_image_message);
            item_profile = itemView.findViewById(R.id.item_receive_image_profile);
        }
    }

    private static class Loc_Receive_VH extends RecyclerView.ViewHolder {
        TextView item_name, item_date, item_member, item_btn;
        ImageView item_image, item_profile;
        public Loc_Receive_VH(View view, Chat_adapter chat_adapter) {
            super(view);

            item_name = view.findViewById(R.id.item_location_name);
            item_date = view.findViewById(R.id.item_location_date);
            item_member = view.findViewById(R.id.item_location_member);
            item_btn = view.findViewById(R.id.item_location_btn);
//            item_image = view.findViewById(R.id.item_location_image);
            item_profile = view.findViewById(R.id.item_location_profile);

            view.setOnClickListener(v -> {
                Intent intent = new Intent(view.getContext(), Home_Location2.class);
                intent.putExtra("flag", true);
                intent.putExtra("lat",chat_adapter.items.get(getAbsoluteAdapterPosition()).getMsg_lat());
                intent.putExtra("log",chat_adapter.items.get(getAbsoluteAdapterPosition()).getMsg_log());
                chat_adapter.context.startActivity(intent);
            });
        }
    }

    private static class Loc_Send_VH extends RecyclerView.ViewHolder {
        TextView item_member, item_date, item_btn;
        ImageView item_image;
        public Loc_Send_VH(View view, Chat_adapter chat_adapter) {
            super(view);

            item_btn = view.findViewById(R.id.item_location_send_btn);
            item_member = view.findViewById(R.id.item_location_send_member);
            item_date = view.findViewById(R.id.item_location_send_date);
//            item_image = view.findViewById(R.id.item_location_send_image);

            view.setOnClickListener(v -> {
                Intent intent = new Intent(view.getContext(), Home_Location2.class);
                intent.putExtra("flag", true);
                intent.putExtra("lat",chat_adapter.items.get(getAbsoluteAdapterPosition()).getMsg_lat());
                intent.putExtra("log",chat_adapter.items.get(getAbsoluteAdapterPosition()).getMsg_log());
                chat_adapter.context.startActivity(intent);
            });
        }
    }
}
