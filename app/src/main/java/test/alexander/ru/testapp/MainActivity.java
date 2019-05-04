package test.alexander.ru.testapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
    Vibrator vibrator;
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getApplicationContext(), "获取位置...", Toast.LENGTH_LONG).show();
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onProviderEnabled(String provider) {
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

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.e("1", "onActivityResult");
            //用户从设置界面回来了，再检查一下有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.e("1", "shouldShowRequestPermissionRationale() 返回 true, 即上次弹出权限点击了禁止（但没有勾选“下次不在询问”）,则重新请求权限");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    //第一次打开App时
                    Log.e("1", "shouldShowRequestPermissionRationale() 返回 false ，即第一次打开App时,重新请求权限");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            } else {
                //有权限了
                Log.e("1", "已经有权限了");
                doLocationing();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(this, "onRequestPermissionsResult", Toast.LENGTH_LONG).show();
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("1", "权限被用户同意，可以去放肆了");
                    //有权限了
                    doLocationing();

                } else {
                    Log.e("1", "权限被用户拒绝了，洗洗睡吧");

                    Boolean notFoeverRefused = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (notFoeverRefused) {
                        //没有选不再提醒，那我就再次申请！
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    } else {
                        Log.e("1", "shouldShowRequestPermissionRationale() 返回 false ，上次选择禁止并勾选【下次不在询问】" +
                                "现在我们唯一能做的就是跳转到我们 App 的设置界面，让用户手动开启权限了。");
                        Toast.makeText(this, "请手动开启定位权限", Toast.LENGTH_LONG).show();
                        //选了不再提醒，那就调到设置页面让用户手动开权限
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 1);
                    }
                }
                return;
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void doLocationing() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, locationListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationText = (TextView) findViewById(R.id.Location);
        locationText.setText("..........");
        //没有权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("1", "shouldShowRequestPermissionRationale() 返回 true, 即上次弹出权限点击了禁止（但没有勾选“下次不在询问”）,则重新请求权限");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                //第一次打开App时
                Log.e("1", "shouldShowRequestPermissionRationale() 返回 false ，即第一次打开App时,重新请求权限");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            //有权限了
            Log.e("1", "已经有权限了");
            doLocationing();
        }
    }

    private void showLocation(Location location) {
        if (location != null) {
            locationText.setText(formatLocation(location));
            new RestPutTask().execute(location.getLongitude(), location.getLatitude());
        }


    }

    private String formatLocation(final Location location) {
        if (location == null) {
            return "";
        }

        String resultStr = location.getLongitude() + " , " + location.getLatitude()
                + "\n" + String.format("%1$tF %1$tT", new Date(location.getTime()))
                + "\n" + location.getProvider();

        new RestPutTask().execute(location.getLongitude(), location.getLatitude());
        return resultStr;

    }

    class RestPutTask extends AsyncTask<Double, Void, Integer> {
        private String PutPosition(final double lon, final double lat) {
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
        private Integer GetIsSafe(){
            String result = "";
            JSONObject jsonObject = null;;
            try {
                URL url = new URL("https://api2.bmob.cn/1/classes/IsSafe/23611c62ee");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Bmob-Application-Id", "ae69ae4ad1b9328f1993c62a637454a7");
                conn.setRequestProperty("X-Bmob-REST-API-Key", "05d377b293e63f9f9e22788154af1449");
                // 获取URLConnection对象对应的输出流
                if (conn.getResponseCode() == 200) {
                    // 定义BufferedReader输入流来读取URL的响应
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        result += line;
                    }
                } else {
                    result= "请求失败----Code:" + conn.getResponseCode() + "Message:" + conn.getResponseMessage();
                }
                jsonObject = new JSONObject(result);

                conn.disconnect();// 断开连接

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int isSafe=jsonObject.optInt("isSafe");
            return isSafe;
        }
        @Override
        protected Integer doInBackground(Double... doubles) {
            PutPosition(doubles[0], doubles[1]);
            int isSafe= GetIsSafe();
            return isSafe;
        }
        @Override
        protected void onPostExecute(Integer isSafe) {
            super.onPostExecute(isSafe);
//            locationText.setText(locationText.getText() + "\n" + isSafe);
            if(isSafe==1){
                undoWarning();
            }else {
                doWarning();
            }
        }


    }

    //报警处理,震动,显示红色
    private void doWarning() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(2000);  // 设置手机振动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.red));
            findViewById(R.id.ConstraintLayout).setBackground(getDrawable(R.color.red));
        }
    }
    //取消报警处理,震动,显示红色
    private void undoWarning() {
        if(null!=vibrator){
            vibrator.cancel();// 关闭振动
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.primaryColor));
            findViewById(R.id.ConstraintLayout).setBackground(getDrawable(R.color.white));
        }
    }
}


