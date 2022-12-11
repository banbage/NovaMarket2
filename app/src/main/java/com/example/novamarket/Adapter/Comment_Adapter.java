package com.example.novamarket.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.novamarket.Activity.Comm_Reply;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.Class.GsonConverter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.SearchEvent;
import com.example.novamarket.EventBus.SendEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Comment;
import com.example.novamarket.Retrofit.DTO_Home;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.security.PublicKey;
import java.text.ParseException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comment_Adapter extends RecyclerView.Adapter<Comment_Adapter.viewHolder> {
    private ArrayList<DTO_Comment> items = new ArrayList<>();
    private Context context;
    private String reply;
    private String writer;
    private TextView item_reply;
    private boolean flag_like = false;


    public Comment_Adapter(Context context) {
        this.context = context;
    }

    public void addItem(DTO_Comment item) {
        this.items.add(item);
        notifyItemInserted(getItemCount());
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public void clearItem() {
        int size = getItemCount();
        this.items.clear();
        notifyItemRangeRemoved(0, size);
    }


    public void writeReply(String content) {
        this.reply = content;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new viewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        DTO_Comment item = items.get(position);
        Logger.json(GsonConverter.setLog(item));
        // 작성자 체크
        if (writer.equals(item.getUser_id())) {
            holder.item_writer.setVisibility(View.VISIBLE);
        } else {
            holder.item_writer.setVisibility(View.GONE);
        }
        if (item.getUser_id().equals(PreferenceManager.getString(context, "user_id"))) {
            holder.item_menu.setVisibility(View.VISIBLE);
        } else {
            holder.item_menu.setVisibility(View.GONE);
        }
        // 프로필 설정
        holder.item_name.setText(item.getName());
        Glide.with(context).load(item.getUser_profile()).circleCrop().into(holder.item_profile);
        // 댓글 내용 설정
        holder.item_content.setText(item.getContent());
        // 작성일자 생성
        try {
            holder.item_date.setText(DateConverter.getUploadMinuteTime(item.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
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

        if (item.getTotal_reply() == 0) {
            holder.item_reply.setText("답글쓰기");
        } else {
            holder.item_reply.setText("답글" + item.getTotal_reply());
        }

        // 답글이 1개라도 있으면
        holder.item_rv.setVisibility(View.VISIBLE);
        if(item.getReplies() != null) {
            if (item.getReplies().size() > 0) {
                for (DTO_Comment reply : item.getReplies()) {
                    holder.reply_adapter.addItem(reply);
                }
            } else {
                holder.reply_adapter.clearItems();
            }
            if (item.getTotal_reply() > 2) {
                holder.item_more.setVisibility(View.VISIBLE);
                holder.item_more_text.setText("답글 " + (item.getTotal_reply() - 2) + "개 더보기");
            } else {
                holder.item_more.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {
        private RecyclerView item_rv;
        private ConstraintLayout item_more;
        private Reply_Adapter reply_adapter;
        private TextView item_name, item_content, item_date, item_like, item_reply, item_writer, item_more_text;
        private ImageView item_profile, item_more_image, item_menu;
        private boolean more = false;

        public viewHolder(@NonNull View itemView, @NonNull Comment_Adapter adapter) {
            super(itemView);
            item_rv = itemView.findViewById(R.id.item_comment_rv);
            item_more = itemView.findViewById(R.id.item_comment_more);
            item_more_image = itemView.findViewById(R.id.item_comment_more_image);
            item_more_text = itemView.findViewById(R.id.item_comment_more_txt);
            item_name = itemView.findViewById(R.id.item_comment_name);
            item_content = itemView.findViewById(R.id.item_comment_content);
            item_date = itemView.findViewById(R.id.item_comment_date);
            item_like = itemView.findViewById(R.id.item_comment_like);
            item_reply = itemView.findViewById(R.id.item_comment_reply);
            item_writer = itemView.findViewById(R.id.item_comment_writer);
            item_profile = itemView.findViewById(R.id.item_comment_image);
            item_menu = itemView.findViewById(R.id.item_comment_menu);


            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(adapter.context, RecyclerView.VERTICAL, false);
            reply_adapter = new Reply_Adapter(adapter.context);
            item_rv.setAdapter(reply_adapter);
            item_rv.setLayoutManager(linearLayoutManager);
            item_rv.setItemAnimator(null);
            reply_adapter.setWriter(adapter.writer);
            reply_adapter.setComment_adapter(adapter);

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

            // 답글 쓰기
            this.item_reply.setOnClickListener(v -> {
                DTO_Comment item = adapter.items.get(getBindingAdapterPosition());
                Intent intent = new Intent(itemView.getContext(), Comm_Reply.class);
                intent.putExtra("comm_id", item.getComm_id());
                intent.putExtra("comment_id", item.getComment_id());
                intent.putExtra("writer", adapter.writer);
                intent.putExtra("user_id", item.getUser_id());
                itemView.getContext().startActivity(intent);
            });

            // 댓글 더보기 기능
            item_more.setOnClickListener(v -> {
                DTO_Comment item = adapter.items.get(getBindingAdapterPosition());
                if (more) {
                    more = false;
                    item_rv.setVisibility(View.GONE);
                    item_more_text.setText("답글 " + item.getTotal_reply() + "개 더보기");
                    item_more_image.setImageResource(R.drawable.down);
                } else {
                    getReply(item);
                    more = true;
                    item_rv.setVisibility(View.VISIBLE);
                    item_more_text.setText("답글 숨기기");
                    item_more_image.setImageResource(R.drawable.up);
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
            Call<DTO_Home> call = retrofitInterface.deleteLike(comment.getComment_id(), PreferenceManager.getString(reply_adapter.context, "user_id"), "comment");
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
            Call<DTO_Home> call = retrofitInterface.writeLike(comment.getComment_id(), PreferenceManager.getString(reply_adapter.context, "user_id"), "comment");
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

        private void getReply(DTO_Comment item) {
            RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
            Call<DTO_Comment> call = retrofitInterface.getMoreReply(item.getComm_id(), item.getComment_id());
            call.enqueue(new Callback<DTO_Comment>() {
                @Override
                public void onResponse(Call<DTO_Comment> call, Response<DTO_Comment> response) {
                    if (response.body() != null && response.isSuccessful()) {
                        reply_adapter.clearItems();
                        for (DTO_Comment item : response.body().getReplies()) {
                            reply_adapter.addItem(item);
                        }
                    }
                }

                @Override
                public void onFailure(Call<DTO_Comment> call, Throwable t) {

                }
            });
        }

        private void CommentUpdate(Comment_Adapter adapter) {
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
                Call<DTO_Comment> call = retrofitInterface.updateComment(item.getComment_id(), item.getUser_id(), item.getComm_id(), et.getText().toString());
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

        private void CommentDelete(Comment_Adapter adapter) {
            DTO_Comment item = adapter.items.get(getBindingAdapterPosition());
            Dialog dialog = new Dialog(itemView.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_check);

            dialog.show();
            TextView yes = dialog.findViewById(R.id.dialog_btn_yes);
            TextView no = dialog.findViewById(R.id.dialog_btn_no);

            yes.setOnClickListener(v -> {
                RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
                Call<DTO_Comment> call = retrofitInterface.deleteComment(item.getComment_id(), item.getUser_id(), item.getComm_id());
                call.enqueue(new Callback<DTO_Comment>() {
                    @Override
                    public void onResponse(Call<DTO_Comment> call, Response<DTO_Comment> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EventBus.getDefault().post(new SendEvent(response.body().getTotal_comment()));
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
