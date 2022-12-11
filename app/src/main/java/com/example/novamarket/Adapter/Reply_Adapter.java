package com.example.novamarket.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.Class.GsonConverter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.CheckEvent;
import com.example.novamarket.EventBus.ReplyEvent;
import com.example.novamarket.EventBus.SendEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Comment;
import com.example.novamarket.Retrofit.DTO_Home;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Reply_Adapter extends RecyclerView.Adapter<Reply_Adapter.ViewHolder> {
    Context context;
    ArrayList<DTO_Comment> items = new ArrayList<>();
    Comment_Adapter comment_adapter;
    String writer;
    boolean flag_like = false;

    public Reply_Adapter(Context context) {
        this.context = context;
    }

    public void setComment_adapter(Comment_Adapter adapter){
       this.comment_adapter = adapter;
    }

    public void addItem(DTO_Comment item) {
        this.items.add(item);
        notifyItemInserted(getItemCount());
    }

    public void setWriter(String writer){
        this.writer = writer;
    }

    public void clearItems() {
        int size = getItemCount();
        this.items.clear();
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public Reply_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull Reply_Adapter.ViewHolder holder, int position) {
        DTO_Comment item = items.get(position);

        // 프로필 설정
        Glide.with(context).load(item.getUser_profile()).circleCrop().into(holder.item_profile);
        holder.item_name.setText(item.getName().trim());
        holder.item_content.setText(item.getContent());
        try {
            holder.item_date.setText(DateConverter.getUploadMinuteTime(item.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 글 작성자
        if(writer.equals(item.getUser_id())){
            holder.item_writer.setVisibility(View.VISIBLE);
        }else{
            holder.item_writer.setVisibility(View.GONE);
        }
        // 내가 작성한 글인지
        if(item.getUser_id().equals(PreferenceManager.getString(context,"user_id"))){
            holder.item_menu.setVisibility(View.VISIBLE);
        }else{
            holder.item_menu.setVisibility(View.GONE);
        }

        // Like 및 답글 세팅
        if (item.getLike() == 0) {
            holder.item_like.setText("좋아요");
        } else {
            holder.item_like.setText("좋아요" + item.getLike());
        }

        if (item.isLike_check()) {
            flag_like = true;
        } else {
            flag_like = false;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView item_name, item_content, item_date, item_like, item_writer;
        private ImageView item_profile, item_menu;

        public ViewHolder(@NonNull View itemView, Reply_Adapter adapter) {
            super(itemView);

            item_name = itemView.findViewById(R.id.item_reply_name);
            item_content = itemView.findViewById(R.id.item_reply_text);
            item_date = itemView.findViewById(R.id.item_reply_date);
            item_like = itemView.findViewById(R.id.item_reply_like);
            item_writer = itemView.findViewById(R.id.item_reply_writer);
            item_profile = itemView.findViewById(R.id.item_reply_image);
            item_menu = itemView.findViewById(R.id.item_reply_menu);

            // 좋아요
            item_like.setOnClickListener(v -> {
                DTO_Comment comment = adapter.items.get(getBindingAdapterPosition());
                if (adapter.flag_like) {
                    adapter.flag_like = false;
                    deleteLike(comment);
                } else {
                    adapter.flag_like = true;
                    writeLike(comment);
                }
            });


            // 수정 및 삭제 메뉴 클릭
            item_menu.setOnClickListener(v -> {
                Dialog dialog = new Dialog(itemView.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_menu);

                dialog.show();
                TextView btn1 = dialog.findViewById(R.id.dialog_btn);
                TextView btn2 = dialog.findViewById(R.id.dialog_btn2);
                TextView btn3 = dialog.findViewById(R.id.dialog_btn3);

                // 목록 커스텀하기
                btn1.setVisibility(View.GONE);
                btn2.setText("게시글 수정");
                btn3.setText("삭제");

                // 게시글 수정
                btn2.setOnClickListener(v1 -> {
                    dialog.dismiss();
                    CommentUpdate(adapter);
                });

                // 게시글 삭제
                btn3.setOnClickListener(v1 -> {
                    dialog.dismiss();
                    CommentDelete(adapter);
                });
            });

        }

        private void deleteLike(DTO_Comment comment) {
            RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
            Call<DTO_Home> call = retrofitInterface.deleteLike(comment.getComment_id(), PreferenceManager.getString(itemView.getContext(), "user_id"), "reply");
            call.enqueue(new Callback<DTO_Home>() {
                @Override
                public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                    if (response.body() != null && response.isSuccessful()) {
                        // 좋아요 수
                        if (response.body().getLike() > 0) {
                            item_like.setText("좋아요" + response.body().getLike());
                        } else {
                            item_like.setText("좋아요");
                        }
                    }
                }

                @Override
                public void onFailure(Call<DTO_Home> call, Throwable t) {
                    Logger.d("에러 메세지 : " + t);
                }
            });
        }

        private void writeLike(DTO_Comment comment) {
            RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
            Call<DTO_Home> call = retrofitInterface.writeLike(comment.getComment_id(), PreferenceManager.getString(itemView.getContext(), "user_id"), "reply");
            call.enqueue(new Callback<DTO_Home>() {
                @Override
                public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // 좋아요 수
                        item_like.setText("좋아요" + response.body().getLike());
                    }
                }

                @Override
                public void onFailure(Call<DTO_Home> call, Throwable t) {
                    Logger.d("에러 메세지 : " + t);
                }
            });
        }

        private void CommentUpdate(Reply_Adapter adapter) {
            DTO_Comment item = adapter.items.get(getBindingAdapterPosition());
            Logger.json(GsonConverter.setLog(item));
            Dialog dialog = new Dialog(itemView.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_check);

            dialog.show();
            TextView title = dialog.findViewById(R.id.dialog_title_check);
            EditText et = dialog.findViewById(R.id.dialog_content_et);
            TextView content = dialog.findViewById(R.id.dialog_content_check);
            TextView yes = dialog.findViewById(R.id.dialog_btn_yes);
            TextView no = dialog.findViewById(R.id.dialog_btn_no);

            // 다이얼로그 초기화 => 제목, 작성 EditText 활성화
            title.setText("게시글 수정");
            et.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
            et.setText(item.getContent());

            // 댓글 수정
            yes.setOnClickListener(v -> {
                RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
                Call<DTO_Comment> call = retrofitInterface.updateReply(item.getComment_id(), item.getUser_id(), item.getComm_id(), et.getText().toString());
                call.enqueue(new Callback<DTO_Comment>() {
                    @Override
                    public void onResponse(Call<DTO_Comment> call, Response<DTO_Comment> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.items.get(getBindingAdapterPosition()).setContent(et.getText().toString());
                            adapter.notifyItemChanged(getBindingAdapterPosition());
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<DTO_Comment> call, Throwable t) {
                        Logger.d("에러 메세지 : " + t.getMessage());
                    }
                });

            });

            no.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }

        private void CommentDelete(Reply_Adapter adapter) {
            DTO_Comment item = adapter.items.get(getBindingAdapterPosition());
            Dialog dialog = new Dialog(itemView.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_check);

            dialog.show();
            TextView yes = dialog.findViewById(R.id.dialog_btn_yes);
            TextView no = dialog.findViewById(R.id.dialog_btn_no);

            Logger.json(GsonConverter.setLog(item));

            yes.setOnClickListener(v -> {
                RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
                Call<DTO_Comment> call = retrofitInterface.deleteReply(item.getComment_id(), item.getUser_id(), item.getComm_id(), item.getGroup());
                call.enqueue(new Callback<DTO_Comment>() {
                    @Override
                    public void onResponse(Call<DTO_Comment> call, Response<DTO_Comment> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EventBus.getDefault().post(new CheckEvent(true));
                            adapter.items.remove(getBindingAdapterPosition());
                            adapter.notifyItemRemoved(getBindingAdapterPosition());
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<DTO_Comment> call, Throwable t) {

                    }
                });
            });

            no.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }
    }
}
