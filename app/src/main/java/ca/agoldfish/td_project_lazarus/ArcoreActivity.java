package ca.agoldfish.td_project_lazarus;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ArcoreActivity extends AppCompatActivity {

    public static final double MIN_OPENGL_VERSION = 3.0;
    public static final String TAG = ArcoreActivity.class.getName();
    private ArFragment arFragment;
    ModelRenderable modelRenderable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!checkIsSupportedDevice(this)){
            String errorMessage = "Sceneform requires OpenGL ES " + MIN_OPENGL_VERSION + " or later ";
            Log.d(TAG, errorMessage);
            Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_arcore);

        //hide action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //setup AR
        setupARScene();
    }

    private void builder3dModel(){
        ModelRenderable.builder()
                .setSource(this,Uri.parse("house.sfb"))
                .build()
                .thenAccept(renderable-> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(this,"Unable to display model",Toast.LENGTH_LONG).show();

                    return null;
                });
    }


    private void setupARScene(){
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        builder3dModel();

        handleUserTaps();
    }

    private void handleUserTaps() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            // view Renderable me be loaded
            if(modelRenderable == null)
                return;

            //create the anchor on the scene
            AnchorNode anchorNode = createAnchorNode(hitResult);

            //add the view to the scene
            addRenderableToScene(anchorNode, modelRenderable);
        });
    }

    private AnchorNode createAnchorNode(HitResult hitResult){
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);

        anchorNode.setParent(arFragment.getArSceneView().getScene());
        return anchorNode;
    }

    private Node addRenderableToScene(AnchorNode anchorNode, Renderable renderable){
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

        // anchor node knows where it fits into our world
        node.setParent(anchorNode);
        node.setRenderable(renderable);
        node.select();

        return node;
    }

    private boolean checkIsSupportedDevice(final Activity activiy){

        ActivityManager activityManager = (ActivityManager) activiy.getSystemService(Context.ACTIVITY_SERVICE);

        if(activityManager==null){
            Log.d(TAG, "checkIsSupportedDevice: is null");
            return false;
        }

        String openGlVersion = activityManager.getDeviceConfigurationInfo().getGlEsVersion();

        return openGlVersion != null && Double.parseDouble(openGlVersion) >= MIN_OPENGL_VERSION;
    }
}
