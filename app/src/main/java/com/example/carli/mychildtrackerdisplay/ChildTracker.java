package com.example.carli.mychildtrackerdisplay;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Base64;
import android.util.Log;

import com.example.carli.mychildtrackerdisplay.Model.Location;
import com.example.carli.mychildtrackerdisplay.Utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.ByteBuffer;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class ChildTracker {
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;

    private KeyStore keyStore = null;
    private MutableLiveData<Integer> interval;


    public ChildTracker(){
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            if (user == null)
                user = firebaseAuth.getCurrentUser();
            if (user != null) {
                database = FirebaseDatabase.getInstance().getReference(user.getUid());
                loadInterval();
            }
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG, e.getMessage());
        }
        try {
            keyStore = KeyStore.getInstance(Constants.KEYSTORE);
            keyStore.load(null);
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG,"Error initiating AndroidKeyStore: "+e.getMessage());
        }
    }

    public void setLocation(android.location.Location loc){
        Location location = new Location(loc.getTime(), loc.getLatitude(), loc.getLongitude(), loc.getAccuracy());

        ObjectMapper objectMapper = new ObjectMapper();
        String locString = new String();
        try {
             locString = objectMapper.writeValueAsString(location);
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG, "Error converting location into JSON: "+e.getMessage());
        }
        Log.d(Constants.LOG_TAG, "Location" + locString);
        String toStore = Base64.encodeToString(encryptLocation(locString), Base64.DEFAULT);

        this.addData(toStore);
    }

    private byte[] encryptLocation(String loc){
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, (SecretKey) keyStore.getKey(Constants.KEY_ALIAS, null));
            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(loc.getBytes());

            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + ciphertext.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            return byteBuffer.array();
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG,"Error encrypting location: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void addData(String val){
        database.child(Constants.DB_ENTRY_DATA).push().setValue(val);
    }

    public void loadInterval(){
        database.child(Constants.DB_ENTRY_INTERVAL).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer intval = dataSnapshot.getValue(Integer.class);
                //if (intval != null)
                if (intval != null && intval.equals(Constants.SOS_INTERVAL)){
                    database.child(Constants.DB_ENTRY_INTERVAL).removeEventListener(this);
                    unPair();
                }
                interval.setValue(intval);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void unPair() {
        try {
            keyStore.deleteEntry(Constants.KEY_ALIAS);
            database.removeValue();
            database.child(Constants.DB_ENTRY_USERTYPE).setValue(Constants.USERTYPE_CHILD);
            firebaseAuth.signOut();
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG, "Unable to unpair device. Error: "+e.getMessage());
        }

    }


    public LiveData<Integer> getInterval(){
        if (interval == null)
            interval = new MutableLiveData<>();
        return interval;
    }



}
