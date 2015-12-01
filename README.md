# android-chat-heads
A facebook like chat heads library for android apps. This includes all the UI physics and spring animations which drive multi user chat behaviour and toggling between maximized, minimized and circular arrangements.

# Demo
![springy chat heads demo](/demo/demo.gif?raw=true)

# How to use

Define the view group in your layout file

        <com.flipkart.chatheads.ui.ChatHeadContainer
        android:id="@+id/chat_head_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
        

Then define the view adapter.

        final ChatHeadContainer chatContainer = (ChatHeadContainer) findViewById(R.id.chat_container);
        chatContainer.setViewAdapter(new ChatHeadViewAdapter() {
            @Override
            public FragmentManager getFragmentManager() {
                return getSupportFragmentManager();
            }

            @Override
            public Fragment instantiateFragment(Object key, ChatHead chatHead) {
                // return the fragment which should be shown when the arrangment switches to maximized (on clicking a chat head)
                // you can use the key parameter to get back the object you passed in the addChatHead method.
                // this key should be used to decide which fragment to show.
                return new Fragment();
            }

            @Override
            public Drawable getChatHeadDrawable(Object key) {
                // this is where you return a drawable for the chat head itself. Typically you return a circular shape
                return getResources().getDrawable(R.drawable.circular_view);
            }
        });
        chatContainer.setOnItemSelectedListener(new ChatHeadContainer.OnItemSelectedListener() {
            @Override
            public boolean onChatHeadSelected(Object key, ChatHead chatHead) {
                if (chatContainer.getArrangementType() == CircularArrangement.class) {
                    System.out.println("Clicked on " + key + " " +
                            "when arrangement was circular");
                }
                chatContainer.setArrangement(MaximizedArrangement.class, null);
                return true;
            }
        });
        chatContainer.addChatHead("head0", false); // you can even pass a custom object instead of "head0"
        chatContainer.addChatHead("head1", false); // a sticky chat head cannot be closed and will remain when all other chat heads are closed.
        

The view adapter is invoked when someone selects a chat head.
In this example I have attached a (String) object to each chat head using generics. You can attach any custom object, for e.g (Conversation) object to denote each chat head.
This object will represent a chat head uniquely and will be passed back in all callbacks.
