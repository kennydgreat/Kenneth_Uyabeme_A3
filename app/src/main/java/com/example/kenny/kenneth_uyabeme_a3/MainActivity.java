package com.example.kenny.kenneth_uyabeme_a3;
/*
* Assignment 3
* This app gets the URL from it's raw then displays contents retrieved
* from the URL*/
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class MainActivity extends Activity {
    //Fields
    private Button loadUrlButton;
    private Button loadFromWebButton;
    private Button saveToDeviceButton;

    private TextView displayUrlTextView;
    private TextView disPlayInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Getting reference to buttons and textViews
        loadUrlButton = findViewById(R.id.btn_load_url_from_raw);
        loadFromWebButton = findViewById(R.id.btn_load_from_web);
        saveToDeviceButton = findViewById(R.id.btn_save_to_device);

        //Getting Reference to TextView
        displayUrlTextView = findViewById(R.id.txt_url_display);
        disPlayInfoTextView =  findViewById(R.id.txt_file_contents_display);

        //setting listener for buttons
        /*1)When clicked the URL display will be set to the URL
        * 2) the Info display is set to file content... this for if statements that click that
        * the button were clicked beforehand */
        loadUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 displayUrlTextView.setText(getUrl());
                disPlayInfoTextView.setText(R.string.file_contents_message);
            }
        });
        /*
        * When click this button lauches a messager to get the contents of
        * the JSON file
        * */
        loadFromWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check for internet connection
                if (isNetworkConnected() ){
                    //Checks to see that the load URL button has been clicked
                    if(!displayUrlTextView.getText().toString().equals(getString(R.string.url_displayed_here_message))) {
                        MyTask myTask = new MyTask();
                        myTask.execute(getUrl());
                    }else disPlayInfoTextView.setText(R.string.must_get_url_first_message);
                }
                else disPlayInfoTextView.setText(R.string.no_internet_connection_message);
            }
        });
        /*Save info in student.txt*/
         saveToDeviceButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // checks that the load url and load from web have been clicked first
                 if(!displayUrlTextView.getText().toString().equals(getResources().getString(R.string.url_displayed_here_message))){
                     if(!(disPlayInfoTextView.getText().toString().equals(getString(R.string.must_get_url_first_message))
                             || disPlayInfoTextView.getText().toString().equals(getString(R.string.student_info_saved_message))
                             || disPlayInfoTextView.getText().toString().equals(getString(R.string.file_contents_message)))){

                         displayUrlTextView.setText(getString(R.string.url_displayed_here_message));
                            writeContentsToFile();
                         disPlayInfoTextView.setText(getString(R.string.student_info_saved_message));
                     } else disPlayInfoTextView.setText(R.string.must_load_from_web_first);

                 } else disPlayInfoTextView.setText(getString(R.string.must_get_url_first_message));

             }
         });
    }
    /**
     * This Method checks for internet access
     */

    private boolean isNetworkConnected(){
        // getting a connectivity manager to get network status
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // getting a NeworkInfo instance
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        // return true if and only if connected
        return (networkInfo != null && networkInfo.isConnected());

    }
    // Customer AsynsTask class
    private class MyTask extends AsyncTask<String, Void, String> {

        //This override of doInbackground gets the JSON file from the URL and
        // return the pasered string through String builder object
        @Override
        protected String doInBackground(String... params) {
            try {

                URL url = new URL(params[0]);
                URLConnection conn = url.openConnection();
                HttpURLConnection httpsConn = (HttpURLConnection) conn;
                int responseCode = httpsConn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    InputStream is = httpsConn.getInputStream();
                    Scanner scanner = new Scanner(is);

                    StringBuilder builder= new StringBuilder();
                    while (scanner.hasNext()){
                        builder.append(scanner.nextLine());
                    }
                    return parseStudentFile(builder.toString());

                }else return "Http Error" ;

            }catch (Exception e){
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            disPlayInfoTextView.setText(s);

        }
    }
// this method parses the student JSON file and gets all the data
    //return the info as a string
    private String parseStudentFile(String s) {
            //Build for info string
            StringBuilder builder = new StringBuilder();
            try{
                //JSON file wrapper
                JSONObject wrapper = new JSONObject(s);
                JSONObject studentinfo = wrapper
                        .getJSONObject("student-info");
                // getting the name object
                builder.append(studentinfo.getString("name"))
                        .append("\n")
                        .append(studentinfo.getString("address"))
                        .append("\n");
                //Getting the phone-numbers jsonarray
                JSONArray numbers = studentinfo.getJSONArray("phone-numbers");
                for(int i =0; i< numbers.length(); i++){
                    JSONObject numberSet = numbers.getJSONObject(i);
                    builder.append(numberSet.getString("name"))
                            .append(": ")
                            .append(numberSet.getString("number"))
                            .append("\n");

                }
                return builder.toString();
            }
            catch(JSONException e){
                return e.toString();
            }
    }
    //This method gets the URL from raw and return the url as a String
    public String getUrl(){
        // getting an inputstream from the file in raw
        InputStream inputStream = getResources().openRawResource(R.raw.url);
        //Scanner to scan inputstream
        Scanner scanner = new Scanner(inputStream);

        return scanner.nextLine();

    }
    //This method writes the contents displayed to the txt file student
    public void writeContentsToFile (){
        // Declaring fileOutput stream
        FileOutputStream fos ;
        // gets an openFileOut steam for the student.txt file
        try{
            fos = openFileOutput("student.txt", MODE_PRIVATE);
        }

        catch (FileNotFoundException e){
            disPlayInfoTextView.setText(R.string.cannot_open_file_message);
            return;
        }
        //Writes text in info display into student.txt
        PrintWriter writer = new PrintWriter(fos);
        writer.println(disPlayInfoTextView.getText());
        writer.close();

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
