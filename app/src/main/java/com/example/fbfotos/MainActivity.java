package com.example.fbfotos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fbfotos.Modelo.Cliente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
private Button btnSelecionaFoto, btnUploadFoto, btnDownloadFoto;
private ImageView imgFotoUpload, imgFotoDownload;
StorageReference mstorageReference;
Uri caminhoImagem;
private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
private DatabaseReference databaseReference = firebaseDatabase.getReference("ibagens");
private List<Cliente> clienteList = new ArrayList<>();
private ArrayAdapter<Cliente>clienteArrayAdapter;
private String nomeArquivo=null;
private EditText edtIndice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarFirebase();
        carregaWidgets();
        botoes();
    }
    private void iniciarFirebase(){
        FirebaseApp.initializeApp(MainActivity.this);
        mstorageReference = FirebaseStorage.getInstance().getReference("ibagens");
        //pesquisa("");
    }
    private void carregaWidgets(){
        btnDownloadFoto=(Button)findViewById(R.id.btnDownloadFoto);
        btnSelecionaFoto=(Button)findViewById(R.id.btnBuscaFoto);
        btnUploadFoto=(Button)findViewById(R.id.btnUploadFoto);
        imgFotoDownload=(ImageView)findViewById(R.id.imgFotoDownload);
        imgFotoUpload=(ImageView)findViewById(R.id.imgFotoDispositivo);
        edtIndice=(EditText)findViewById(R.id.edtIndice);
    }
    private void botoes(){
        btnSelecionaFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buscaFoto();
            }
        });

        btnUploadFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gravaDatabase();
                uploadFoto();
            }
        });

        btnDownloadFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pesquisa("");
                downloadFoto(MainActivity.this);
            }
        });
    }

    private void buscaFoto(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK){
            caminhoImagem=data.getData();
            imgFotoUpload.setImageURI(caminhoImagem);
            Toast.makeText(MainActivity.this, "entrou", Toast.LENGTH_LONG);
        }
    }
    private void gravaDatabase(){
        Date dt = new Date();
        Cliente cli = new Cliente();
        cli.setId("i");
        nomeArquivo=String.valueOf(dt.getDate()+dt.getTime());
        cli.setNome(nomeArquivo);
        cli.setEndereco(cli.getNome());
        DatabaseReference clieneRef = databaseReference.child(cli.getNome());
        clieneRef.setValue(cli);
    }

    private void uploadFoto(){
        StorageReference ref = mstorageReference.child(nomeArquivo);
        ref.putFile(caminhoImagem)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Upload com sucesso", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Erro ao realizar upload", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void pesquisa(String nome){
        Query query;
        query = FirebaseDatabase.getInstance().getReference("ibagens");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clienteList.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    if (dataSnapshot.exists()){
                        Cliente cliente = dataSnapshot1.getValue(Cliente.class);
                        clienteList.add(cliente);
                    }
                }
                clienteArrayAdapter = new ArrayAdapter<Cliente>(MainActivity.this,
                android.R.layout.simple_list_item_1, clienteList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void downloadFoto(Context context){
        Integer indice = 0;
        indice = Integer.valueOf(edtIndice.getText().toString());
        pesquisa("");
        StorageReference storageReference = mstorageReference;
        if (clienteList.isEmpty()==false){
            storageReference.child(clienteList.get(indice).getEndereco()).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(MainActivity.this).load(uri).into(imgFotoDownload);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }
}