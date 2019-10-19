package kr.gachon.goopago;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Target;
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

    private Button papagoCopy;
    private Button googleCopy;

    DBHelper helper = new DBHelper(this);
    private Spinner source_spinner;
    private Spinner target_spinner;

    private String real_s_lang;                   // 어떤 언어로 작성한 문장을
    private String real_t_lang;                   // 어떤 언어로 번역이 되게 할지... 담는 언어 코드(ex.ko, ja, en) 변수들

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
                String postParams = "source="+real_s_lang+"&target="+real_t_lang+"&text="+text;              // source_language 와 target_language를 번역이 필요한 text와 같이 보냄
                // String postParams = "source=ko&target=en&text=" + text;  //queryString형식으로 보낸다.
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
        private String TARGET = "&target="+ real_t_lang;                      // 문장이 real_t_lang 에 들어있는 값으로 번역이 됨
        private String SOURCE = "&source=" + real_s_lang;                     // 문장이 real_s_lang 에 들어있는 값으로 작성되었음
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

        papagoCopy = (Button) findViewById(R.id.papagoCopy);
        googleCopy = (Button) findViewById(R.id.googleCopy);

        source_spinner = (Spinner) findViewById(R.id.source_spinner);
        target_spinner = (Spinner) findViewById(R.id.target_spinner);

        startActivity(new Intent(this, LoadingActivity.class)); // 앱실행 로딩화면

        String[] s_lang = {"한국어","영어","일본어","중국어(간체)","중국어(번체)","베트남어","태국어","독일어","스페인어","이탈리아어","프랑스어"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, s_lang);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        source_spinner.setAdapter(adapter);

        source_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] t_lang;

                if(parent.getItemAtPosition(position).equals("한국어")) {                                                                                   // 첫 번쨰 스피너에서 한국어가 선택되면
                    real_s_lang = "ko";                                                                                                                   // 작성될 문장은 한국어(ko) 이고
                    t_lang = new String[]{"영어","일본어","중국어(간체)","중국어(번체)","베트남어","태국어","독일어","스페인어","이탈리아어","프랑스어"};    // 두 번째 스피너에 한국어에서 번역이 지원되는 언어(en, ja, zh-CH 등)만 뜬다.
                }else if (parent.getItemAtPosition(position).equals("영어")) {
                    real_s_lang = "en";
                    t_lang = new String[]{"한국어","일본어","중국어(간체)","중국어(번체)"};
                }else if (parent.getItemAtPosition(position).equals("일본어")) {
                    real_s_lang = "ja";
                    t_lang = new String[]{"한국어","영어","중국어(간체)","중국어(번체)"};
                }else if (parent.getItemAtPosition(position).equals("중국어(간체)")) {
                    real_s_lang = "zh-CN";
                    t_lang = new String[]{"한국어","영어","일본어","중국어(번체)"};
                }else if (parent.getItemAtPosition(position).equals("중국어(번체)")) {
                    real_s_lang = "zh-TW";
                    t_lang = new String[]{"한국어","영어","일본어","중국어(간체)"};
                }else if (parent.getItemAtPosition(position).equals("베트남어")) {
                    real_s_lang = "vi";
                    t_lang = new String[]{"한국어"};
                }else if (parent.getItemAtPosition(position).equals("태국어")) {
                    real_s_lang = "th";
                    t_lang = new String[]{"한국어"};
                }else if (parent.getItemAtPosition(position).equals("독일어")) {
                    real_s_lang = "de";
                    t_lang = new String[]{"한국어"};
                }else if (parent.getItemAtPosition(position).equals("스페인어")) {
                    real_s_lang = "es";
                    t_lang = new String[]{"한국어"};
                }else if (parent.getItemAtPosition(position).equals("이탈리아어")) {
                    real_s_lang = "it";
                    t_lang = new String[]{"한국어"};
                }else if (parent.getItemAtPosition(position).equals("프랑스어")) {
                    real_s_lang = "fr";
                    t_lang = new String[]{"한국어"};
                }else
                    t_lang = new String[]{"한국어"};

                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, t_lang);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                target_spinner.setAdapter(adapter2);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        target_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).equals("한국어")) {
                    real_t_lang = "ko";
                } else if (parent.getItemAtPosition(position).equals("영어")) {
                    real_t_lang = "en";
                } else if (parent.getItemAtPosition(position).equals("일본어")) {
                    real_t_lang = "ja";
                } else if (parent.getItemAtPosition(position).equals("중국어(간체)")) {
                    real_t_lang = "zh-CN";
                } else if (parent.getItemAtPosition(position).equals("중국어(번체)")) {
                    real_t_lang = "zh-TW";
                } else if (parent.getItemAtPosition(position).equals("베트남어")) {
                    real_t_lang = "vi";
                } else if (parent.getItemAtPosition(position).equals("태국어")) {
                    real_t_lang = "th";
                } else if (parent.getItemAtPosition(position).equals("독일어")) {
                    real_t_lang = "de";
                } else if (parent.getItemAtPosition(position).equals("스페인어")) {
                    real_t_lang = "es";
                } else if (parent.getItemAtPosition(position).equals("이탈리아어")) {
                    real_t_lang = "it";
                } else if (parent.getItemAtPosition(position).equals("프랑스어")) {
                    real_t_lang = "fr";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                if (resultText.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "저장할 문장이 없습니다", Toast.LENGTH_SHORT).show();
                } else {
                    String beforeText = translationText.getText().toString();           // 번역 전 문장을 받아옴
                    String afterText = resultText.getText().toString();                  // 파파고의 번역 후 문장을 받아옴
                    String whatapi = "papago";

                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.execSQL("insert into sentence (beforeText, afterText, whatapi) values (?,?,?)",         // DB의 sentence 테이블에 데이터 입력
                            new String[]{beforeText, afterText, whatapi});
                    db.close();

                    Toast.makeText(MainActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //구글 번역문 저장 버튼
        googleKeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultText2.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "저장할 문장이 없습니다", Toast.LENGTH_SHORT).show();
                } else {
                    String beforeText = translationText.getText().toString();          // 번역 전 문장을 받아옴
                    String afterText = resultText2.getText().toString();               // 구글의 번역 후 문장을 받아옴
                    String whatapi = "google";

                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.execSQL("insert into sentence (beforeText, afterText, whatapi) values (?,?,?)",       // DB의 sentence 테이블에 데이터 입력
                            new String[]{beforeText, afterText, whatapi});
                    db.close();

                    Toast.makeText(MainActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                }
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

        // 구글 번역 결과 클립보드에 복사
        papagoCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", resultText.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplication(), "클립보드에 복사되었습니다.",Toast.LENGTH_LONG).show();
            }
        });

        // 파파고 번역 결과 클립보드에 복사
        googleCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", resultText2.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplication(), "클립보드에 복사되었습니다.",Toast.LENGTH_LONG).show();
            }
        });
    }

}
