package kr.gachon.goopago;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReadDBActivity extends AppCompatActivity {

    LinearLayout layout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_db);

        layout2 = (LinearLayout) findViewById(R.id.layout2);           // activity_read_db의  스크롤뷰 바로 밑 리니어 레이아웃 (여러 위젯 담기 위함)

        DBHelper helper = new DBHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select beforeText, afterText from sentence", null);
        while (cursor.moveToNext()){                                                             // 메인 화면에서 저장 버튼을 클릭 할 떄 마다
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(                    // 저장할 문장과 그 번역문, 삭제 버튼을 한 리니어 레이아웃에 담는다.
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            LinearLayout innerlayout = new LinearLayout(this);
            innerlayout.setOrientation(LinearLayout.VERTICAL);
            layout2.addView(innerlayout);

            final TextView textview =  new TextView(this);                         // 번역 전 문장을 담아 보여줄 텍스트뷰
            final TextView textview2 = new TextView(this);                         // 번역 후 문장을 담아 보여줄 텍스트뷰
            final Button deleteBtn = new Button(this);                             // 필요없는 저장 리스트 항목 삭제 버튼
            deleteBtn.setText("삭제");
            innerlayout.addView(textview);
            innerlayout.addView(textview2);
            innerlayout.addView(deleteBtn);

            textview.setText(cursor.getString(0));
            textview2.setText(cursor.getString(1));
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteBtn.setVisibility(View.GONE);
                    textview.setVisibility(View.GONE);
                    textview2.setVisibility(View.GONE);

                    // db.execSQL("delete from sentence where afterText =" + textview2);

                }
            });
        }
    }
}
