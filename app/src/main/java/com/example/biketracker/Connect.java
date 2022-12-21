package com.example.biketracker;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.biketracker.ui.login.LoginFragment;


import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;


import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class Connect {
    String AppId = "biketrackerapp-etvbv";
    MongoCollection<BikeUser> mongoCollection;
    App app;
    Credentials anonymousCredentials;
    AtomicReference<User> user;
    User appUser;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    CodecRegistry pojoCodecRegistry;
    Document queryFilter;

    public void initialize(){
        String appID = AppId;
        app = new App(new AppConfiguration.Builder(appID).build());

        anonymousCredentials = Credentials.anonymous();
        user = new AtomicReference<>();
        app.loginAsync(anonymousCredentials, it -> {
            if (it.isSuccess()) {
                Log.v("AUTH", "Successfully authenticated anonymously.");
                user.set(app.currentUser());
            } else {
                Log.e("AUTH", it.getError().toString());
            }
        });

        appUser = app.currentUser();
        mongoClient = appUser.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("BikeTrackerApp");
// registry to handle POJOs (Plain Old Java Objects)
        pojoCodecRegistry = fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        mongoCollection = mongoDatabase.getCollection("users",
                BikeUser.class).withCodecRegistry(pojoCodecRegistry);

        Log.v("EXAMPLE", "Successfully instantiated the MongoDB collection handle");

    }



    public void create(EditText name, EditText email, EditText password){
        BikeUser bikeUser = new BikeUser(
                new ObjectId(),
                name.getText().toString(),
                email.getText().toString(),
                password.getText().toString());
        mongoCollection.insertOne(bikeUser).getAsync(task -> {
            if (task.isSuccess()) {
                Log.v("EXAMPLE", "successfully inserted a document with id: " + task.get().getInsertedId());
            } else {
                Log.e("EXAMPLE", "failed to insert documents with: " + task.getError().getErrorMessage());
            }
        });


    }


    public int read(String email, String password) throws InterruptedException {
        queryFilter = new Document("email", email);
        AtomicInteger check = new AtomicInteger(0);

        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                BikeUser result = task.get();
                check.set(1);
                Log.v("EXAMPLE", "successfully found a document: " + result);
                if (Objects.equals(result.getPassword(), password)) {
                    check.set(2);
                }
            } else {
                Log.e("EXAMPLE", "failed to find document with: ", task.getError());
            }
        });

        return check.get();
    }


    public void update(){
        queryFilter = new Document("name", "petunia");
        Document updateDocument = new Document("$set", new Document("sunlight", "partial"));
        mongoCollection.updateOne(queryFilter, updateDocument).getAsync(task -> {
            if (task.isSuccess()) {
                long count = task.get().getModifiedCount();
                if (count == 1) {
                    Log.v("EXAMPLE", "successfully updated a document.");
                } else {
                    Log.v("EXAMPLE", "did not update a document.");
                }
            } else {
                Log.e("EXAMPLE", "failed to update document with: ", task.getError());
            }
        });

    }

    public void delete(){
        queryFilter = new Document("color", "green");
        mongoCollection.deleteOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                long count = task.get().getDeletedCount();
                if (count == 1) {
                    Log.v("EXAMPLE", "successfully deleted a document.");
                } else {
                    Log.v("EXAMPLE", "did not delete a document.");
                }
            } else {
                Log.e("EXAMPLE", "failed to delete document with: ", task.getError());
            }
        });


    }
}


