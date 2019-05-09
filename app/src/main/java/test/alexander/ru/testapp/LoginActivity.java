package test.alexander.ru.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.buttonCommit).setOnClickListener(new View.OnClickListener() {
            //点击提交，存已登录到SharedPreferences
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.textViewPeopleName);
                String peopleName = editText.getText().toString();

                //在bmob创建的位置和安全2个表添加一条(2个REST POST操作)
                new POST_Task().execute("IsSafe", "{\"isSafe\":1,\"name\":\"" + peopleName + "\"}");
                new POST_Task().execute("position", "{\"lat\":1,\"lon\":2,\"name\":\"" + peopleName + "\"}");

                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                //存SharedPreferences  "login", "1"
                editor.putString("peopleName", peopleName);
                editor.commit();

                //跳转到主页面
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

    }

    class POST_Task extends AsyncTask<String, Integer, String> {
        //<doInBackground参数params的类型由Task.execute(params)传入,
        //    onProgressUpdate进度progress的类型,
        //    doInBackground后传给onPostExecute的类型
        private String POST(String tableName, String postBody) {
            String result = "";
            try {
                URL url = new URL("https://api2.bmob.cn/1/classes/" + tableName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-Bmob-Application-Id", "ae69ae4ad1b9328f1993c62a637454a7");
                conn.setRequestProperty("X-Bmob-REST-API-Key", "05d377b293e63f9f9e22788154af1449");
                // 发送POST请求必须设置如下两行
                conn.setDoOutput(true);
                conn.setDoInput(true);
                // 获取URLConnection对象对应的输出流
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(postBody);
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

        @Override
        protected void onPreExecute() {//在 UI 线程中调用
        }

        @Override
        protected String doInBackground(String... params) {//此方法会在工作线程中运行,在这里去处理所有耗时的任务,
            String postResult = POST(params[0], params[1]);
            return postResult;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {//在 UI 线程中调用
        }

        private static final String TAG = "POST_Task";
        @Override
        protected void onPostExecute(String s) {//在 UI 线程中调用
            //在 doInBackground(...) 方法执行完毕后才会运行。
            //doInBackground() 返回的值将发送到 onPostExecute()
            String postResult = s;
            Log.e(TAG, "onPostExecute: "+postResult );
        }
    }
}
