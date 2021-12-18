package com.example.finalproject.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WeatherForecast extends AppCompatActivity {

    ImageView  weatherView;
    TextView currentTemperature;
    TextView minTemp;
    TextView maxTemp;
    TextView uvRating;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weatherforecast_layout);

        //Initializing fields
        weatherView = findViewById(R.id.weatherIcon);
        currentTemperature = findViewById(R.id.currentTemperature);
        minTemp = findViewById(R.id.minTemperature);
        maxTemp = findViewById(R.id.maxTemperature);
        uvRating = findViewById(R.id.uvRating);
        progressBar = findViewById(R.id.progressBar);

        //Setting progress bar to visible
        progressBar.setVisibility(View.VISIBLE);

        //Starting new thread
        ForecastQuery fQuery = new ForecastQuery();
        fQuery.execute();

        currentTemperature.setText(fQuery.currentTemp);
        minTemp.setText(fQuery.min);
        maxTemp.setText(fQuery.max);
        uvRating.setText(fQuery.uv);
      // weatherView.setImageResource(fQuery.pictureName);
    }

    private class ForecastQuery extends AsyncTask<String, Integer, String> {

        //Declaring variables
        String uv = null;
        String min = null;
        String max = null;
        String currentTemp = null;
        String pictureName = null;
        Bitmap weatherPic = null;

        protected String doInBackground(String ... args) {
            try {

                //create a URL object of what server to contact:
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=7e943c97096a9784391a981c4d878b22&mode=xml&units=metric");

                //open the connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //wait for data:
                InputStream response = urlConnection.getInputStream();

                //Declaring PullParser instance
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput( response  , "UTF-8");

                int eventType = xpp.getEventType(); //The parser is currently at START_DOCUMENT

                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType == XmlPullParser.START_TAG)
                    {
                        //pointing to start tag
                        if(xpp.getName().equals("temperature"))
                        {
                            //pointing to <temperature> start tag
                            currentTemp = xpp.getAttributeValue(null,    "value");
                            min = xpp.getAttributeValue(null, "min");
                            max = xpp.getAttributeValue(null, "max");
                        }
                        //pointing to weather picture
                        if (xpp.getName().equals("weather")){
                            pictureName = xpp.getAttributeValue(null, "icon");

                            //Get weather picture file
                            URL urlIcon = new URL("http://openweathermap.org/img/w/" + pictureName + ".png");
                            urlConnection = (HttpURLConnection) urlIcon.openConnection();
                            urlConnection.connect();

                            //Check if access to file is ok and grab weather picture file
                            int responseCode = urlConnection.getResponseCode();
                            if (responseCode == 200) {
                                weatherPic = BitmapFactory.decodeStream(urlConnection.getInputStream());
                            }
                           // publishProgress(100);

                            //Save weather picture file to application local directory
                            FileOutputStream outputStream = openFileOutput( pictureName + ".png", Context.MODE_PRIVATE);
                            weatherPic.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
                            outputStream.flush();
                            outputStream.close();
                        }
                    }
                    eventType = xpp.next(); //move to the next xml event and store it in a variable
                }

                Log.i("MainActivity", "The current Temperature is: " + currentTemp) ;
                Log.i("MainActivity", "The maximum Temperature is: " + max) ;
                Log.i("MainActivity", "The minimum Temperature is: " + min) ;

                //create a URL object of what server to contact:
                url = new URL("http://api.openweathermap.org/data/2.5/uvi?appid=7e943c97096a9784391a981c4d878b22&lat=45.348945&lon=-75.759389");

                //open the connection
                urlConnection = (HttpURLConnection) url.openConnection();

                //wait for data:
                response = urlConnection.getInputStream();

                //JSON reading:
                //Build the entire string response:
                BufferedReader reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8), 8);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                String result = sb.toString(); //result is the whole string


                // convert string to JSON:
                JSONObject uvReport = new JSONObject(result);

                //get the double associated with "value"
                float uvRating = (float) uvReport.getDouble("value");
                uv = String.valueOf(uvRating);

                Log.i("MainActivity", "The uv is now: " + uvRating) ;
            }
            catch (Exception e)
            {
                Log.e("Error", e.getMessage());
            }

            publishProgress(25);
            publishProgress(50);
            publishProgress(75);

            return "Done";
        }

        protected void onProgressUpdate(Integer ... args)
        {
            int value = 0;
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(value);
        }

        protected void onPostExecute(String fromDoInBackground)
        {
            currentTemperature.setText(currentTemp);
            minTemp.setText(min);
            maxTemp.setText(max);
            uvRating.setText(uv);
            progressBar.setVisibility(View.INVISIBLE);
            Log.i("HTTP", fromDoInBackground);
        }
    }
}