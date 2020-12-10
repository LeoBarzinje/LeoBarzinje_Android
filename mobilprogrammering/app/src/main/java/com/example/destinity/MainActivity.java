package com.example.destinity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private  final int RC_SIGN_IN= 1;
RecyclerView recyclerView;

RecyclerView.LayoutManager layoutManager;
    private static final int GALLERY_REQUEST = 9;
    private Uri imgUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
    private CollectionReference brukerCollectionReference;
    private List<Users> brukerListe;
    private List<String> brukerUidList;
    private ListenerRegistration firestoreListenerRegistration;
    private ProgramAdapter programAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

            brukerListe= new ArrayList<>();
            brukerUidList = new ArrayList<>();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavController controller = Navigation.findNavController(this, R.id.fragment2);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNav);
        NavigationUI.setupWithNavController(bottomNavigation, controller);
        controller.navigate(R.id.mainFragmentOne);

                storage =FirebaseStorage.getInstance();
                storageReference = storage.getReference();
                firestoreDb = FirebaseFirestore.getInstance();

                    //firebase Collection "users"
                brukerCollectionReference = firestoreDb.collection("users");
        System.out.println("-----------------------------------------d-------------------------------------------------------");
        System.out.println(brukerCollectionReference);
                generetestData();


        Button logOutButton = findViewById(R.id.logOut);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance().signOut(getApplicationContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Signed out ", Toast.LENGTH_LONG);
                    }
                });
            }
        });


        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = auth.getCurrentUser();

                if (currentUser == null){
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),

                            new AuthUI.IdpConfig.GoogleBuilder().build());
                // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
                else {
                    Toast.makeText(getApplicationContext(), "signed in as " + currentUser.getDisplayName(), Toast.LENGTH_LONG).show();


                }
            }

        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_SIGN_IN)
        return;
           // IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser currentUser = auth.getCurrentUser();
                Toast.makeText(getApplicationContext(), "signed in as " + currentUser.getDisplayName(), Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplicationContext(), "signed in cancelled ", Toast.LENGTH_LONG);
            }
        ImageView imgView= findViewById(R.id.profile_image);

                if(requestCode == 1 && resultCode ==RESULT_OK && data != null && data.getData()!=null){
                    imgUri = data.getData();
                    imgView.setImageURI(imgUri);
                    uploadPicture();
                }
    }






//-------------------------------------------------------------- laster opp bildet til firebase
    private void uploadPicture() {
        final ProgressDialog pd= new ProgressDialog(this);
        pd.setTitle("uploading image...");
        pd.show();

      final String randomKey =UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/"+ randomKey);

        riversRef.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                       Snackbar.make(findViewById(R.id.settingsFragment), "image uploaded", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "funker ikke ", Toast.LENGTH_LONG);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Progress: " +(int) progressPercent+ "%");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        CreateFireStoreREadListener();
        auth.addAuthStateListener(authStateListener);

    }

    private void CreateFireStoreREadListener() {
        /* brukerCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        Users user = documentSnapshot.toObject(Users.class);
                        user.setId(documentSnapshot.getId());
                        brukerListe.add(user);
                        brukerUidList.add(user.getId());
                    }
            programAdapter.notifyDataSetChanged();
                }
        else{
                    System.out.println("eror getting documents: "+ task.getException());
                }
            }
        });*/

        firestoreListenerRegistration = brukerCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if( error!= null){
                        System.out.println("feil");
                        return;
                    }

                        for (DocumentChange documentChange : value.getDocumentChanges()){
                            QueryDocumentSnapshot documentSnapshot = documentChange.getDocument();
                            Users user = documentSnapshot.toObject(Users.class);
                            user.setId(documentSnapshot.getId());
                            int pos = brukerListe.indexOf(user.getId());
                            switch (documentChange.getType()){
                                case ADDED:
                                    brukerListe.add(user);
                                    brukerUidList.add(user.getId());
                                    programAdapter.notifyItemInserted(brukerListe.size()-1);
                                    break;
                                case REMOVED:
                                    brukerListe.remove(pos);
                                    brukerUidList.remove(pos);
                                    programAdapter.notifyItemRemoved(pos);
                                    break;
                                case MODIFIED:
                                    brukerListe.set(pos, user);
                                    programAdapter.notifyItemChanged(pos);
                                    break;

                            }


                        }

                }
            });

    }

    @Override
    protected void onPause() {
        super.onPause();
        auth.removeAuthStateListener(authStateListener);
        if(firestoreListenerRegistration != null ){
            firestoreListenerRegistration.remove();
        }
    }


