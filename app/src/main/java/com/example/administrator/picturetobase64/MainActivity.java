package com.example.administrator.picturetobase64;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Selection;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youdao.sdk.app.YouDaoApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.attr.content;
import static android.R.attr.data;
import static android.R.attr.text;
import static android.webkit.WebView.HitTestResult.IMAGE_TYPE;

public class MainActivity extends AppCompatActivity {
    Uri imageUri;
    TextView textView;
    String img="";
    final String appSecret="mrTQIUdJjNBAHCL1SQnficYT9ya3NubQ";
    final String appkey="73aeb343c4c5e55e";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        TextView textView=(TextView) findViewById(R.id.showPath);


        super.onCreate(savedInstanceState);
        final ImageView imageView=(ImageView)findViewById(R.id.showImage);
        setContentView(R.layout.activity_main);
        ImageButton button1=(ImageButton)findViewById(R.id.selectImage);
        //setContentView(R.layout.activity_main);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},4);
                else openAlbum();

            }
        });

        ImageButton button3=(ImageButton)findViewById(R.id.work);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendRequestWithOkHttp();

            }
        });

        ImageButton button2=(ImageButton)findViewById(R.id.openWeb);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,WebActivity.class);
                startActivity(intent);
            }
        });



    }
    public static String toUtf8(String str) {
              String result = null;
               try {
                       result = new String(str.getBytes("UTF-8"), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                       // TODO Auto-generated catch block
                        e.printStackTrace();
                   }
             return result;
            }

    private void sendRequestWithOkHttp(){

        Random rand = new Random();
        final int i = rand.nextInt(100);
        final String salt=String.valueOf(i);
        final String str=appkey+img+salt+appSecret;
        final String sign=md5(str);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client=new OkHttpClient();
                    RequestBody requestBody=new FormBody.Builder()
                            .add("img",toUtf8(img))
                            .add("langType","zh-en")
                            .add("detectType","10011")
                            .add("imageType","1")
                            .add("appKey",appkey)
                            .add("salt",salt)
                            .add("sign",toUtf8(sign))
                            .add("docType","json")
                            .build();

                    Request request=new Request.Builder()
                            .url("http://openapi.youdao.com/ocrapi")
                            .post(requestBody)
                            .build();


                    Response response=client.newCall(request).execute();
                    String responseData=response.body().string();
                    JSONObject Data=new JSONObject(responseData);
                    parseJsonData(Data);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJsonData(JSONObject jsonObject){
        String text="";
        try{
            JSONObject Result=jsonObject.getJSONObject("Result");
            JSONArray regions=Result.getJSONArray("regions");
            for(int i=0;i<regions.length();i++){//regions item
                JSONObject region=regions.getJSONObject(i);
                JSONArray lines=region.getJSONArray("lines");
                for (int j=0;j<lines.length();j++){
                    JSONObject line=lines.getJSONObject(j);
                    JSONArray words=line.getJSONArray("words");
                    for (int k=0;k<words.length();k++){
                        JSONObject word=words.getJSONObject(k);
                        text=text+word.getString("text");
                    }
                    text=text+"\n";
                }
            }
            Log.d("Text",text);
            showJsonData(text);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    void showJsonData(final String response)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               TextView textView=(TextView) findViewById(R.id.showPath);
                textView.setMovementMethod(new ScrollingMovementMethod(){
                });
                textView.setText(response);
            }
        });
    }

    void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,2);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 4:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else Toast.makeText(MainActivity.this,"you denied the permission",Toast.LENGTH_SHORT).show();
        }
    }


    @TargetApi(19)
    void handleImageOnKitKat(Intent data){
        try {
            String imagePath = null;
            Uri uri = data.getData();
            if (DocumentsContract.isDocumentUri(MainActivity.this, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = docId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                imagePath = getImagePath(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                imagePath = uri.getPath();
            }
            displayImage(imagePath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        displayImage(imagePath);
    }
    String getImagePath(Uri uri,String selection){
        String path=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst())
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path;
    }

    void displayImage(String imagePath){
        if(imagePath!=null){
            Bitmap bitmap= BitmapFactory.decodeFile(imagePath);
            ImageView imageView=(ImageView)findViewById(R.id.showImage);
            imageView.setImageBitmap(bitmap);
            img=toUtf8(imageToBase64(imagePath));
            textView.setText(imagePath);
        }
    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case 100:
                if(resultCode==RESULT_OK){
                    try {
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        ImageView imageView=(ImageView)findViewById(R.id.showImage);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            case 2:
                if(resultCode==RESULT_OK){
                    if(Build.VERSION.SDK_INT>=19){
                        handleImageOnKitKat(data);
                    }else{
                        handleImageBeforeKitKat(data);
                    }

                }
        }
    }

    public static String imageToBase64(String path) {

        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }
    public static String md5(String string) {
        if(string == null){
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        byte[] btInput = string.getBytes();
        try{
            /** 获得MD5摘要算法的 MessageDigest 对象 */
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            /** 使用指定的字节更新摘要 */
            mdInst.update(btInput);
            /** 获得密文 */
            byte[] md = mdInst.digest();
            /** 把密文转换成十六进制的字符串形式 */
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        }catch(NoSuchAlgorithmException e){
            return null;
        }
    }


}