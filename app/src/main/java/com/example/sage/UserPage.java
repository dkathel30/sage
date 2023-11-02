package com.example.sage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserPage extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText message_text_text;
    ImageView send_btn;
    List<Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        // Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        message_text_text = findViewById(R.id.message_text_text);
        send_btn = findViewById(R.id.send_btn);
        recyclerView = findViewById(R.id.recyclerView);
        Button menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(UserPage.this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.user_page_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                return onOptionsItemSelected(item);
            });
            popup.show();
        });




        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        loadMessagesFromFirebase();

        send_btn.setOnClickListener(view -> {
            String question = message_text_text.getText().toString().trim();
            addToChat(question, Message.SEND_BY_ME);
            saveMessageToFirebase(question, Message.SEND_BY_ME); // Save message to Firebase
            message_text_text.setText("");
            callAPI(question);
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_page_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_calendar_event) {
            // Handle action_calendar_event
            createCalendarEvent();
            return true;
        } else if (id == R.id.action_set_reminders) {
            // Handle action_set_reminder
            showTimePickerAndSetReminder();
            return true;
        } else if (id == R.id.action_weather_updates) {
            // Handle action_weather_update
            getWeatherUpdate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void createCalendarEvent() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);

        // Setting the event details
        intent.putExtra(CalendarContract.Events.TITLE, "Event Title");
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Event Description");
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Event Location");

        // Set the time (start time, end time) for the event
        Calendar startTime = Calendar.getInstance();
        startTime.set(2023, 10, 30, 10, 0);  // Example: Set the date to October 30, 2023, at 10:00 AM
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTimeInMillis());

        Calendar endTime = Calendar.getInstance();
        endTime.set(2023, 10, 30, 11, 0);  // Example: Set the date to October 30, 2023, at 11:00 AM
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());

        // Attempt to start the activity to add an event
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Handle the situation where no calendar app is installed
            Toast.makeText(this, "No calendar app found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimePickerAndSetReminder() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(UserPage.this,
                (view, selectedHour, selectedMinute) -> setReminderAtSpecificTime(selectedHour, selectedMinute),
                hour, minute, false);
        timePickerDialog.setTitle("Select Time for Reminder");
        timePickerDialog.show();
    }
    private void setReminderAtSpecificTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }



    private void getWeatherUpdate() {
        String API_KEY = "097e4572789199c733900eb557bd412a";
        String CITY_NAME = "Sydney";
        String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=" + CITY_NAME + "&appid=" + API_KEY + "&units=metric";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                        double temperature = jsonObject.getJSONObject("main").getDouble("temp");

                        // Updating the UI with the fetched weather details. Use runOnUiThread() or a handler.
                        runOnUiThread(() -> {

                            Toast.makeText(UserPage.this, "Weather in " + CITY_NAME + ": " + weatherDescription + ", Temperature: " + temperature + "°C", Toast.LENGTH_LONG).show();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle the error
                e.printStackTrace();
            }
        });
    }


    private void loadMessagesFromFirebase() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userChatsRef = mDatabase.child("Users").child(userId).child("Chats");

        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String message = snapshot.child("Message").getValue(String.class);
                    String sender = snapshot.child("Sender").getValue(String.class);

                    if (message != null && sender != null) {
                        messageList.add(new Message(message, sender));
                    }
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log or show an error message to the user
            }
        });
    }

    private void saveMessageToFirebase(String message, String sender) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userChatsRef = mDatabase.child("Users").child(userId).child("Chats").push(); // Create a new chat entry

        // Create a map to store the chat
        Map<String, String> chat = new HashMap<>();
        chat.put("Message", message);
        chat.put("Timestamp", String.valueOf(System.currentTimeMillis())); // or use Firebase's ServerValue.TIMESTAMP
        chat.put("Sender", sender);

        userChatsRef.setValue(chat);
    }

    void addToChat(String message, String sendBy){
        runOnUiThread(() -> {
            messageList.add(new Message(message, sendBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SEND_BY_BOT);
        saveMessageToFirebase(response, Message.SEND_BY_BOT); // Save bot's response to Firebase
    }

    void callAPI(String question){
        // Display a placeholder message indicating the bot is typing.
        messageList.add(new Message("Typing...", Message.SEND_BY_BOT));

        // Constructing the API call payload.
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","gpt-3.5-turbo");

            // Setting up the messages array.
            JSONArray messagesArray = new JSONArray();

            // Add the user's message to the array.
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messagesArray.put(userMessage);

            jsonBody.put("messages", messagesArray);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Setting up the request to OpenAI API.
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API.API_URL)
                .header("Authorization", "Bearer " + API.API)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    try {
                        JSONObject jsonObjectResponse = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObjectResponse.getJSONArray("choices");
                        JSONObject messageObject = jsonArray.getJSONObject(0).getJSONObject("message");
                        String result = messageObject.getString("content");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        addResponse("Failed to parse JSON due to: " + e.getMessage());
                    }

            } else {
                    String responseBodyString = response.body() != null ? response.body().string() : "No response body";
                    addResponse("Failed to load response due to: " + responseBodyString);
                }

            }
        });
    }


    public void onBackClick(View view) {
        onBackPressed();
    }
}
