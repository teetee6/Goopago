package kr.gachon.goopago;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity {

    private EditText translationText;
    private Button translationButton;
    private TextView resultText;
    private TextView resultText2;
    private String result;

    private Button papagoKeep;
    private Button googleKeep;
    private Button keepList;

    DBHelper helper = new DBHelper(this);

    // 백 그라운드에서 파파고 API와 연결하여 번역 결과를 가져옵니다.
    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            StringBuilder output = new StringBuilder();
            String clientId = "vuJK_b3Guf_elis1O5vj";
            String clientSecret = "qZG7FNy2lF";
            try {
                // 번역문을 UTF-8으로 인코딩합니다.
                String text = URLEncoder.encode(translationText.getText().toString(), "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

                // 파파고 API와 연결을 수행합니다.
                URL url = new URL(apiURL);  //URL객체 생성. URL객체의 매개변수가 "http://"포함하면, http연결을 위한 객체를 만듬.
                HttpURLConnection con = (HttpURLConnection) url.openConnection();   //URL객체의 openConnection메소드는 URLConnection객체를 반환함. HTTpURLConeection으로 형변환하자.
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);  //이 메소드는, 브라우저->서버 전달되는 헤더에 들어가는 필드값.
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                // 번역할 문장을 파라미터로 전송합니다.
                String postParams = "source=ko&target=en&text=" + text;  //queryString형식으로 보낸다.
                con.setDoOutput(true);   //이 객체(con)의 출력이 가능하게 함.
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());  //con은 서버가서, 데이터 get해올거임.
                wr.writeBytes(postParams);  //보낼 데이터
                wr.flush();
                wr.close();

                // 번역 결과를 받아옵니다.
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    output.append(inputLine);       //받아온 데이터를 output에 탈탈 넣어둠.
                }
                br.close();
            } catch (Exception ex) {
                Log.e("SampleHTTP", "Exception in processing response.", ex);
                ex.printStackTrace();
            }
            result = output.toString();     //받아온 데이터를 string형으로 전환까지 완료. 이제 Json 파싱하자.
            return null;
        }

        protected void onPostExecute(Integer a) {
            JsonParser parser = new JsonParser();       // JSON 문자열을 객체로 바꿔주는 Gson라이브러리에 있는
            JsonElement element = parser.parse(result);     //JsonParser, JsonElement란 도구를 이용해서 데이터를 가져오자
            if(element.getAsJsonObject().get("errormessage") != null) {         // getAsJsonObject 로 원하는 타입의 데이터 가져오자.
                Log.e("번역 오류", "번역 오류가 발생했습니다. " +                      // getAsJsonObject().get("name") 이면, 키가 "name"인 데이터 가져오는거임.
                        "[오류코드 : " + element.getAsJsonObject().get("errorCode").getAsString() + "]");
            } else if(element.getAsJsonObject().get("message") != null) {
                // 번역 결과 출력
                resultText.setText(element.getAsJsonObject().get("message").getAsJsonObject().get("result")
                        .getAsJsonObject().get("translatedText").getAsString());
            }
        }
    }


    class GoogleBackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        private final static String GoogleApiURL = "https://translation.googleapis.com/language/translate/v2?key=";
        private final static String KEY = "AIzaSyAntVsP46GIoX_EIHeU6CYAqgVHmOgH8fM";
        private final static String TARGET = "&target=en";
        private final static String SOURCE = "&source=ko";
        private final static String QUERY = "&q=";



        private String targettext;
        String texting;
        @Override
        protected Integer doInBackground(Integer... arg0) {
            StringBuilder output = new StringBuilder();
            String KEY = "AIzaSyAntVsP46GIoX_EIHeU6CYAqgVHmOgH8fM";
            try {
                // 번역문을 UTF-8으로 인코딩합니다.
                String sourcetext = URLEncoder.encode(translationText.getText().toString(), "UTF-8");
                String GoogleApiURL = "https://translation.googleapis.com/language/translate/v2?key=";


                // 파파고 API와 연결을 수행합니다.
                URL googleurl = new URL(GoogleApiURL+KEY+SOURCE+TARGET+QUERY+sourcetext);  //URL객체 생성. URL객체의 매개변수가 "http://"포함하면, http연결을 위한 객체를 만듬.
                HttpURLConnection conn = (HttpURLConnection) googleurl.openConnection();   //URL객체의 openConnection메소드는 URLConnection객체를 반환함. HTTpURLConeection으로 형변환하자.

                BufferedReader bffr;
                if (conn.getResponseCode() == 200) { // 정상 호출
                    bffr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {  // 에러 발생
                    bffr = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                String line;
                while ((line = bffr.readLine()) != null) {
                    output.append(line);       //받아온 데이터를 output에 탈탈 넣어둠.
                }
                bffr.close();
            } catch (IOException ex) {
                Log.e("GoogleTranslatorError", ex.getMessage());
            }
            texting = output.toString();



            return null;
        }



        protected void onPostExecute(Integer a) {
            JsonParser googleparser = new JsonParser();
            JsonElement googleelement = googleparser.parse(texting);
            if (googleelement.isJsonObject()) {
                JsonObject obj = googleelement.getAsJsonObject();
                if (obj.get("error") == null) {
                    targettext = obj.get("data").getAsJsonObject().get("translations").getAsJsonArray().get(0).getAsJsonObject().get("translatedText").getAsString();

                    //JSON은 html상에 있었으므로, 특수문자(')등이 있으면, html에서 특수문자(')값은 &#39;로 매칭되서 표시된다.
                    // HTML코드를 디코딩 해줘야함.
                    // [org.apache.commons.lang3.StringEscapeUtils]  api가 그런 디코딩 변환과정을 지원해줌.
                    String HtmlEscaped_targettext = StringEscapeUtils.unescapeHtml4(targettext);

                    resultText2.setText(HtmlEscaped_targettext);


                }
            }
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        translationText = (EditText) findViewById(R.id.translationText);
        translationButton = (Button) findViewById(R.id.translationButton);
        resultText = (TextView) findViewById(R.id.resultText);
        resultText2 = (TextView) findViewById(R.id.resultText2);

        papagoKeep = (Button) findViewById(R.id.papagoKeep);
        googleKeep = (Button) findViewById(R.id.googleKeep);
        keepList = (Button) findViewById(R.id.keepList);

        translationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BackgroundTask().execute();
                new GoogleBackgroundTask().execute();

            }
        });

        //파파고 번역문 저장 버튼
        papagoKeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String beforeText = translationText.getText().toString();           // 번역 전 문장을 받아옴
                String afterText = resultText.getText().toString();                  // 파파고의 번역 후 문장을 받아옴

                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL("insert into sentence (beforeText, afterText) values (?,?)",         // DB의 sentence 테이블에 데이터 입력
                        new String[]{beforeText, afterText});
                db.close();
            }
        });

        //구글 번역문 저장 버튼
        googleKeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String beforeText = translationText.getText().toString();          // 번역 전 문장을 받아옴
                String afterText = resultText2.getText().toString();               // 구글의 번역 후 문장을 받아옴

                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL("insert into sentence (beforeText, afterText) values (?,?)",       // DB의 sentence 테이블에 데이터 입력
                        new String[]{beforeText, afterText});
                db.close();
            }
        });

        //저장된 문장 리스트 확인 가능한 버튼
        keepList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReadDBActivity.class);
                startActivity(intent);
            }
        });

    }
}
