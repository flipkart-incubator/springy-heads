# THIS PROJECT IS NO MORE MAINTAINED. A good alternative is at https://github.com/google/hover
# Springy heads
A chat head library for use within your apps. This includes all the UI physics and spring animations which drive multi user chat behaviour and toggling between maximized, minimized and circular arrangements. 

# Demo
![springy chat heads demo](/demo/demo.gif?raw=true)

You can also download the demo app

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=com.flipkart.springyheads.demo)


# Installation
Gradle:
```groovy
compile 'com.flipkart.springyheads:library:0.9.6'
```


# How to use

Define the view group in your layout file
```xml
<com.flipkart.chatheads.ui.ChatHeadContainer
android:id="@+id/chat_head_container"
android:layout_width="match_parent"
android:layout_height="match_parent"/>  
```

Then define the view adapter in your activity
```java
final ChatHeadContainer chatContainer = (ChatHeadContainer) findViewById(R.id.chat_container);
chatContainer.setViewAdapter(new ChatHeadViewAdapter() {
    @Override
    public FragmentManager getFragmentManager() {
        return getSupportFragmentManager();
    }

    @Override
    public Fragment attachView(Object key, ChatHead chatHead) {
        // return the fragment which should be shown when the arrangment switches to maximized (on clicking a chat head)
        // you can use the key parameter to get back the object you passed in the addChatHead method.
        // this key should be used to decide which fragment to show.
        return new Fragment();
    }

    @Override
    public Drawable getChatHeadDrawable(Object key) {
        // this is where you return a drawable for the chat head itself based on the key. Typically you return a circular shape
        // you may want to checkout circular image library https://github.com/flipkart-incubator/circular-image
        return getResources().getDrawable(R.drawable.circular_view);
    }
});
```        
Then add the chat heads
```java
chatContainer.addChatHead("head0", false); // you can even pass a custom object instead of "head0"
chatContainer.addChatHead("head1", true); // a sticky chat head (passed as 'true') cannot be closed and will remain when all other chat heads are closed.
```
And finally set the arrangement
```java
chatContainer.setArrangement(MinimizedArrangement.class, null);
```
The view adapter is invoked when someone selects a chat head.
In this example a String object ("head0") is attached to each chat head. You can instead attach any custom object, for e.g a Conversation object to denote each chat head.
This object will represent a chat head uniquely and will be passed back in all callbacks.

# Toggle arrangements
You can toggle between the Minimized and Maximized arrangement like this
```java
chatContainer.setArrangement(MinimizedArrangement.class, null);
            
/** OR **/
             
chatContainer.setArrangement(MaximizedArrangement.class, null);

```
# Callbacks
```java
chatContainer.setListener(new ChatHeadListener() {
    @Override
    public void onChatHeadAdded(Object key) {
        //called whenever a new chat head with the specified 'key' has been added
    }

    @Override
    public void onChatHeadRemoved(Object key, boolean userTriggered) {
        //called whenever a new chat head with the specified 'key' has been removed.
        // userTriggered: 'true' says whether the user removed the chat head, 'false' says that the code triggered it
    }

    @Override
    public void onChatHeadArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement) {
        //called whenever the chat head arrangement changed. For e.g minimized to maximized or vice versa.
    }

    @Override
    public void onChatHeadAnimateStart(ChatHead chatHead) {
        //called when the chat head has started moving (
    }

    @Override
    public void onChatHeadAnimateEnd(ChatHead chatHead) {
        //called when the chat head has settled after moving
    }
});
```
In normal scenarios you dont need to know when a chat head is selected because the fragment returned by the view adapter is automatically attached to it. For special cases where you need to perform any external action, you can use item selected listener.
```java
chatContainer.setOnItemSelectedListener(new ChatHeadContainer.OnItemSelectedListener() {
    @Override
    public boolean onChatHeadSelected(Object key, ChatHead chatHead) {
        if (chatContainer.getArrangementType() == MaximizedArrangement.class) {
            Log.d("springyheads","Clicked on " + key + " " +
                    "when arrangement was Maximized");
        }
        return false; //returning true will mean that you have handled the behaviour and the default behaviour will be skipped
    }
});
```
# Configuration
You can override the standard sizes by writing a custom class which extends the default config class.
For e.g., the below class defines some standard properties and also the initial position of the chat head.
```java
public class CustomChatHeadConfig extends ChatHeadDefaultConfig {
    public CustomChatHeadConfig(Context context, int xPosition, int yPosition) {
        super(context);
        setHeadHorizontalSpacing(ChatHeadUtils.dpToPx(context, -2));
        setHeadVerticalSpacing(ChatHeadUtils.dpToPx(context, 2));
        setHeadWidth(ChatHeadUtils.dpToPx(context, 50));
        setHeadHeight(ChatHeadUtils.dpToPx(context, 50));
        setInitialPosition(new Point(xPosition, yPosition));
        setCloseButtonHeight(ChatHeadUtils.dpToPx(context, 50));
        setCloseButtonWidth(ChatHeadUtils.dpToPx(context, 50));
        setCloseButtonBottomMargin(ChatHeadUtils.dpToPx(context, 100));
        setCircularRingWidth(ChatHeadUtils.dpToPx(context, 53));
        setCircularRingHeight(ChatHeadUtils.dpToPx(context, 53));
    }

    @Override
    public int getMaxChatHeads(int maxWidth, int maxHeight) {
        return (int) Math.floor(maxWidth / (getHeadWidth() + getHeadHorizontalSpacing(maxWidth, maxHeight))) - 1;
    }
}
```
Once this config class is defined, you can set it to the manager
```java
chatContainer.setConfig(new CustomChatHeadConfig(this, 0, 100);
```

# Setting badge count

Do check out our library circular-image (https://github.com/flipkart-incubator/circular-image) for generating a circular drawable containing upto 4 smaller bitmaps as well as a badge count.

# License
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

# More info
You can find a working example in MainActivity of demo module included in the source. 
If you want to add a feature or fix a bug, please issue a pull request. 
This implementation of chat heads is meant to be used within the activity context and cannot be used inside a service.
