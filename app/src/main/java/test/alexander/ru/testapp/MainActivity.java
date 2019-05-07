package test.alexander.ru.testapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView locationStatsTextSafe;
    private TextView locationStatsTextUnSafe;
    private LocationManager locationManager;
    private AudioManager audioService;
    private Vibrator vibrator;
    private AssetFileDescriptor fileBee;
    private AlertDialog warningDialog;
    private String peopleName;
    public LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getApplicationContext(), "获取位置...", Toast.LENGTH_SHORT).show();
            if (location != null) {
                new RestPutTask().execute(location.getLongitude(), location.getLatitude(),peopleName);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onProviderEnabled(String provider) {
            Location location=locationManager.getLastKnownLocation(provider);
            if (location != null) {
                new RestPutTask().execute(location.getLongitude(), location.getLatitude(),peopleName);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            doLocationing();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
        vibrator.cancel();
        mediaPlayer.pause();
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
        //每隔2s 更新一次 位置
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检测登录状态
        final SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        String  prefpeopleName=pref.getString("peopleName", null);
        if ( prefpeopleName== null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }
        peopleName=prefpeopleName;
        getSupportActionBar().setTitle("人员:"+peopleName);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        fileBee = getResources().openRawResourceFd(R.raw.bee);
        //设定数据源，并准备播放
        try {
            mediaPlayer.setDataSource(fileBee.getFileDescriptor(), fileBee.getStartOffset(), fileBee.getLength());
            fileBee.close();
            mediaPlayer.setVolume(0.2f, 0.2f);
            mediaPlayer.setLooping(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            mediaPlayer = null;
        }

        locationStatsTextSafe =  findViewById(R.id.LocationStatsSafe);
        locationStatsTextUnSafe = findViewById(R.id.LocationStatsUnSafe);

        // 警报图标对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialog);
        final View WarnIcon = getLayoutInflater().inflate(R.layout.warn_icon, null);
        builder.setView(WarnIcon);
        warningDialog = builder.create();

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



    class RestPutTask extends AsyncTask<Object, Void, Integer> {
        private String PutPosition(final double lon, final double lat,String peopleName) {
            String result = "";
            try {
                URL url = new URL("https://api2.bmob.cn/1/classes/position?where={\"name\":\""+
                        peopleName+"\"}");
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
                // handle error response code it occurs
                int responseCode = conn.getResponseCode();
                InputStream inputStream;
                if (200 <= responseCode && responseCode <= 299) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                inputStream));

                StringBuilder response = new StringBuilder();
                String currentLine;

                while ((currentLine = in.readLine()) != null)
                    response.append(currentLine);

                in.close();

                result = response.toString();
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
        private String GetIsSafe(String peopleName) {
            String result = "";

            ;
            try {
                URL url = new URL("https://api2.bmob.cn/1/classes/IsSafe?where={\"name\":\""+
                        peopleName+"\"}");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Bmob-Application-Id", "ae69ae4ad1b9328f1993c62a637454a7");
                conn.setRequestProperty("X-Bmob-REST-API-Key", "05d377b293e63f9f9e22788154af1449");
                // handle error response code it occurs
                int responseCode = conn.getResponseCode();
                InputStream inputStream;
                if (200 <= responseCode && responseCode <= 299) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                inputStream));

                StringBuilder response = new StringBuilder();
                String currentLine;

                while ((currentLine = in.readLine()) != null)
                    response.append(currentLine);

                in.close();

                result = response.toString();
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
        protected Integer doInBackground(Object... params) {
            String  putPositionResults= PutPosition((double)params[0],(double) params[1],(String)params[2]);
            String getIsSafeResults=GetIsSafe((String)params[2]);
            Log.e("1", "doInBackground: "+putPositionResults );
            Log.e("1", "doInBackground: "+getIsSafeResults);
            int isSafe = 1;
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(getIsSafeResults);
                isSafe = jsonObject.getJSONArray("results").getJSONObject(0).getInt("isSafe");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return isSafe;
        }

        @Override
        protected void onPostExecute(Integer isSafe) {
            super.onPostExecute(isSafe);
            if (isSafe == 1 || isSafe==null) {
                undoWarning();
            } else {
                doWarning();
            }
        }


    }
    int IS_SAFE=1;
    MediaPlayer mediaPlayer = new MediaPlayer();

    boolean shouldPlayBeep = true;

    /**
     * 报警处理,震动,显示红色
     */
    private void doWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.red));
            findViewById(R.id.ConstraintLayout).setBackground(getDrawable(R.color.red));
            locationStatsTextSafe.setVisibility(View.INVISIBLE);
            locationStatsTextUnSafe.setVisibility(View.VISIBLE);
        }
        // 设置手机振动
        vibrator.vibrate(100);

        // 开始播放警报声
        shouldPlayBeep = true;
        //静音或震动模式
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            shouldPlayBeep = false;
            mediaPlayer.pause();
        }
        if (shouldPlayBeep && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * 取消报警处理,震动,显示红色
     */
    private void undoWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.primaryColor));
            findViewById(R.id.ConstraintLayout).setBackground(getDrawable(R.color.white));
            locationStatsTextSafe.setVisibility(View.VISIBLE);
            locationStatsTextUnSafe.setVisibility(View.INVISIBLE);
        }
        if (null != vibrator) {
            vibrator.cancel();// 关闭振动
            shouldPlayBeep = false;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
}


