# android-chat-heads
A facebook like chat heads library for android apps. This includes all the UI physics and spring animations which drive multi user chat behaviour and toggling between maximized and minimized modes.



# Integration

The view adapter is invoked when someone selects a chat head.
In this example I have attached a <String> object to each chat head using generics. You can attach any object, for e.g <Conversation> object to denote each chat head.
This object will be passed in all callbacks.

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
