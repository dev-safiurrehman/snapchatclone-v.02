package com.simcoder.snapchatclone.fragment.main;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.AugmentedFaceNode;
import com.simcoder.snapchatclone.MainActivity;
import com.simcoder.snapchatclone.R;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;
import com.google.ar.core.AugmentedFace;


/**
 * Fragment that handles the camera view. Makes use of the CameraKit library
 * and ARCore/Sceneform libraries in order to achieve this.
 */
public class CameraViewFragment extends Fragment implements View.OnClickListener {

    private View view;
    private CameraView mCamera;
    private ImageButton mProfile;
    private ImageButton mFlash;

    private Session session;
    private ModelRenderable modelRenderable;
    private Texture texture;

    public static CameraViewFragment newInstance() {
        return new CameraViewFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera_view, container, false);

        // Initialize ARCore/Sceneform
        ArFragment arFragment = (ArFragment) getChildFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                for (AugmentedFace face : arFragment.getArSceneView().getSession().getAllTrackables(AugmentedFace.class)) {// Do something with the face mesh
                    // Create a face mesh
                    AugmentedFaceNode augmentedFaceNode = new AugmentedFaceNode(face);
                    // Add the face mesh
                    augmentedFaceNode.setParent(arFragment.getArSceneView().getScene());
                    // Load the 3D face filter
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        ModelRenderable.builder()
                                .setSource(getContext(), R.raw.fox_face)
                                .build()
                                .thenAccept(renderable -> {
                                    modelRenderable = renderable;
                                    // Apply the 3D face filter to the face mesh
//                                    augmentedFaceNode.setFaceMeshTexture(renderer);
                                   modelRenderable.setShadowCaster(false);
                                   modelRenderable.setShadowReceiver(false);
                                });


                    }
                    Texture.builder()
                            .setSource(getContext(), R.drawable.fox_face_mesh_texture)
                            .build()
                            .thenAccept(texture1 ->
                                    this.texture = texture);

                }
            }


        });

        initializeObjects();

        return view;
    }



    /**
     * Initializes the UIsa elements
     */
    private void initializeObjects() {
        mCamera = view.findViewById(R.id.camera);
        ImageButton mReverse = view.findViewById(R.id.reverse);
        mProfile = view.findViewById(R.id.profile);
        EditText mSearch = view.findViewById(R.id.search);
        mFlash = view.findViewById(R.id.flash);

        mReverse.setOnClickListener(this);
        mProfile.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mFlash.setOnClickListener(this);

        mCamera.setFlash(CameraKit.Constants.FLASH_ON);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    if (((MainActivity) getActivity()).getUser().getImage() != null)
                        Glide.with(getActivity())
                                .load(((MainActivity) getActivity()).getUser().getImage())
                                .apply(RequestOptions.circleCropTransform())
                                .into(mProfile);

                    handler.postDelayed(this, 1000);
                }

            }
        }, 1000);




    }

    /**
     * Captures image and updates the variable of the bitmap in the MainActivity
     */
    public void captureImage() {
        mCamera.captureImage(new CameraKitEventCallback<CameraKitImage>() {
            @Override
            public void callback(CameraKitImage cameraKitImage) {
                ((MainActivity) getActivity()).setBitmapToSend(cameraKitImage.getBitmap());
                ((MainActivity) getActivity()).openDisplayImageFragment();
            }
        });
    }

    /**
     * Changes the camera being used by the frontal or back camera
     * depending on the current position
     */
    private void reverseCameraFacing() {
        if (mCamera.getFacing() == CameraKit.Constants.FACING_BACK)
            mCamera.setFacing(CameraKit.Constants.FACING_FRONT);
        else
            mCamera.setFacing(CameraKit.Constants.FACING_BACK);
    }

    /**
     * enables or disables the flash depending on the current setting
     */
    private void flashClick() {
        if (mCamera.getFlash() == CameraKit.Constants.FLASH_ON) {
            mFlash.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_flash_off_black_24dp));
            mCamera.setFlash(CameraKit.Constants.FLASH_OFF);
        } else {
            mFlash.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_flash_on_black_24dp));
            mCamera.setFlash(CameraKit.Constants.FLASH_ON);
        }
    }

    /**
     * Handles onClick events
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reverse:
                reverseCameraFacing();
                break;
            case R.id.flash:
                flashClick();
                break;
            case R.id.profile:
                ((MainActivity) getActivity()).openProfileEditFragment();
                break;
            case R.id.search:
                ((MainActivity) getActivity()).openFindUsersFragment();
                break;
        }
    }


    /**
     * overrides onResume to start the camera
     */
    @Override
    public void onResume() {
        super.onResume();
        initializeObjects();
        mCamera.start();
    }

    /**
     * overrides onPause to stop the camera
     */
    @Override
    public void onPause() {
        mCamera.stop();
        super.onPause();
    }
}