// Henter brukerens info
    public void getInfoUser(View view) {
        Button godtaKnappen = findViewById(R.id.godtaEditInfo);
        godtaKnappen.setVisibility(View.GONE);
        Button cancelKnappen = findViewById(R.id.kanselerEditInformasjon);
        cancelKnappen.setVisibility(View.GONE);

        FirebaseUser currentUser = auth.getCurrentUser();
        TextView editAlderText = findViewById(R.id.editAlder);
        TextView editBoSted = findViewById(R.id.editBosted);
        TextView editInformasjon = findViewById(R.id.editInfo);
        TextView user_alder = findViewById(R.id.alder_user);
        TextView user_bosted = findViewById(R.id.bosted_user);
        TextView  user_description= findViewById(R.id.description_user);
        TextView  bald_alder= findViewById(R.id.alder_bald);
        TextView  bald_bosted= findViewById(R.id.bosted_bald);
        TextView  description_bald= findViewById(R.id.description_bald);
        bald_alder.setText("Alder: ");
        bald_bosted.setText("Bosted: ");
        description_bald.setText("Litt om deg selv: ");

        user_alder.setText(editAlderText.getText().toString());
        user_bosted.setText(editBoSted.getText().toString());
        user_description.setText(editInformasjon.getText().toString());
        Button skjul = findViewById(R.id.hideText);
        skjul.setVisibility(View.VISIBLE);
        Button visInformasjon = findViewById(R.id.knapp);
        visInformasjon.setVisibility(View.GONE);
        TextView brkNavn = findViewById(R.id.brukerNavn);
        brkNavn.setText(currentUser.getDisplayName());



    }


    public void showInfo(View view) {

        Button godtaKnappen = findViewById(R.id.godtaEditInfo);
        godtaKnappen.setVisibility(View.GONE);
        Button cancelKnappen = findViewById(R.id.kanselerEditInformasjon);
        cancelKnappen.setVisibility(View.GONE);


        TextView  bald_alder= findViewById(R.id.alder_bald);
        TextView  bald_bosted= findViewById(R.id.bosted_bald);
        TextView  description_bald= findViewById(R.id.description_bald);
        bald_alder.setText("");
        bald_bosted.setText("");
        description_bald.setText("");


        TextView brkNavn = findViewById(R.id.brukerNavn);
        brkNavn.setText("");
        Button hide = findViewById(R.id.hideText);
        hide.setVisibility(View.GONE);
        Button showInfo = findViewById(R.id.knapp);
        showInfo.setVisibility(View.VISIBLE);

    }


    public void changeUserProfilePicture(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);

        }


    public void knappATrykkerpo(View view) {
        programAdapter = new ProgramAdapter(this, brukerListe);
        String[] usernames = {auth.getCurrentUser().getDisplayName(), "Leo Barzinje"};
        String[] alder = {"40", "20"};
        String[] bosted= {"Lutvannsveien 29", "Oslo "};
        String[] informasjon= {"jeg liker å gå turer og høre på musikk", "danse omkring "};
        int[] brukerbilde = {R.drawable.bilde, R.drawable.bildeto};

        TextView dagensSpors = (TextView) findViewById(R.id.dagspm);
        dagensSpors.setVisibility(View.GONE);
        Button removeButtonA  = findViewById(R.id.knappA);
        Button removeButtonB  = findViewById(R.id.knappB);
        removeButtonA.setVisibility(View.GONE);
        removeButtonB.setVisibility(View.GONE);

        TextView questionDay = findViewById(R.id.questionRemove);
        questionDay.setVisibility(View.GONE);

        RecyclerView recyclerView = findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(programAdapter);


    }

    public void gotoChatFragment(View view) {
        switchToFragment1();
    }
    public void switchToFragment1() {
        NavController controller = Navigation.findNavController(this, R.id.fragment2);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNav);
        NavigationUI.setupWithNavController(bottomNavigation, controller);
        controller.navigate(R.id.chatFragment);

    }

    public void knappBtrykkerpo(View view) {
        String[] usernames = {auth.getCurrentUser().getDisplayName(), "Ole Pettersen"};
        String[] alder = {"40", "25"};
        String[] bosted= {"Lutvannsveien 29", "Bergen"};
        String[] informasjon= {"jeg liker å gå turer og høre på musikk", "gå lange turer "};
        int[] brukerbilde = {R.drawable.bilde, R.drawable.download};

        TextView dagensSpors = (TextView) findViewById(R.id.dagspm);
        dagensSpors.setVisibility(View.GONE);
        Button removeButtonA  = findViewById(R.id.knappA);
        Button removeButtonB  = findViewById(R.id.knappB);
        removeButtonA.setVisibility(View.GONE);
        removeButtonB.setVisibility(View.GONE);

        TextView questionDay = findViewById(R.id.questionRemove);
        questionDay.setVisibility(View.GONE);

        RecyclerView recyclerView = findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(programAdapter);
    }

    public void ChangeuserInfo(View view) {
        TextView editAlderText = findViewById(R.id.editAlder);
        TextView editBoSted = findViewById(R.id.editBosted);
        TextView editInformasjon = findViewById(R.id.editInfo);
        TextView user_alder = findViewById(R.id.alder_user);
        TextView user_bosted = findViewById(R.id.bosted_user);
        TextView  user_description= findViewById(R.id.description_user);
        TextView  bald_alder= findViewById(R.id.alder_bald);
        TextView  bald_bosted= findViewById(R.id.bosted_bald);
        TextView  description_bald= findViewById(R.id.description_bald);
        bald_alder.setText("Alder: ");
        bald_bosted.setText("Bosted: ");
        description_bald.setText("Litt om deg selv: ");


        editAlderText.setVisibility(View.VISIBLE);
        editBoSted.setVisibility(View.VISIBLE);
        editInformasjon.setVisibility(View.VISIBLE);

         user_alder.setVisibility(View.GONE);
         user_alder.setVisibility(View.GONE);
         user_description.setVisibility(View.GONE);

         Button godtaKnappen = findViewById(R.id.godtaEditInfo);
         godtaKnappen.setVisibility(View.VISIBLE);
         Button cancelKnappen = findViewById(R.id.kanselerEditInformasjon);
         cancelKnappen.setVisibility(View.VISIBLE);


    }

    public void sammeInformasjon(View view) {
        TextView editAlderText = findViewById(R.id.editAlder);
        TextView editBoSted = findViewById(R.id.editBosted);
        TextView editInformasjon = findViewById(R.id.editInfo);
        TextView user_alder = findViewById(R.id.alder_user);
        TextView  user_description= findViewById(R.id.description_user);
        showInfo(view);


        editAlderText.setVisibility(View.GONE);
        editBoSted.setVisibility(View.GONE);
        editInformasjon.setVisibility(View.GONE);

        user_alder.setVisibility(View.GONE);
        user_alder.setVisibility(View.GONE);
        user_description.setVisibility(View.GONE);

        Button godtaKnappen = findViewById(R.id.godtaEditInfo);
        godtaKnappen.setVisibility(View.GONE);
        Button cancelKnappen = findViewById(R.id.kanselerEditInformasjon);
        cancelKnappen.setVisibility(View.GONE);
    }

    public void nyInformasjon(View view) {
        TextView editAlderText = findViewById(R.id.editAlder);
        TextView editBoSted = findViewById(R.id.editBosted);
        TextView editInformasjon = findViewById(R.id.editInfo);
        TextView user_alder = findViewById(R.id.alder_user);
        TextView  user_description= findViewById(R.id.description_user);
        TextView user_bosted = findViewById(R.id.bosted_user);
        editAlderText.setVisibility(View.GONE);
        editBoSted.setVisibility(View.GONE);
        editInformasjon.setVisibility(View.GONE);

        user_alder.setVisibility(View.VISIBLE);
        user_bosted.setVisibility(View.VISIBLE);
        user_description.setVisibility(View.VISIBLE);

        Button godtaKnappen = findViewById(R.id.godtaEditInfo);
        godtaKnappen.setVisibility(View.GONE);
        Button cancelKnappen = findViewById(R.id.kanselerEditInformasjon);
        cancelKnappen.setVisibility(View.GONE);

        user_alder.setText(editAlderText.getText().toString());
        user_bosted.setText(editBoSted.getText().toString());
        user_description.setText(editInformasjon.getText().toString());







    }
    private void  generetestData(){
        ArrayList<Users> users = new ArrayList<>();
        users.add(new Users(25,"167fc40b-8ebb-4210-9fce-63f80bedc943",  "Petter Dole", "SExbLDprbRWQo5LWV21noiTEdE83","Oslo", "liker å gå turer"));
      //  users.add(new Users("Ole Dole","dkivG68w09MRICobX42e3XATbav2",  35, "Sarspborg", "danse omkring", "167fc40b-8ebb-4210-9fce-63f80bedc943"));
        //users.add(new Users("Dole Petter","qCbf1PLAveaFlEGsCVWqVfS3fW53",  25, "Halden", "høre på musikk", "167fc40b-8ebb-4210-9fce-63f80bedc943"));

        for(Users auser : users){
            brukerCollectionReference.add(auser);
        }
    }
}
