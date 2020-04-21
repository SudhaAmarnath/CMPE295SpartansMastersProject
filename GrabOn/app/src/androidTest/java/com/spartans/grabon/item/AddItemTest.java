package com.spartans.grabon.item;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.rule.ActivityTestRule;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.spartans.grabon.R;
import com.spartans.grabon.utils.Singleton;
import com.theartofdev.edmodo.cropper.CropImage;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.victoralbertos.device_animation_test_rule.DeviceAnimationTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.spartans.grabon.item.ImageMatcher.hasDrawable;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_EXTRA_RESULT;
import static org.hamcrest.JMock1Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class AddItemTest {

//    @Rule
//    public ActivityTestRule<AddItem> mAddItemActivityTestRule = new  ActivityTestRule<AddItem>(AddItem.class);

    @Rule
    public IntentsTestRule<AddItem> activityRule =
            new IntentsTestRule<>(AddItem.class);

    @ClassRule
    public static DeviceAnimationTestRule deviceAnimationTestRule = new DeviceAnimationTestRule();


    private AddItem mAddItem = null;
    private String ITEM_NAME = "Pixel 3a";
    private String DESCRIPTION = "BrandNew";
    private String Category = "Electronics";
    private String price = "450";
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db = Singleton.getDb();
    private String uID;
    private String TOAST_STRING = "Item Added";

    @Before
    public void setUp() throws Exception {
        savePickedImage();
        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(getImageResult());
    }

    @After
    public void tearDown() throws Exception {
        activityRule  =null;
    }


    @Test
    public void onCreate() {

    }

    @Test
    public void pickImage_ImagePicked(){
        onView(withId(R.id.addItemImage)).perform(click());
        onView(withId(R.id.addItemImage)).check(matches(hasDrawable()));
    }

    @Test
    public void default_ImageHasDrawable(){
        onView(withId(R.id.addItemImage)).check(matches(hasDrawable()));
    }

    @Test
    public void validateAddItem(){
        onView(withId(R.id.addItemName)).perform(click(), clearText(),typeText(ITEM_NAME), closeSoftKeyboard());
        onView(withId(R.id.addItemCategory)).perform(click());
        onData(allOf(is(instanceOf(String.class)),is("Electronics"))).perform(click());
        onView(withId(R.id.addItemCategory)).check(matches(withSpinnerText(containsString("Electronics"))));

        onView(withId(R.id.addItemDesc)).perform(click(), clearText(),typeText(DESCRIPTION),  closeSoftKeyboard());
        onView(withId(R.id.addItemPrice)).perform(click(), clearText(),typeText(price),  closeSoftKeyboard());

        onView(withId(R.id.addItemButton)).perform(click());

//        onView(withText(R.string.TOAST_STRING)).inRoot(withDecorView(not(is(activityRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));

    }

    private Instrumentation.ActivityResult getImageResult() {
        Intent resultData = new Intent();
        File dir = activityRule.getActivity().getExternalCacheDir();
        File file = new File(dir.getPath(), "pixel3a.jpg");
        Uri uri = Uri.fromFile(file);
        resultData.putExtra("IMAGE", uri);
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
    }



    private void savePickedImage() {
        Bitmap bm = BitmapFactory.decodeResource(activityRule.getActivity().getResources(), R.drawable.pixel3a);
        assertTrue(bm != null);
        File dir = activityRule.getActivity().getExternalCacheDir();
        File file = new File(dir.getPath(), "pixel3a.jpg");
        System.out.println(file.getAbsolutePath());
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
