package com.example.biketracker.DB.DAOs;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.util.Log;

import androidx.core.util.Consumer;

import com.example.biketracker.DB.Schemas.Device;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class DeviceDAO extends UserDAO {

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

    public void initialize() {
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

    public void getDeviceNames(ObjectId id, Consumer<AtomicReference<String>> callback) {
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

    public void getDeviceName(ObjectId id, String name, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> check = new AtomicReference<>("Error");
        queryFilter = new Document("_id", id);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Device result = task.get();
                if (Objects.equals(result.getName(), name)) {
                    check.set(name);
                }
            }
            callback.accept(check);
        });
    }

    public void getDeviceId(String name, Consumer<AtomicReference<ObjectId>> callback) {
        AtomicReference<ObjectId> check = new AtomicReference<>();
        queryFilter = new Document("name", name);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Device result = task.get();
                check.set(result.getId());
            }
            callback.accept(check);
        });
    }

    public void updateName(String newName, ObjectId id, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> check = new AtomicReference<>("Error");
        Document updateDocument = new Document("$set", new Document("name", newName));
        queryFilter = new Document("_id", id);
        mongoCollection.updateOne(queryFilter, updateDocument).getAsync(it -> {
            if (it.isSuccess()) check.set("Success");
            Log.v("DEVICE NAME", newName);
            callback.accept(check);
        });
    }

    // TODO: Make this functional with the project
    public void delete() {
    }
}