package test.alexander.ru.testapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView locationText;
    private LocationManager locationManager;
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationText = (TextView) findViewById(R.id.Location);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationText.setText("..........");
    }

    private void showLocation(Location location) {
        if (location != null) {
            locationText.setText(formatLocation(location));

        }


    }

    private String formatLocation(final Location location) {
        if (location == null) {
            return "";
        }

        String resultStr = location.getLongitude()+ " , " + location.getLatitude()
                + "\n" + String.format("%1$tF %1$tT", new Date(location.getTime()))
                + "\n" + location.getProvider();

        new RestPutTask().execute(location.getLongitude(),location.getLatitude());
        return resultStr;

    }
    class RestPutTask extends AsyncTask<Double, Void, String> {
        private   String PUT(final double lon, final double lat) {
            String result = "";
            try {

                URL url = new URL("https://api2.bmob.cn/1/classes/position/b5bbd688b1");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-Bmob-Application-Id", "ae69ae4ad1b9328f1993c62a637454a7");
                conn.setRequestProperty("X-Bmob-REST-API-Key", "05d377b293e63f9f9e22788154af1449");
                // 发送POST请求必须设置如下两行
                conn.setDoOutput(true);
                conn.setDoInput(true);
                // 获取URLConnection对象对应的输出流
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                String jsonParam = "{\"lon\":" + lon + ",\"lat\":" + lat + "}";

                out.print(jsonParam);
                // flush输出流的缓冲
                out.flush();
                if (conn.getResponseCode() == 200) {
                    // 定义BufferedReader输入流来读取URL的响应
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        result += line;
                    }
                } else {
                    return "请求失败----Code:" + conn.getResponseCode() + "Message:" + conn.getResponseMessage();
                }
                conn.disconnect();// 断开连接

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            locationText.setText(locationText.getText()+"\n"+s);
        }

        @Override
        protected String doInBackground(Double... doubles) {
            return PUT(doubles[0],doubles[1]);
        }
    }
}


