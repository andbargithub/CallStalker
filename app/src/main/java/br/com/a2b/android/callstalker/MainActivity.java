package br.com.a2b.android.callstalker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lvwListaChamadas;
    private SwipeRefreshLayout rfsCallLog;
    List<String> lstRegistros = new ArrayList<>();

    Context contexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contexto = this.getApplicationContext();

        lvwListaChamadas = findViewById(R.id.lvwCallLog);
        rfsCallLog = findViewById(R.id.refreshCallLog);

        lvwListaChamadas.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            //Abre o browser com o número para averiguar se existe no Whatsapp
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String nro = lstRegistros.get(position);


                String urlWP = "http://api.whatsapp.com/send?phone=55" + nro;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlWP));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                //browserIntent.setPackage("com.android.chrome");
                browserIntent.setPackage("com.whatsapp");
                try {
                    contexto.startActivity(browserIntent);
                    //finish();
                }
                catch (Exception erro)
                {
                    Toast.makeText(contexto, "Erro: " + erro.getMessage(), Toast.LENGTH_LONG).show();
                }


            }
        });

        rfsCallLog.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CarregaChamadas();
                rfsCallLog.setRefreshing(false);
            }
        });
    }

    //Método inicial, entrada da execução do Aplicativo
    protected void onResume() {
        super.onResume();
        CarregaChamadas();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getApplicationContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                lstRegistros);

        lvwListaChamadas.setAdapter(adapter);
    }

    //Lê o histórico de chamadas do telefone
    private void CarregaChamadas() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    1);

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lstRegistros = new ArrayList<>();
        Cursor navChamadas = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");

        int cachedNameIndex = navChamadas.getColumnIndex(CallLog.Calls.CACHED_NAME);

        int number = navChamadas.getColumnIndex(CallLog.Calls.NUMBER);
        int type = navChamadas.getColumnIndex(CallLog.Calls.TYPE);
        int date = navChamadas.getColumnIndex(CallLog.Calls.DATE);
        int duration = navChamadas.getColumnIndex(CallLog.Calls.DURATION);

        while(navChamadas.moveToNext()){

            String phNumber = navChamadas.getString(number);
            String callType = navChamadas.getString(type);
            String dir = null;

            int dircode = Integer.parseInt(callType);

            switch (dircode){
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "Outgoing";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "Incoming";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "Missed";
                    break;
            }

            String cachedName = navChamadas.getString(cachedNameIndex);

            String linha = phNumber ;//+ " - " + dir;

            if(dircode != CallLog.Calls.OUTGOING_TYPE) {
                if(!lstRegistros.contains(linha))
                    lstRegistros.add(linha);
            }
        }

        navChamadas.close();



    }
}
