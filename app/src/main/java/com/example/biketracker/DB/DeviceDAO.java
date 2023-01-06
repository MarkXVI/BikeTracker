package com.example.biketracker.DB;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.util.Log;

import androidx.core.util.Consumer;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class DeviceDAO extends Connect {

    String AppId = "biketrackerapp-etvbv";
    MongoCollection<Device> mongoCollection;
    App app;
    Credentials anonymousCredentials;
    AtomicReference<User> user;
    User appUser;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    CodecRegistry pojoCodecRegistry;
    Document queryFilter;

    public void initialize(Runnable callback) {
        app = new App(new AppConfiguration.Builder(AppId).build());

        anonymousCredentials = Credentials.anonymous();
        user = new AtomicReference<>();
        app.loginAsync(anonymousCredentials, it -> {
            if (it.isSuccess()) {
                Log.v("Connect AUTH", "Successfully authenticated anonymously.");
                user.set(app.currentUser());
            } else {
                Log.e("Connect AUTH", it.getError().toString());
            }
            callback.run();
        });

        appUser = app.currentUser();
        assert appUser != null;
        mongoClient = appUser.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("BikeTrackerApp");
        // registry to handle POJOs (Plain Old Java Objects)
        pojoCodecRegistry = fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        mongoCollection = mongoDatabase.getCollection("devices",
                Device.class).withCodecRegistry(pojoCodecRegistry);

        Log.v("Connect initialize", "Successfully instantiated the MongoDB collection handle");
    }

    public void create(Device device, Consumer<AtomicInteger> callback) {
        AtomicInteger check = new AtomicInteger(0);
        mongoCollection.insertOne(device).getAsync(task -> {
            if (task.isSuccess()) check.set(1);
            callback.accept(check);
        });
    }

    public void getDeviceName(ObjectId id, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> name = new AtomicReference<>();
        queryFilter = new Document("_id", id);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Log.v("FIND DEVICE", "Successfully found a device with the id: " + id);
                Device result = task.get();
                name.set(result.getName());
            } else {
                Log.e("FIND DEVICE", "Couldn't find a device with the id: " + id);
            }
            callback.accept(name);
        });
    }

    // TODO: Make this functional with the project
    public void update() {
    }

    // TODO: Make this functional with the project
    public void delete() {
    }
}