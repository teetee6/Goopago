package kr.gachon.goopago;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReadDBActivity extends AppCompatActivity {

    LinearLayout layout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_db);

        layout2 = (LinearLayout) findViewById(R.id.layout2);          // activity_read_db의  스크롤뷰 바로 밑 리니어 레이아웃 (여러 위젯 담기 위함)

        DBHelper helper = new DBHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final Cursor cursor = db.rawQuery("select beforeText, afterText, whatapi from sentence", null);
        while (cursor.moveToNext()){                                                             // 메인 화면에서 저장 버튼을 클릭 할 떄 마다
            LinearLayout.LayoutParams innerparams = new LinearLayout.LayoutParams(                // 저장할 문장과 그 번역문, 삭제 버튼을 한 리니어 레이아웃에 담는다.
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            innerparams.bottomMargin = 10;
            innerparams.topMargin = 10;
            final LinearLayout innerlayout = new LinearLayout(this);
            innerlayout.setPadding(30,30,30,30);
            innerlayout.setOrientation((LinearLayout.HORIZONTAL));                           // 번역 전,후 문장과 삭제 버튼 배치를 가로로 하기 위해 horizontal
            innerlayout.setBackgroundResource(R.drawable.layout_border);                    // 레이아웃 간 구분을 보기 쉽게 하기 위함
            innerlayout.setLayoutParams(innerparams);
            layout2.addView(innerlayout);

            LinearLayout.LayoutParams innerViewparams = new LinearLayout.LayoutParams(          // 번역 전,후 문장을 담는 레이아웃
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
            );
            innerViewparams.rightMargin = 5;
            LinearLayout innerViewlayout = new LinearLayout(this);
            innerViewlayout.setOrientation(LinearLayout.VERTICAL);
            innerViewlayout.setLayoutParams(innerViewparams);

            LinearLayout.LayoutParams innerDeleteparams = new LinearLayout.LayoutParams(            // 삭제 버튼을 담는 레이아웃
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 4
            );
            LinearLayout innerDeletelayout = new LinearLayout(this);
            innerDeletelayout.setOrientation(LinearLayout.HORIZONTAL);
            innerDeletelayout.setGravity(Gravity.CENTER_VERTICAL);
            innerDeletelayout.setLayoutParams(innerDeleteparams);

            innerlayout.addView(innerViewlayout);
            innerlayout.addView(innerDeletelayout);

            TextView whatapi = new TextView(this);
            TextView textview1 =  new TextView(this);                         // 번역 전 문장을 담아 보여줄 텍스트뷰
            final TextView textview2 = new TextView(this);                         // 번역 후 문장을 담아 보여줄 텍스트뷰
            Button deleteBtn = new Button(this);                             // 필요없는 저장 리스트 항목 삭제 버튼
            deleteBtn.setText("삭제");

            innerViewlayout.addView(whatapi);
            innerViewlayout.addView(textview1);
            innerViewlayout.addView(textview2);
            innerDeletelayout.addView(deleteBtn);

            if (cursor.getString(2).equals("papago")){                     // 파파고 or 구글, 어떤 api를 사용했는지
                whatapi.setText("[파파고]");
            }
            else
                whatapi.setText("[구글]");

            textview1.setText(cursor.getString(0));
            textview1.setTypeface(null, Typeface.BOLD);                                  // 번역 전 문장을 진하게 표시
            textview2.setText(cursor.getString(1));


            // 토스트로 만들어보려다 포기
/*
            innerViewlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(cursor.getString(2).equals("0")) {
                        Toast.makeText(ReadDBActivity.this, "파파고로 번역한 문장입니다.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ReadDBActivity.this, "구글로 번역한 문장입니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

 */

            deleteBtn.setOnClickListener(new View.OnClickListener() {             // 삭제 버튼을 누르면 선택한 데이터를 DB에서 삭제함
                @Override
                public void onClick(View v) {
                    db.execSQL("delete from sentence where afterText =" + "'" + textview2.getText().toString() + "'");
                    innerlayout.setVisibility(View.GONE);
                }
            });
        }
    }
}
