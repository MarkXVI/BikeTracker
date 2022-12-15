package com.example.biketracker;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.util.Log;
import android.widget.EditText;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

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

    public void Initialize(){
        String appID = AppId;
        app = new App(new AppConfiguration.Builder(appID).build());

        anonymousCredentials = Credentials.anonymous();
        user = new AtomicReference<User>();
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



    public void Create(EditText name, EditText email, EditText password){
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

    public void Read(){
        Document queryFilter  = new Document("name", "Trump");
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                BikeUser result = task.get();
                Log.v("EXAMPLE", "successfully found a document: " + result);
            } else {
                Log.e("EXAMPLE", "failed to find document with: ", task.getError());
            }
        });

    }

    public void Update(){

    }

    public void Delete(){

    }
}
