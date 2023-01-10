package com.example.biketracker.DB.DAOs;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.os.Build;
import android.util.Log;

import androidx.core.util.Consumer;

import com.example.biketracker.DB.PasswordUtils;
import com.example.biketracker.DB.Schemas.User;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class UserDAO {
    String AppId = "biketrackerapp-etvbv";
    MongoCollection<User> mongoCollection;
    App app;
    Credentials anonymousCredentials;
    AtomicReference<io.realm.mongodb.User> user;
    io.realm.mongodb.User appUser;
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
        mongoCollection = mongoDatabase.getCollection("users",
                User.class).withCodecRegistry(pojoCodecRegistry);

        Log.v("Connect initialize", "Successfully instantiated the MongoDB collection handle");
    }

    public void create(String name, String email, String password, Consumer<AtomicInteger> callback) {
        AtomicInteger check = new AtomicInteger(0);
        User user = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ArrayList<ObjectId> deviceIds = new ArrayList<>();
                user = new User(new ObjectId(), name, email, PasswordUtils.hashPassword(password), deviceIds);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mongoCollection.insertOne(user).getAsync(task -> {
            if (task.isSuccess()) {
                check.set(1);
                Log.v("Connect create", "successfully inserted a document with id: " + task.get().getInsertedId());
            } else {
                Log.e("Connect create", "failed to insert documents with: " + task.getError().getErrorMessage());
            }
            callback.accept(check);
        });
    }

    public void read(String email, String password, Consumer<AtomicInteger> callback) {
        AtomicInteger check = new AtomicInteger(0);
        queryFilter = new Document("email", email);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                User result = task.get();
                if (result == null) check.set(1);
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        if (!Objects.equals(result.getPassword(), PasswordUtils.hashPassword(password)))
                            check.set(2);
                        else {
                            Log.v("Connect read", "successfully found a document: " + result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                check.set(3);
                Log.e("Connect read", "failed to find document with: ", task.getError());
            }
            callback.accept(check);
        });
    }

    // TODO: Make this functional with the project
    public void update() {
        queryFilter = new Document("name", "petunia");
        Document updateDocument = new Document("$set", new Document("sunlight", "partial"));
        mongoCollection.updateOne(queryFilter, updateDocument).getAsync(task -> {
            if (task.isSuccess()) {
                long count = task.get().getModifiedCount();
                if (count == 1) {
                    Log.v("Connect update", "successfully updated a document.");
                } else {
                    Log.v("Connect update", "did not update a document.");
                }
            } else {
                Log.e("Connect update", "failed to update document with: ", task.getError());
            }
        });
    }

    // TODO: Make this functional with the project
    public void delete() {
        queryFilter = new Document("color", "green");
        mongoCollection.deleteOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                long count = task.get().getDeletedCount();
                if (count == 1) {
                    Log.v("Connect delete", "successfully deleted a document.");
                } else {
                    Log.v("Connect delete", "did not delete a document.");
                }
            } else {
                Log.e("Connect delete", "failed to delete document with: ", task.getError());
            }
        });
    }

    public void addGroupToUser(ObjectId id, String email, Consumer<AtomicInteger> callback) {
        AtomicInteger check = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        queryFilter = new Document("email", email);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Log.v("ADD GROUP TO USER", "Successfully found a user with the email: " + email);

                User result = task.get();
                ArrayList<ObjectId> ids = result.getGroupIds();
                ids.add(id);
                result.setGroupIds(ids);

                Document updateDocument = new Document("$set", new Document("groupIds", ids));
                mongoCollection.updateOne(queryFilter, updateDocument).getAsync(it -> {
                    if (it.isSuccess()) check.set(1);
                    latch.countDown();
                });
            } else {
                Log.e("ADD GROUP TO USER", String.valueOf(task.getError()));
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

    public void getGroupIds(String email, Consumer<AtomicReference<ArrayList<ObjectId>>> callback) {
        AtomicReference<ArrayList<ObjectId>> ids = new AtomicReference<>(new ArrayList<>());
        queryFilter = new Document("email", email);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                User result = task.get();
                ArrayList<ObjectId> list = result.getGroupIds();
                ids.set(list);
            }
            callback.accept(ids);
        });
    }

    public void removeGroupFromUser(String email, ObjectId groupId, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> check = new AtomicReference<>("Error");
        queryFilter = new Document("email", email);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                User result = task.get();
                ArrayList<ObjectId> ids = result.getGroupIds();
                ids.remove(groupId);
                result.setGroupIds(ids);

                Document updateDocument = new Document("$set", new Document("groupIds", ids));
                mongoCollection.updateOne(queryFilter, updateDocument).getAsync(it -> {
                    if (it.isSuccess()) check.set("Success");
                    callback.accept(check);
                });
            }
            callback.accept(check);
        });
    }
}


