package ar.edu.itba.ingesoft.ui.chats;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ar.edu.itba.ingesoft.Firebase.Authenticator;
import ar.edu.itba.ingesoft.Classes.Chat;
import ar.edu.itba.ingesoft.Classes.Message;
import ar.edu.itba.ingesoft.Interfaces.DatabaseEventListeners.OnChatEventListener;
import ar.edu.itba.ingesoft.R;
import ar.edu.itba.ingesoft.ui.recyclerviews.Adapters.ChatsMessagesAdapter;

public class ChatMessagesFragment extends Fragment {

    public static final String TAG = "chat_message_fragment";

    private ChatMessagesViewModel mViewModel;

    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInputEditText;
    private FloatingActionButton sendFloatingActionButton;

    private String chatID;
    private String recipient;
    private String recipientName;

    private boolean isNewChat = false;

    public static ChatMessagesFragment newInstance() {
        return new ChatMessagesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chat_messages_fragment, container, false);

        this.messagesRecyclerView = (RecyclerView) root.findViewById(R.id.messages_recycler_view);
        this.messageInputEditText = (TextInputEditText) root.findViewById(R.id.chat_message_input_edit_text);
        this.sendFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.chat_message_send_button);

        // When the message input is clicked, the rv is scrolled to the last message
        root.findViewById(R.id.chat_message_input_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNewChat && mViewModel.retrieveMessages() != null && mViewModel.retrieveMessages().size() > 0){
                    messagesRecyclerView.scrollToPosition(mViewModel.retrieveMessages().size() - 1);
                    //messagesRecyclerView.smoothScrollToPosition(0);
                    Log.d(TAG, "smooth scrolling on message input edit text");
                }
            }
        });

        // Listener to send the message
        this.sendFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageInputEditText.getText() != null){
                    String message = messageInputEditText.getText().toString();
                    if (message.length() > 0){
                        // Getting the logged user
                        FirebaseUser fu = new Authenticator().getSignedInUser();

                        if (isNewChat){
                            chatID = mViewModel.createChat(fu.getEmail(), recipient, fu.getDisplayName(), recipientName);
                            isNewChat = false;
                            mViewModel.setUpChatChangeListener(chatID, new OnChatEventListener(){

                                @Override
                                public void onChatRetrieved(Chat chat) {
                                    throw new RuntimeException("Not Implemented");
                                }

                                @Override
                                public void onChatChanged(Chat chat) {
                                    ChatsMessagesAdapter adapter = (ChatsMessagesAdapter)messagesRecyclerView.getAdapter();

                                    if (adapter != null){
                                        adapter.messagesChanged(chat.getMessages());
                                    } else {
                                        Log.e(TAG, "Null adapter");
                                    }
                                }
                            });
                        }

                        // Generating the new message instance
                        Message newMessage = new Message(fu.getEmail(), message, new Date());

                        // Notify the adapter of the new message
                        ChatsMessagesAdapter adapter = ((ChatsMessagesAdapter)messagesRecyclerView.getAdapter());
                        //if (adapter != null)
                            //adapter.addMessage(newMessage);

                        // Notify the view model of the new message
                        mViewModel.addMessage(getContext(), newMessage, new OnChatEventListener() {
                            @Override
                            public void onChatRetrieved(Chat chat) {
                                throw new RuntimeException("Not Implemented");
                            }

                            @Override
                            public void onChatChanged(Chat chat) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (adapter != null){
                                            adapter.updateData(chat.getMessages());
                                            messagesRecyclerView.scrollToPosition(chat.getMessages().size() - 1);
                                        }
                                    }
                                });
                            }
                        });

                        // Clearing the message text
                        messageInputEditText.setText("");
                    } else {

                    }
                } else {

                }
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mViewModel = ViewModelProviders.of(this).get(ChatMessagesViewModel.class);

        if (this.chatID != null && !this.isNewChat){
            this.mViewModel.setChatID(chatID);
        }

        // Set up of the recycler view
        this.messagesRecyclerView.setHasFixedSize(true);
        this.messagesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));
        ChatsMessagesAdapter adapter = new ChatsMessagesAdapter(this.mViewModel.retrieveMessages(), new Authenticator().getSignedInUser().getEmail());
        this.messagesRecyclerView.setAdapter(adapter);

        if (!isNewChat){
            this.mViewModel.setUpChatChangeListener(this.chatID, new OnChatEventListener(){

                @Override
                public void onChatRetrieved(Chat chat) {
                    throw new RuntimeException("Not Implemented");
                }

                @Override
                public void onChatChanged(Chat chat) {
                    ChatsMessagesAdapter adapter = (ChatsMessagesAdapter)messagesRecyclerView.getAdapter();

                    if (adapter != null){
                        adapter.messagesChanged(chat.getMessages());
                    } else {
                        Log.e(TAG, "Null adapter");
                    }
                }
            });
            // Scroll the rv to the last message
            //this.messagesRecyclerView.smoothScrollToPosition(this.mViewModel.getChatMessageCount() - 1);
        }
    }

    /**
     * Method to set the chat ID in the fragment
     * @param chatID of the chat
     */
    public void setChatID(String chatID){
        if (chatID != null){
            this.chatID = chatID;
        }
        this.isNewChat = chatID == null;
    }

    /**
     * Method to set the recipient of the message
     * @param recipient of the message
     */
    public void setRecipient(String recipient, String recipientName){
        this.recipient = recipient;
        this.recipientName = recipientName;
    }

}
