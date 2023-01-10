package com.example.biketracker.DB.DAOs;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.util.Log;

import androidx.core.util.Consumer;

import com.example.biketracker.DB.Schemas.Group;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.ArrayList;
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

    public void getGroupNames(ObjectId id, Consumer<AtomicReference<String>> callback) {
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

    public void updateName(String newName, ObjectId id, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> check = new AtomicReference<>("Error");
        Document updateDocument = new Document("$set", new Document("name", newName));
        queryFilter = new Document("_id", id);
        mongoCollection.updateOne(queryFilter, updateDocument).getAsync(it -> {
            if (it.isSuccess()) check.set("Success");
            Log.v("GROUP NAME", newName);
            callback.accept(check);
        });
    }

    public void deleteGroup(ObjectId id, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> check = new AtomicReference<>("Error");
        queryFilter = new Document("_id", id);
        mongoCollection.deleteOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) check.set("Success");
            callback.accept(check);
        });
    }

    public void getDeviceIds(ObjectId id, Consumer<AtomicReference<ArrayList<ObjectId>>> callback) {
        AtomicReference<ArrayList<ObjectId>> ids = new AtomicReference<>(new ArrayList<>());
        queryFilter = new Document("_id", id);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Group result = task.get();
                ArrayList<ObjectId> list = result.getDeviceIds();
                ids.set(list);
            }
            callback.accept(ids);
        });
    }

    public void getDeviceIdsWithId(ObjectId id, Consumer<AtomicReference<ArrayList<ObjectId>>> callback) {
        AtomicReference<ArrayList<ObjectId>> ids = new AtomicReference<>(new ArrayList<>());
        queryFilter = new Document("_id", id);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Log.v("GET GROUP", "Found a group with the id: " + id);
                Group result = task.get();
                ArrayList<ObjectId> list = result.getDeviceIds();
                ids.set(list);
            } else {
                Log.e("GET GROUP", "Failed to find a group with the id: " + id);
            }
            callback.accept(ids);
        });
    }

    public void getGroupName(ObjectId id, String name, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> check = new AtomicReference<>("Error");
        queryFilter = new Document("_id", id);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Group result = task.get();
                if (Objects.equals(result.getName(), name)) {
                    check.set(name);
                }
            }
            callback.accept(check);
        });
    }

    public void getGroupId(String name, ObjectId id, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> check = new AtomicReference<>("Error");
        queryFilter = new Document("_id", id);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Group result = task.get();
                if (Objects.equals(result.getName(), name)) check.set("Success");
            }
            callback.accept(check);
        });
    }

    public void removeDeviceFromGroup(ObjectId groupId, ObjectId deviceId, Consumer<AtomicReference<String>> callback) {
        AtomicReference<String> check = new AtomicReference<>("Error");
        queryFilter = new Document("_id", groupId);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Group result = task.get();
                ArrayList<ObjectId> ids = result.getDeviceIds();
                ids.remove(deviceId);
                result.setDeviceIds(ids);

                Document updateDocument = new Document("$set", new Document("deviceIds", ids));
                mongoCollection.updateOne(queryFilter, updateDocument).getAsync(it -> {
                    if (it.isSuccess()) check.set("Success");
                    callback.accept(check);
                });
            } else callback.accept(check);
        });
    }

    public void addDeviceToGroup(ObjectId groupId, ObjectId deviceId, Consumer<AtomicInteger> callback) {
        AtomicInteger check = new AtomicInteger(0);
        queryFilter = new Document("_id", groupId);
        mongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {
                Group result = task.get();
                ArrayList<ObjectId> ids = result.getDeviceIds();
                ids.add(deviceId);
                result.setDeviceIds(ids);

                Document updateDocument = new Document("$set", new Document("deviceIds", ids));
                mongoCollection.updateOne(queryFilter, updateDocument).getAsync(it -> {
                    if (it.isSuccess()) check.set(1);
                    callback.accept(check);
                });
            } else callback.accept(check);
        });
    }
}
