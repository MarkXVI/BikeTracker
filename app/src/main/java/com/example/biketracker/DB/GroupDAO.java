package com.example.biketracker.DB;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.util.Log;

import androidx.core.util.Consumer;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class GroupDAO {

    String AppId = "biketrackerapp-etvbv";
    MongoCollection<Group> mongoCollection;
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
        mongoCollection = mongoDatabase.getCollection("groups",
                Group.class).withCodecRegistry(pojoCodecRegistry);

        Log.v("Connect initialize", "Successfully instantiated the MongoDB collection handle");
    }

    public void create(Group group, Consumer<AtomicInteger> callback) {
        AtomicInteger check = new AtomicInteger(0);
        mongoCollection.insertOne(group).getAsync(task -> {
            if (task.isSuccess()) check.set(1);
            callback.accept(check);
        });
    }

    public void getGroupName(ObjectId id, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> name = new AtomicReference<>();
        queryFilter = new Document("_id", id);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Log.v("FIND GROUP", "Successfully found a group with the id: " + id);
                Group result = task.get();
                name.set(result.getName());
            } else {
                Log.e("FIND GROUP", "Couldn't find a group with the id: " + id);
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

    public void getDeviceIds(String name, Consumer<AtomicReference<ArrayList<ObjectId>>> callback) {
        AtomicReference<ArrayList<ObjectId>> ids = new AtomicReference<>(new ArrayList<>());
        queryFilter = new Document("name", name);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Log.v("GET GROUP", "Found a group with the name: " + name);
                Group result = task.get();
                ArrayList<ObjectId> list = result.getDeviceIds();
                ids.set(list);
            } else {
                Log.e("GET GROUP", "Failed to find a group with the name: " + name);
            }
            callback.accept(ids);
        });
    }

    public void addDeviceToGroup(ObjectId id, String name, Consumer<AtomicInteger> callback) {
        AtomicInteger check = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        queryFilter = new Document("name", name);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Log.v("ADD DEVICE TO GROUP", "Successfully found a group with the name: " + name);

                Group result = task.get();
                ArrayList<ObjectId> ids = result.getDeviceIds();
                ids.add(id);
                result.setDeviceIds(ids);

                Document updateDocument = new Document("$set", new Document("deviceIds", ids));
                mongoCollection.updateOne(queryFilter, updateDocument).getAsync(it -> {
                    if (it.isSuccess()) check.set(1);
                    latch.countDown();
                });
            } else {
                Log.e("ADD DEVICE TO GROUP", String.valueOf(task.getError()));
                latch.countDown();
            }
        });
        new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            callback.accept(check);
        }).start();
    }
}
