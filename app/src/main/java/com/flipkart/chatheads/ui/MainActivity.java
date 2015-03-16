package com.flipkart.chatheads.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.flipkart.chatheads.R;

import java.util.Set;


public class MainActivity extends ActionBarActivity{




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ChatHeadContainer chatContainer = (ChatHeadContainer) findViewById(R.id.chat_container);
        chatContainer.setViewAdapter(new ChatHeadViewAdapter<String>(){

            @Override
            public FragmentManager getFragmentManager() {
                return MainActivity.this.getFragmentManager();
            }

            @Override
            public Fragment getFragment(String key, ChatHead<String> chatHead) {
                TestFragment testFragment = TestFragment.newInstance("test", key);
                return testFragment;
            }


            @Override
            public View getChatHeadView(String key) {
                return null;
            }


        });
        chatContainer.addChatHead("head0");
        chatContainer.addChatHead("head1");
        Button addButton = (Button) findViewById(R.id.add);
        Button removeButton = (Button) findViewById(R.id.remove);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatContainer.addChatHead("head" + Math.random());
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Object> keys = chatContainer.getChatHeads().keySet();
                Object[] objects = keys.toArray();
                if(objects.length>0)
                {
                    Object object = objects[objects.length - 1];
                    chatContainer.removeChatHead(object);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();


    }


}
